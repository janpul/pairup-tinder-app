package com.example.pairup

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.bumptech.glide.Glide

class ConversationActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var recyclerView: RecyclerView
    private lateinit var conversationAdapter: ConversationAdapter
    private lateinit var messageInput: EditText
    private lateinit var sendButton: Button
    private lateinit var contactNameTextView: TextView
    private lateinit var contactImageView: ImageView
    private lateinit var backButton: ImageButton
    private var messagesList = mutableListOf<Message>()
    private var matchId: String = ""
    private var receiverId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_conversation)

        // Initialize Firebase services
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // UI elements
        contactNameTextView = findViewById(R.id.contact_name)
        contactImageView = findViewById(R.id.contact_image)
        backButton = findViewById(R.id.back_button)

        // Back button functionality
        backButton.setOnClickListener {
            val intent = Intent(this, Messages::class.java)
            startActivity(intent)
        }

        // Get match ID passed from the previous activity
        matchId = intent.getStringExtra("MATCH_ID") ?: ""

        // Fetch match details to get the receiver ID
        if (matchId.isNotEmpty()) {
            fetchMatchDetails()
        } else {
            Toast.makeText(this, "No match ID provided", Toast.LENGTH_SHORT).show()
        }

        recyclerView = findViewById(R.id.recycler_view_conversation)
        recyclerView.layoutManager = LinearLayoutManager(this)
        conversationAdapter = ConversationAdapter(messagesList)
        recyclerView.adapter = conversationAdapter

        messageInput = findViewById(R.id.edit_text_message)
        sendButton = findViewById(R.id.button_send)

        sendButton.setOnClickListener {
            val messageText = messageInput.text.toString()
            if (messageText.isNotEmpty() && receiverId.isNotEmpty()) {
                sendMessage(messageText)
                fetchMessages()
                messageInput.text.clear()
            }
        }

        // Fetch previous messages after match details are fetched
        fetchMessages()
    }

    // Fetch match details (user1Id, user2Id) and determine receiverId
    private fun fetchMatchDetails() {
        Log.d("ConversationActivity", "Fetching match details for matchId: $matchId")

        firestore.collection("matches")
            .document(matchId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val user1Id = document.getString("user1Id") ?: ""
                    val user2Id = document.getString("user2Id") ?: ""
                    val currentUserId = auth.currentUser?.uid ?: ""

                    // Determine receiverId
                    receiverId = if (user1Id == currentUserId) user2Id else user1Id

                    // Fetch contact's name and image
                    fetchContactDetails(receiverId)
                } else {
                    Log.e("ConversationActivity", "Match document does not exist.")
                }
            }
            .addOnFailureListener { e ->
                Log.e("ConversationActivity", "Error fetching match details: ${e.message}")
                Toast.makeText(this, "Error fetching match details: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // Fetch contact's name and profile image
    private fun fetchContactDetails(receiverId: String) {
        firestore.collection("users")
            .document(receiverId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val contactName = document.getString("firstName") ?: "Unknown"
                    val profilePicUrl = document.getString("profileImageUrl")

                    contactNameTextView.text = contactName
                    if (!profilePicUrl.isNullOrEmpty()) {
                        Glide.with(this)
                            .load(profilePicUrl)
                            .placeholder(R.drawable.ic_profile)
                            .into(contactImageView)
                    } else {
                        contactImageView.setImageResource(R.drawable.ic_profile)
                    }
                } else {
                    Toast.makeText(this, "Contact not found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Log.e("ConversationActivity", "Error fetching contact details: ${e.message}")
                Toast.makeText(this, "Error fetching contact details: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun fetchMessages() {
        firestore.collection("messages")
            .whereEqualTo("matchId", matchId)
            .orderBy("timestamp")
            .addSnapshotListener { querySnapshot, e ->
                if (e != null) {
                    Log.e("ConversationActivity", "Error fetching messages: ${e.message}")
                    Toast.makeText(this, "Error fetching messages: ${e.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                messagesList.clear()
                querySnapshot?.let { snapshot ->
                    for (document in snapshot) {
                        val message = document.toObject(Message::class.java)
                        messagesList.add(message)
                    }
                    conversationAdapter.notifyDataSetChanged()
                }
            }
    }

    private fun sendMessage(text: String) {
        val currentUser = auth.currentUser ?: return
        val message = Message(
            senderId = currentUser.uid,
            receiverId = receiverId,
            timestamp = System.currentTimeMillis(),
            message = text,
            matchId = matchId
        )

        firestore.collection("messages").add(message)
            .addOnSuccessListener {
                fetchMessages()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error sending message: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // Data class for Message
    data class Message(
        val senderId: String = "",
        val receiverId: String = "",
        val timestamp: Long = 0L,
        val message: String = "",
        val matchId: String = ""
    )
}

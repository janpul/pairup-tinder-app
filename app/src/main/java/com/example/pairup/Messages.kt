package com.example.pairup

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class Messages : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var recyclerView: RecyclerView
    private lateinit var matchesAdapter: MatchesAdapter
    private var matchesList = mutableListOf<Match>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_messages)

        // Initialize Firebase services
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        recyclerView = findViewById(R.id.recycler_view_messages)
        recyclerView.layoutManager = LinearLayoutManager(this)
        matchesAdapter = MatchesAdapter(matchesList) { match ->
            // Check if match.id is not null or empty
            if (match.id.isNotEmpty()) {
                val intent = Intent(this, ConversationActivity::class.java)
                intent.putExtra("MATCH_ID", match.id)
                startActivity(intent)
            } else {
                Toast.makeText(this, "Error: Invalid match ID", Toast.LENGTH_SHORT).show()
            }
        }
        recyclerView.adapter = matchesAdapter

        // Fetch matches from Firestore
        fetchMatches()

        // Navigation buttons (same as in your original code)
        val discoverButton: ImageButton = findViewById(R.id.btn_discover_mess)
        discoverButton.setOnClickListener {
            val intent = Intent(this, Discover::class.java)
            startActivity(intent)
        }

        val profileButton: ImageButton = findViewById(R.id.btn_profile_mess)
        profileButton.setOnClickListener {
            val intent = Intent(this, Profile::class.java)
            startActivity(intent)
        }

        val logoutButton: ImageButton = findViewById(R.id.btn_logout)
        logoutButton.setOnClickListener {
            auth.signOut();
            val intent = Intent(this, MainActivity::class.java)
            Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show()
            startActivity(intent)
            finish()
        }
    }

    private fun fetchMatches() {
        val currentUserId = auth.currentUser?.uid ?: return

        firestore.collection("matches")
            .whereIn("user1Id", listOf(currentUserId)) // Check where the current user is user1Id
            .get()
            .addOnSuccessListener { querySnapshot1 ->
                val tempMatchesList = mutableListOf<Match>()
                for (document in querySnapshot1) {
                    // Use document ID as matchId
                    val match = document.toObject(Match::class.java).apply {
                        id = document.id // Set Firestore document ID as match.id
                    }
                    tempMatchesList.add(match)
                }

                // Also check where the current user is user2Id
                firestore.collection("matches")
                    .whereIn("user2Id", listOf(currentUserId))
                    .get()
                    .addOnSuccessListener { querySnapshot2 ->
                        for (document in querySnapshot2) {
                            val match = document.toObject(Match::class.java).apply {
                                id = document.id // Set Firestore document ID as match.id
                            }
                            tempMatchesList.add(match)
                        }

                        // Update the matches list and notify the adapter
                        matchesList.clear()
                        matchesList.addAll(tempMatchesList)
                        matchesAdapter.notifyDataSetChanged()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Error fetching matches: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error fetching matches: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    data class Match(
        var id: String = "",
        val user1Id: String = "",
        val user2Id: String = ""
    )
}

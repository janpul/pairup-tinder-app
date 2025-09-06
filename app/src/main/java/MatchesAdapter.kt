package com.example.pairup

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.bumptech.glide.Glide
import android.widget.ImageView

class MatchesAdapter(
    private val matches: List<Messages.Match>,
    private val onItemClick: (Messages.Match) -> Unit
) : RecyclerView.Adapter<MatchesAdapter.MatchViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MatchViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.message_item, parent, false)
        return MatchViewHolder(view)
    }

    override fun onBindViewHolder(holder: MatchViewHolder, position: Int) {
        val match = matches[position]
        holder.bind(match)
    }

    override fun getItemCount(): Int = matches.size

    inner class MatchViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val usernameTextView: TextView = itemView.findViewById(R.id.username)
        private val messagePreviewTextView: TextView = itemView.findViewById(R.id.message_preview)
        private val auth = FirebaseAuth.getInstance()
        private val firestore = FirebaseFirestore.getInstance()

        fun bind(match: Messages.Match) {
            val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
            val matchedUserId = when (currentUserId) {
                match.user1Id -> match.user2Id
                match.user2Id -> match.user1Id
                else -> "Unknown"
            }

            val profileImageView: ImageView = itemView.findViewById(R.id.profile_image)

            if (matchedUserId == "Unknown") {
                usernameTextView.text = "Unknown User"
                profileImageView.setImageResource(R.drawable.ic_profile)
                messagePreviewTextView.text = "Tap to start chatting"
                return
            }

            // Real-time listener for user data
            FirebaseFirestore.getInstance().collection("users")
                .document(matchedUserId)
                .addSnapshotListener { documentSnapshot, e ->
                    if (e != null || documentSnapshot == null || !documentSnapshot.exists()) {
                        usernameTextView.text = "Error loading user"
                        profileImageView.setImageResource(R.drawable.ic_profile)
                        return@addSnapshotListener
                    }

                    usernameTextView.text = documentSnapshot.getString("firstName") ?: "Unknown"
                    val profilePicUrl = documentSnapshot.getString("profileImageUrl")
                    if (!profilePicUrl.isNullOrEmpty()) {
                        Glide.with(itemView.context)
                            .load(profilePicUrl)
                            .placeholder(R.drawable.ic_profile)
                            .into(profileImageView)
                    } else {
                        profileImageView.setImageResource(R.drawable.ic_profile)
                    }
                }

            // Real-time listener for messages
            FirebaseFirestore.getInstance().collection("messages")
                .whereEqualTo("matchId", match.id)
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(1)
                .addSnapshotListener { querySnapshot, e ->
                    if (e != null || querySnapshot == null) {
                        Log.e("MatchesAdapter", "Error loading messages: ${e?.message}", e)
                        messagePreviewTextView.text = "Error loading messages"
                        return@addSnapshotListener
                    }

                    val latestMessage = querySnapshot.documents.firstOrNull()
                    if (latestMessage != null) {
                        val message = latestMessage.getString("message") ?: ""
                        val senderId = latestMessage.getString("senderId") ?: ""
                        messagePreviewTextView.text = if (senderId == currentUserId) {
                            "You: $message"
                        } else {
                            message
                        }
                    } else {
                        messagePreviewTextView.text = "Tap to start chatting"
                    }
                }

            itemView.setOnClickListener {
                onItemClick(match)
            }
        }


    }
}

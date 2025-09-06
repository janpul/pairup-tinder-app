package com.example.pairup

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.Timestamp

class Discover : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var cardImageView: ImageView
    private lateinit var profileName: TextView
    private lateinit var profileInfo: TextView
    private lateinit var bio: TextView

    private var profiles = mutableListOf<Profile>()
    private var currentIndex = 0
    private var seenProfiles = mutableSetOf<String>() // Track seen profile IDs

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_discover)

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Initialize views
        cardImageView = findViewById(R.id.imageView7)
        profileName = findViewById(R.id.profile_name)
        profileInfo = findViewById(R.id.profile_info)
        bio = findViewById(R.id.bio)

        // Load profiles from Firestore
        loadSeenProfiles() // Load seen profiles first
        loadProfiles()

        // Navigation buttons
        val profileButton: ImageButton = findViewById(R.id.btn_profile_mess)
        profileButton.setOnClickListener {
            startActivity(Intent(this, com.example.pairup.Profile::class.java))
        }

        val messageButton: ImageButton = findViewById(R.id.btn_messages)
        messageButton.setOnClickListener {
            startActivity(Intent(this, Messages::class.java))
        }

        val logoutButton: ImageButton = findViewById(R.id.btn_logout)
        logoutButton.setOnClickListener {
            auth.signOut()
            Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        // Swipe action buttons
        val likeButton: ImageButton = findViewById(R.id.like)
        likeButton.setOnClickListener {
            handleLike()
        }

        val nopeButton: ImageButton = findViewById(R.id.nope)
        nopeButton.setOnClickListener {
            handleNope()
        }
    }

    private fun loadSeenProfiles() {
        val currentUser = auth.currentUser ?: return
        val userId = currentUser.uid

        firestore.collection("users").document(userId).collection("seen")
            .get()
            .addOnSuccessListener { querySnapshot ->
                seenProfiles.clear()
                for (doc in querySnapshot) {
                    seenProfiles.add(doc.id)
                }
                Log.d("DiscoverActivity", "Loaded seen profiles: $seenProfiles")
            }
            .addOnFailureListener { e ->
                Log.e("DiscoverActivity", "Error loading seen profiles: ${e.message}")
            }
    }

    private fun loadProfiles() {
        val currentUser = auth.currentUser ?: return
        val userId = currentUser.uid

        // Get the current user's preferences
        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                val currentGender = document.getString("gender")
                val interestedIn = document.getString("interestedIn")

                // Define the query based on the user's preference
                val query = when (interestedIn) {
                    "Both" -> firestore.collection("users")
                        .whereIn("gender", listOf("Male", "Female")) // Query for both genders
                    else -> firestore.collection("users")
                        .whereEqualTo("gender", interestedIn)
                }

                // Execute the query and process the results
                query.get()
                    .addOnSuccessListener { querySnapshot ->
                        profiles.clear()
                        for (doc in querySnapshot) {
                            if (doc.id != userId && !seenProfiles.contains(doc.id)) { // Exclude the current user and seen profiles
                                val profile = Profile(
                                    id = doc.id,
                                    name = doc.getString("firstName") ?: "",
                                    info = "${doc.getString("age")}, ${doc.getString("location")}",
                                    bio = doc.getString("bio") ?: "",
                                    profileImageUrl = doc.getString("profileImageUrl") ?: ""
                                )
                                profiles.add(profile)
                            }
                        }
                        // Call updateProfile() even if no profiles are found to handle the UI accordingly
                        updateProfile()
                        if (profiles.isEmpty()) {
                            Toast.makeText(this, "No profiles found", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e("DiscoverActivity", "Error loading profiles: ${e.message}")
                        updateProfile()  // Ensure UI updates even on failure
                    }
            }
            .addOnFailureListener { e ->
                Log.e("DiscoverActivity", "Error loading user data: ${e.message}")
                updateProfile()  // Ensure UI updates even on failure
            }
    }



    private fun updateProfile() {
        if (profiles.isNotEmpty() && currentIndex < profiles.size) {
            val profile = profiles[currentIndex]
            Glide.with(this).load(profile.profileImageUrl).into(cardImageView)
            profileName.text = profile.name
            profileInfo.text = profile.info
            bio.text = "\"${profile.bio}\""

            // Make sure the buttons are visible when there are profiles
            findViewById<ImageButton>(R.id.like).visibility = View.VISIBLE
            findViewById<ImageButton>(R.id.nope).visibility = View.VISIBLE
        } else {
            // Handle the case where there are no profiles left
            profileName.text = "You're all out of profiles!"
            profileInfo.text = "Grab some water, come back soon."
            bio.text = "" // Clear bio text
            cardImageView.setImageResource(R.drawable.sad)

            // Hide the action buttons when there are no more profiles
            findViewById<ImageButton>(R.id.like).visibility = View.GONE
            findViewById<ImageButton>(R.id.nope).visibility = View.GONE
        }
    }

    private fun showNextProfile() {
        // Remove the current profile and update the index
        if (profiles.isNotEmpty()) {
            profiles.removeAt(currentIndex)
            if (currentIndex >= profiles.size) {
                currentIndex = profiles.size - 1
            }
            updateProfile()
        } else {
            updateProfile()  // This will hide the buttons when there are no profiles left
        }
    }

    private fun handleLike() {
        val likedProfile = profiles[currentIndex]
        val currentUserId = auth.currentUser?.uid ?: return

        // Save the "like" action
        firestore.collection("users").document(likedProfile.id)
            .collection("likes").document(currentUserId).set(mapOf("timestamp" to Timestamp.now()))
            .addOnSuccessListener {
                Log.d("DiscoverActivity", "Like saved for user ${likedProfile.id}")

                // Check if this is a mutual match
                checkForMatch(likedProfile.id, currentUserId)

                // Add the profile to the "seen" list
                saveSeenProfile(likedProfile.id)

                // Remove the liked profile from the list
                showNextProfile()
            }
            .addOnFailureListener { e ->
                Log.e("DiscoverActivity", "Error saving like: ${e.message}")
            }
    }

    private fun handleNope() {
        val nopeProfile = profiles[currentIndex]

        // Add the profile to the "seen" list
        saveSeenProfile(nopeProfile.id)

        // Show the next profile
        showNextProfile()
    }

    private fun saveSeenProfile(profileId: String) {
        val currentUser = auth.currentUser ?: return
        val userId = currentUser.uid

        // Save the profile ID to the "seen" collection in Firestore
        firestore.collection("users").document(userId).collection("seen")
            .document(profileId).set(mapOf("timestamp" to Timestamp.now()))
            .addOnSuccessListener {
                Log.d("DiscoverActivity", "Profile $profileId added to seen list")
                seenProfiles.add(profileId) // Update in-memory seenProfiles set
            }
            .addOnFailureListener { e ->
                Log.e("DiscoverActivity", "Error saving seen profile: ${e.message}")
            }
    }

    private fun checkForMatch(likedUserId: String, currentUserId: String) {
        firestore.collection("users").document(currentUserId)
            .collection("likes").document(likedUserId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    // Match found! Save to matches collection
                    saveMatch(currentUserId, likedUserId)
                }
            }
            .addOnFailureListener { e ->
                Log.e("DiscoverActivity", "Error checking match: ${e.message}")
            }
    }

    private fun saveMatch(userId1: String, userId2: String) {
        val matchData = hashMapOf(
            "user1Id" to userId1,
            "user2Id" to userId2,
            "timestamp" to Timestamp.now()
        )

        firestore.collection("matches").add(matchData)
            .addOnSuccessListener {
                Log.d("DiscoverActivity", "Match saved successfully!")
                Toast.makeText(this, "It's a match!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Log.e("DiscoverActivity", "Error saving match: ${e.message}")
            }
    }

    data class Profile(
        val id: String,
        val name: String,
        val info: String,
        val bio: String,
        val profileImageUrl: String
    )
}
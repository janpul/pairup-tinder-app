package com.example.pairup

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.bumptech.glide.Glide // For loading the profile image

class Profile : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        // Initialize Firebase services
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Find TextViews to display the data
        val fullNameDisplay: TextView = findViewById(R.id.lblfullnameprofile)
        val locationDisplay: TextView = findViewById(R.id.lbllocationprofile)
        val genderDisplay: TextView = findViewById(R.id.lblgenderprofile)
        val bioDisplay: TextView = findViewById(R.id.lblbioprofile)
        val profileImage: ImageView = findViewById(R.id.profileImage)

        // Fetch user data from Firestore
        val userId = auth.currentUser?.uid
        if (userId != null) {
            firestore.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        // Get data from Firestore document
                        val firstName = document.getString("firstName")
                        val lastName = document.getString("lastName")
                        val location = document.getString("location")
                        val age = document.getString("age")
                        val gender = document.getString("gender")
                        val bio = document.getString("bio")
                        val profileImageUrl = document.getString("profileImageUrl")

                        // Combine first name and last name
                        val fullName = "$firstName $lastName, $age"
                        fullNameDisplay.text = fullName

                        // Set the rest of the user data
                        locationDisplay.text = "Location: $location" ?: "No Location"
                        genderDisplay.text = "Gender: $gender" ?: "No Gender"
                        bioDisplay.text = bio ?: "No Bio"

                        // Load profile image using Glide (or another image loading library)
                        if (!profileImageUrl.isNullOrEmpty()) {
                            Glide.with(this)
                                .load(profileImageUrl)
                                .into(profileImage)
                        }

                    } else {
                        Toast.makeText(this, "No such user found", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Error fetching data: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        }

        // Navigate to Discover
        val discoverButton: ImageButton = findViewById(R.id.btn_discover_mess)
        discoverButton.setOnClickListener {
            val intent = Intent(this, Discover::class.java)
            startActivity(intent)
        }

        // Navigate to Messages
        val messagesButton: ImageButton = findViewById(R.id.btn_messages)
        messagesButton.setOnClickListener {
            val intent = Intent(this, Messages::class.java)
            startActivity(intent)
        }

        // Logout Button
        val logoutButton: ImageButton = findViewById(R.id.btn_logout)
        logoutButton.setOnClickListener {
            auth.signOut();
            val intent = Intent(this, MainActivity::class.java)
            Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show()
            startActivity(intent);
            finish();
        }
    }
}

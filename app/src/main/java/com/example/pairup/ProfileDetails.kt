package com.example.pairup

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.*

class ProfileDetails : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage

    private lateinit var profileImage: ImageView
    private lateinit var cameraIcon: ImageView
    private var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_details)

        // Initialize Firebase services
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        val firstNameInput: EditText = findViewById(R.id.first_name)
        val lastNameInput: EditText = findViewById(R.id.last_name)
        val emailInput: EditText = findViewById(R.id.email)
        val passwordInput: EditText = findViewById(R.id.password)
        val nextButton: Button = findViewById(R.id.btn_ProfDetails)
        profileImage = findViewById(R.id.profile_image)
        cameraIcon = findViewById(R.id.camera_icon)

        // Set up image selection
        cameraIcon.setOnClickListener {
            selectImageFromGallery()
        }

        nextButton.setOnClickListener {
            val firstName = firstNameInput.text.toString()
            val lastName = lastNameInput.text.toString()
            val email = emailInput.text.toString()
            val password = passwordInput.text.toString()

            // Validate input
            if (firstName.isBlank() || lastName.isBlank() || email.isBlank() || password.isBlank()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Ensure image is selected
            if (imageUri == null) {
                Toast.makeText(this, "Please upload a profile image", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Disable button to prevent multiple clicks
            nextButton.isEnabled = false

            // Register user with Firebase Authentication
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val user = auth.currentUser
                        val userId = user?.uid ?: ""

                        // Upload image and save user profile
                        uploadImageToFirebase(userId) { imageUrl ->
                            saveUserProfile(userId, firstName, lastName, email, imageUrl)
                        }
                    } else {
                        Toast.makeText(this, "Authentication failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                        nextButton.isEnabled = true // Re-enable the button if authentication fails
                    }
                }
        }
    }

    private fun selectImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, 1000)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1000 && resultCode == Activity.RESULT_OK) {
            imageUri = data?.data
            profileImage.setImageURI(imageUri) // Display selected image in the ImageView
        }
    }

    private fun uploadImageToFirebase(userId: String, callback: (String) -> Unit) {
        val storageRef = storage.reference.child("profile_images/$userId/${UUID.randomUUID()}.jpg")
        imageUri?.let { uri ->
            storageRef.putFile(uri)
                .addOnSuccessListener { taskSnapshot ->
                    storageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                        callback(downloadUri.toString())
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Image upload failed: ${exception.message}", Toast.LENGTH_SHORT).show()
                    callback("default") // Use default image if upload fails
                }
        }
    }

    private fun saveUserProfile(userId: String, firstName: String, lastName: String, email: String, profileImageUrl: String) {
        val userData = hashMapOf(
            "firstName" to firstName,
            "lastName" to lastName,
            "email" to email,
            "profileImageUrl" to profileImageUrl
        )

        firestore.collection("users").document(userId)
            .set(userData)
            .addOnCompleteListener { task ->

                if (task.isSuccessful) {
                    // Pass data to the next activity
                    val intent = Intent(this, Interests::class.java)
                    intent.putExtra("FIRST_NAME", firstName)
                    intent.putExtra("LAST_NAME", lastName)
                    intent.putExtra("EMAIL", email)
                    intent.putExtra("PROFILE_IMAGE_URL", profileImageUrl)
                    startActivity(intent)
                } else {
                    Toast.makeText(this, "Failed to save profile", Toast.LENGTH_SHORT).show()
                }
            }
    }
}


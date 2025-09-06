package com.example.pairup

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.slider.RangeSlider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AdditionalInfo : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_additional_info)

        // Initialize Firebase Auth and Firestore
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Retrieve data from previous activities
        val firstName = intent.getStringExtra("FIRST_NAME")
        val lastName = intent.getStringExtra("LAST_NAME")
        val email = intent.getStringExtra("EMAIL")
        val interests = intent.getStringExtra("INTERESTS")
        val profileImageUrl = intent.getStringExtra("PROFILE_IMAGE_URL") // Retrieve profile image URL

        val locationInput: EditText = findViewById(R.id.location)
        val ageInput: EditText = findViewById(R.id.age)
        val genderSpinner: Spinner = findViewById(R.id.gender)
        val interestedInSpinner: Spinner = findViewById(R.id.interested_in)
        val bioInput: EditText = findViewById(R.id.bio)
        val nextButton: Button = findViewById(R.id.btn_AddInfo)

        // Set up Spinners
        setupSpinner(genderSpinner, R.array.gender_options)
        setupSpinner(interestedInSpinner, R.array.interest_options)

        // Distance Range Slider
        val distanceRangeSlider: RangeSlider = findViewById(R.id.distance_range)
        distanceRangeSlider.values = listOf(10.0f, 50.0f)

        // Age Range Slider
        val ageRangeSlider: RangeSlider = findViewById(R.id.age_range)
        ageRangeSlider.values = listOf(18.0f, 30.0f)

        // Handle Spinner selections (optional)
        genderSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Do nothing
            }
        }

        interestedInSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Do nothing
            }
        }

        nextButton.setOnClickListener {
            val location = locationInput.text.toString()
            val age = ageInput.text.toString()
            val gender = genderSpinner.selectedItem.toString()
            val interestedIn = interestedInSpinner.selectedItem.toString()
            val bio = bioInput.text.toString()
            val distanceRange = distanceRangeSlider.values
            val ageRange = ageRangeSlider.values

            if (location.isEmpty() || age.isEmpty() || gender.isEmpty() || bio.isEmpty() || interestedIn.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val userId = auth.currentUser?.uid
            if (userId != null) {
                val user = hashMapOf(
                    "firstName" to firstName,
                    "lastName" to lastName,
                    "email" to email,
                    "interests" to interests,
                    "location" to location,
                    "age" to age,
                    "gender" to gender,
                    "bio" to bio,
                    "profileImageUrl" to profileImageUrl, // Include profile image URL
                    "interestedIn" to interestedIn, // Include interested in
                    "distanceRange" to distanceRange,
                    "ageRange" to ageRange
                )

                firestore.collection("users").document(userId).set(user)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "Profile saved successfully!", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this, Discover::class.java)
                            startActivity(intent)
                        } else {
                            Toast.makeText(this, "Failed to save profile. Try again.", Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                Toast.makeText(this, "User not authenticated. Please log in again.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupSpinner(spinner: Spinner, arrayResId: Int) {
        ArrayAdapter.createFromResource(
            this,
            arrayResId,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter
        }
    }
}

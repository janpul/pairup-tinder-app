package com.example.pairup

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class Interests : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_interests)

        // Retrieve data from ProfileDetails
        val firstName = intent.getStringExtra("FIRST_NAME")
        val lastName = intent.getStringExtra("LAST_NAME")
        val email = intent.getStringExtra("EMAIL")
        val profileImageUrl = intent.getStringExtra("PROFILE_IMAGE_URL") // Retrieve the profile image URL

        // Initialize checkboxes
        val photography = findViewById<CheckBox>(R.id.checkbox_photography)
        val shopping = findViewById<CheckBox>(R.id.checkbox_shopping)
        val karaoke = findViewById<CheckBox>(R.id.checkbox_karaoke)
        val yoga = findViewById<CheckBox>(R.id.checkbox_yoga)
        val cooking = findViewById<CheckBox>(R.id.checkbox_cooking)
        val sporty = findViewById<CheckBox>(R.id.checkbox_sporty)
        val running = findViewById<CheckBox>(R.id.checkbox_running)
        val music = findViewById<CheckBox>(R.id.checkbox_music)

        val nextButton: Button = findViewById(R.id.btn_interests)

        nextButton.setOnClickListener {
            // Gather selected interests
            val selectedInterests = mutableListOf<String>()
            if (photography.isChecked) selectedInterests.add("Photography")
            if (shopping.isChecked) selectedInterests.add("Shopping")
            if (karaoke.isChecked) selectedInterests.add("Karaoke")
            if (yoga.isChecked) selectedInterests.add("Yoga")
            if (cooking.isChecked) selectedInterests.add("Cooking")
            if (sporty.isChecked) selectedInterests.add("Sporty")
            if (running.isChecked) selectedInterests.add("Running")
            if (music.isChecked) selectedInterests.add("Music")

            // Ensure at least one interest is selected
            if (selectedInterests.isEmpty()) {
                Toast.makeText(this, "Please select at least one interest", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Retrieve and pass the profile image URL
            val profileImageUrl = intent.getStringExtra("PROFILE_IMAGE_URL")

            // Pass data to AdditionalInfo
            val intent = Intent(this, AdditionalInfo::class.java)
            intent.putExtra("FIRST_NAME", firstName)
            intent.putExtra("LAST_NAME", lastName)
            intent.putExtra("EMAIL", email)
            intent.putExtra("INTERESTS", selectedInterests.joinToString(", "))
            intent.putExtra("PROFILE_IMAGE_URL", profileImageUrl) // Pass profile image URL
            startActivity(intent)
        }
    }
}

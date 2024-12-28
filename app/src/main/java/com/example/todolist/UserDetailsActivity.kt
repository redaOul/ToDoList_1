package com.example.todolist

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.todolist.databinding.ActivityUserDetailsBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class UserDetailsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUserDetailsBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth
        // Get the currently signed-in user
        val user = auth.currentUser

        // Populate the welcome message
        val userName = user?.displayName ?: "User"
        binding.tvWelcome.text = "Welcome, $userName!"

        // Populate user details
        if (user != null) {
            val userDetails = """
                UID: ${user.uid}
                Email: ${user.email ?: "N/A"}
                Name: ${user.displayName ?: "N/A"}
                Phone: ${user.phoneNumber ?: "N/A"}
                Email Verified: ${user.isEmailVerified}
            """.trimIndent()

            binding.tvUserDetails.text = userDetails
        } else {
            binding.tvUserDetails.text = "No user information available."
        }

        setupClickListeners()

        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, AddTaskActivity::class.java))
            finish() // Finish SplashActivity so it doesn't stay in the back stack
        }, 4000) // 2000 ms delay for splash screen
    }

    private fun setupClickListeners() {
        binding.SignOutBtn.setOnClickListener{
            performSignOut()
        }
    }

    private fun performSignOut() {
        auth.signOut()
        binding.tvWelcome.text = "Bye Bye!"
        val intent = Intent(this, AuthActivity::class.java)
        startActivity(intent)
        finish()
    }
}
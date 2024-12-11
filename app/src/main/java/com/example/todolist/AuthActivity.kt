package com.example.todolist

import android.content.Intent
import android.content.res.ColorStateList
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.content.ContextCompat
import com.example.todolist.databinding.ActivityAuthBinding
import android.widget.Toast

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.Firebase

class AuthActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAuthBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Auth
        auth = Firebase.auth

        setupClickListeners()
        // Start with Sign In form
        binding.viewFlipper.displayedChild = 0
    }

    private fun setupClickListeners() {
        binding.apply {
            btnSignIn.setOnClickListener {
                // Switch to sign in form
                viewFlipper.displayedChild = 0
                updateTabButtons(isSignIn = true)
            }

            btnSignUp.setOnClickListener {
                // Switch to sign up form
                viewFlipper.displayedChild = 1
                updateTabButtons(isSignIn = false)
            }

            btnLoginSignIn.setOnClickListener {
                val email = etEmailSignIn.text.toString()
                val password = etPasswordSignIn.text.toString()

                if (validateSignInInput(email, password)) {
                    performSignIn(email, password)
                }
            }

            btnRegister.setOnClickListener {
                val name = etFullName.text.toString()
                val email = etEmailSignUp.text.toString()
                val password = etPasswordSignUp.text.toString()

                if (validateSignUpInput(name, email, password)) {
                    performSignUp(name, email, password)
                }
            }

            tvForgotPassword.setOnClickListener {
                // Handle forgot password
            }

            btnGoogle.setOnClickListener {
                performGoogleSignIn()
            }
        }
    }

    private fun updateTabButtons(isSignIn: Boolean) {
        binding.apply {
            if (isSignIn) {
                // Unselected: Sign Up button
                btnSignUp.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this@AuthActivity, R.color.white))
                btnSignUp.setTextColor(ContextCompat.getColor(this@AuthActivity, R.color.red_500))
                btnSignUp.strokeColor = ColorStateList.valueOf(ContextCompat.getColor(this@AuthActivity, R.color.red_500))

                // Selected: Sign In button
                btnSignIn.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this@AuthActivity, R.color.red_500))
                btnSignIn.setTextColor(ContextCompat.getColor(this@AuthActivity, R.color.white))
                btnSignIn.strokeColor = ColorStateList.valueOf(ContextCompat.getColor(this@AuthActivity, R.color.red_500))
            } else {
                // Unselected: Sign In button
                btnSignIn.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this@AuthActivity, R.color.white))
                btnSignIn.setTextColor(ContextCompat.getColor(this@AuthActivity, R.color.red_500))
                btnSignIn.strokeColor = ColorStateList.valueOf(ContextCompat.getColor(this@AuthActivity, R.color.red_500))

                // Selected: Sign Up button
                btnSignUp.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this@AuthActivity, R.color.red_500))
                btnSignUp.setTextColor(ContextCompat.getColor(this@AuthActivity, R.color.white))
                btnSignUp.strokeColor = ColorStateList.valueOf(ContextCompat.getColor(this@AuthActivity, R.color.red_500))
            }
        }
    }

    private fun validateSignInInput(email: String, password: String): Boolean {
        var isValid = true

        if (email.isEmpty()) {
            binding.etEmailSignIn.error = "Email cannot be empty"
            isValid = false
        }

        if (password.isEmpty()) {
            binding.etPasswordSignIn.error = "Password cannot be empty"
            isValid = false
        }

        return isValid
    }

    private fun validateSignUpInput(name: String, email: String, password: String): Boolean {
        var isValid = true

        if (name.isEmpty()) {
            binding.etFullName.error = "Name cannot be empty"
            isValid = false
        }

        if (email.isEmpty()) {
            binding.etEmailSignUp.error = "Email cannot be empty"
            isValid = false
        }

        if (password.isEmpty()) {
            binding.etPasswordSignUp.error = "Password cannot be empty"
            isValid = false
        }

        return isValid
    }

    private fun performSignIn(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
        .addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                binding.result.text="signInWithEmail:success"
                val user = auth.currentUser
                redirectToUserDetails()
            } else {
                binding.result.text="signInWithEmail:failure"
            }
        }
    }

    private fun performSignUp(name: String, email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    binding.result.text="createUserWithEmail:success"
                    val user = auth.currentUser
                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName(name)  // Set the name
                        .build()

                    user?.updateProfile(profileUpdates)?.addOnCompleteListener { profileTask ->
                        if (profileTask.isSuccessful) {
                            redirectToUserDetails()
                        }
                    }
                } else {
                    binding.result.text="createUserWithEmail:failure"
                }
            }
    }


    private fun performGoogleSignIn() {
        // Implement Google Sign In here
    }

    private fun redirectToUserDetails() {
        val intent = Intent(this, UserDetailsActivity::class.java)
        startActivity(intent)
        finish()
    }
}
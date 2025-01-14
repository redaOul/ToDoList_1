package com.example.todolist.activity

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.todolist.databinding.ActivityForgotPasswordBinding
import com.example.todolist.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth

class ForgotPasswordActivity : AppCompatActivity() {
    private lateinit var binding: ActivityForgotPasswordBinding
    private lateinit var authRepository: AuthRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        authRepository = AuthRepository(FirebaseAuth.getInstance())

        binding.resetButton.setOnClickListener {
            resetPassword()
        }
    }

    private fun resetPassword() {
        val email = binding.emailText.text.toString().trim()
        if (!validateEmail(email)) return

        authRepository.resetPassword(email) {
            success, errorMessage ->

                if (success) {
                    binding.result.visibility = View.VISIBLE
                    binding.result.text = "Reset link sent to your email\n return to login page"
                    Log.d("Reset Password", "Reset link sent to your email")
                } else {
                    binding.result.text = "$errorMessage"
                    Log.d("Reset Password", "Error: $errorMessage")
                }
        }
    }

    private fun validateEmail(email: String): Boolean {
        if (email.isEmpty()) {
            binding.emailText.error = "Email is required"
            return false
        } else {
            binding.emailText.error = null
        }
        return true
    }
}
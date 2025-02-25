package com.example.todolist.activity

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.todolist.R
import com.example.todolist.databinding.ActivityForgotPasswordBinding
import com.example.todolist.repository.AuthRepository
import com.example.todolist.utils.ValidationUtils
import com.google.android.material.snackbar.Snackbar
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
        val email = binding.emailText.text.toString()

        val (isEmailValid, emailError) = ValidationUtils.validateEmail(email)
        if (isEmailValid) {
            binding.emailLayout.error = null
            performResetPassword(email)
        } else {
            binding.emailLayout.error = emailError
        }
    }

    private fun performResetPassword(email: String) {
        authRepository.resetPassword(email) { success, errorMessage ->
            binding.result.visibility = View.VISIBLE
            if (success) {
                showSnackBar("Reset link sent to your email\nReturn to login page")
            } else {
                showSnackBar("Error: $errorMessage", false)
            }
        }
    }

    private fun showSnackBar(message: String, state: Boolean = true) {
        val textColor: Int
        val backgroundColor: Int

        if (state){
            textColor = ContextCompat.getColor(this, R.color.medium_green)
            backgroundColor = ContextCompat.getColor(this, R.color.mint_cream)
        } else {
            textColor = ContextCompat.getColor(this, R.color.deep_red)
            backgroundColor = ContextCompat.getColor(this, R.color.rose_white)
        }

        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).apply {
            setTextColor(textColor)
            setBackgroundTint(backgroundColor)
            show()
        }
    }
}
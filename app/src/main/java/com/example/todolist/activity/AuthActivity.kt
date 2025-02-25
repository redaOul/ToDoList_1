package com.example.todolist.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import com.example.todolist.databinding.ActivityAuthBinding
import com.example.todolist.R
import com.example.todolist.repository.AuthRepository
import com.example.todolist.utils.ValidationUtils
import com.google.firebase.auth.FirebaseAuth
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen


class AuthActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAuthBinding
    private lateinit var authRepository: AuthRepository
    private var isUserChecked = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val splashScreen = installSplashScreen()
        authRepository = AuthRepository(FirebaseAuth.getInstance())

        splashScreen.setKeepOnScreenCondition { !isUserChecked }
        checkUserAndRoute()
    }

    private fun checkUserAndRoute() {
        isUserChecked = true
        if (authRepository.getCurrentUser() != null) {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        } else {
            binding = ActivityAuthBinding.inflate(layoutInflater)
            setContentView(binding.root)
            binding.signInForm.visibility = View.VISIBLE
            setupClickListeners()
        }
    }

    private fun setupClickListeners() {
        binding.apply {
            // Switch to sign in form
            btnSignIn.setOnClickListener {
                signInForm.visibility = View.VISIBLE
                signUpForm.visibility = View.GONE
                updateTabButtons(true)
            }

            // Switch to sign up form
            btnSignUp.setOnClickListener {
                signUpForm.visibility = View.VISIBLE
                signInForm.visibility = View.GONE
                updateTabButtons(false)
            }

            btnLoginSignIn.setOnClickListener {
                signInProcess()
            }

            btnRegister.setOnClickListener {
                signUpProcess()
            }

            tvForgotPassword.setOnClickListener {
                redirectToPasswordReset()
            }
        }
    }

    private fun updateTabButtons(isSignIn: Boolean) {
        val red500 = ContextCompat.getColorStateList(this@AuthActivity, R.color.red_500)
        val white = ContextCompat.getColorStateList(this@AuthActivity, R.color.white)

        binding.apply {
            if (isSignIn) {
                // Unselected: Sign Up button
                btnSignUp.backgroundTintList = white
                btnSignUp.setTextColor(red500)

                // Selected: Sign In button
                btnSignIn.backgroundTintList = red500
                btnSignIn.setTextColor(white)

                // clear input fields and errors
                emailSignInLayout.error = null
                emailSignInLayout.isErrorEnabled = false
                emailSignInText.text = null
                passwordSignInLayout.error = null
                passwordSignInLayout.isErrorEnabled = false
                passwordSignInText.text = null
            } else {
                // Unselected: Sign In button
                btnSignIn.backgroundTintList = white
                btnSignIn.setTextColor(red500)

                // Selected: Sign Up button
                btnSignUp.backgroundTintList = red500
                btnSignUp.setTextColor(white)

                // clear input fields and errors
                nameSignUpLayout.error = null
                nameSignUpLayout.isErrorEnabled = false
                nameSignUpText.text = null
                emailSignUpLayout.error = null
                emailSignUpLayout.isErrorEnabled = false
                emailSignUpText.text = null
                passwordSignUpLayout.error = null
                passwordSignUpLayout.isErrorEnabled = false
                passwordSignUpText.text = null
            }

            result.text = ""
            result.visibility = View.GONE
        }
    }

    private fun signInProcess() {
        val email = binding.emailSignInText.text.toString()
        val password = binding.passwordSignInText.text.toString()

        val (isEmailValid, emailError) = ValidationUtils.validateEmail(email)
        if (!isEmailValid) {
            binding.emailSignInLayout.error = emailError
        }

        val (isPasswordValid, passwordError) = ValidationUtils.validatePassword(password)
        if (!isPasswordValid) {
            binding.passwordSignInLayout.error = passwordError
        }

        if (isEmailValid && isPasswordValid) {
            binding.emailSignInLayout.error = null
            binding.passwordSignInLayout.error = null
            performSignIn(email, password)
        }
    }

    private fun performSignIn(email: String, password: String) {
        authRepository.signIn(email, password) { success, message ->
            if (success) {
                redirectToUserDetails()
            } else {
                binding.result.visibility = View.VISIBLE
                binding.result.text = message
            }
        }
    }

    private fun signUpProcess(){
        val name = binding.nameSignUpText.text.toString()
        val email = binding.emailSignUpText.text.toString()
        val password = binding.passwordSignUpText.text.toString()

        val (isNameValid, nameError) = ValidationUtils.validateName(name)
        if (!isNameValid) {
            binding.nameSignUpLayout.error = nameError
        }

        val (isEmailValid, emailError) = ValidationUtils.validateEmail(email)
        if (!isEmailValid) {
            binding.emailSignUpLayout.error = emailError
        }

        val (isPasswordValid, passwordError) = ValidationUtils.validatePassword(password)
        if (!isPasswordValid) {
            binding.passwordSignUpLayout.error = passwordError
        }

        if (isNameValid && isEmailValid && isPasswordValid) {
            binding.nameSignUpLayout.error = null
            binding.emailSignUpLayout.error = null
            binding.passwordSignUpLayout.error = null
            performSignUp(name, email, password)
        }
    }

    private fun performSignUp(name: String, email: String, password: String) {
        authRepository.signUp(name, email, password) { success, message ->
            if (success) {
                redirectToUserDetails()
            } else {
                binding.result.visibility = View.VISIBLE
                binding.result.text = message
            }
        }
    }

    private fun redirectToPasswordReset(){
        startActivity(Intent(this, ForgotPasswordActivity::class.java))
    }

    private fun redirectToUserDetails() {
        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }
}
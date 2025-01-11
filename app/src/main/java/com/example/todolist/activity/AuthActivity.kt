package com.example.todolist.activity

import android.content.Intent
import android.content.res.ColorStateList
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
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
        val splashScreen = installSplashScreen()
        authRepository = AuthRepository(FirebaseAuth.getInstance())

        splashScreen.setKeepOnScreenCondition { !isUserChecked }
        checkUserAndRoute()

        super.onCreate(savedInstanceState)

        setupClickListeners()
    }

    private fun checkUserAndRoute() {
        isUserChecked = true
        if (authRepository.getCurrentUser() != null) {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        } else {
            binding = ActivityAuthBinding.inflate(layoutInflater)
            setContentView(binding.root)
            binding.viewFlipper.displayedChild = 0
            // Your normal Activity1 initialization code here
        }
    }

    private fun setupClickListeners() {
        // clear result text after switching
        binding.apply {
            // Switch to sign in form
            btnSignIn.setOnClickListener {
                viewFlipper.displayedChild = 0
                updateTabButtons(isSignIn = true)
            }

            // Switch to sign up form
            btnSignUp.setOnClickListener {
                viewFlipper.displayedChild = 1
                updateTabButtons(isSignIn = false)
            }

            btnLoginSignIn.setOnClickListener {
                val email = etEmailSignIn.text.toString()
                val password = etPasswordSignIn.text.toString()

                val (isValid, error) = ValidationUtils.validateSignInInput(email, password)
                if (isValid) {
                    performSignIn(email, password)
                } else {
                    binding.result.text = error
                }
            }

            btnRegister.setOnClickListener {
                val name = etFullName.text.toString()
                val email = etEmailSignUp.text.toString()
                val password = etPasswordSignUp.text.toString()

                val (isValid, error) = ValidationUtils.validateSignUpInput(name, email, password)
                if (isValid) {
                    performSignUp(name, email, password)
                } else {
                    binding.result.text = error
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
                btnSignUp.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this@AuthActivity,
                    R.color.white
                ))
                btnSignUp.setTextColor(ContextCompat.getColor(this@AuthActivity, R.color.red_500))
                btnSignUp.strokeColor = ColorStateList.valueOf(ContextCompat.getColor(this@AuthActivity,
                    R.color.red_500
                ))

                // Selected: Sign In button
                btnSignIn.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this@AuthActivity,
                    R.color.red_500
                ))
                btnSignIn.setTextColor(ContextCompat.getColor(this@AuthActivity, R.color.white))
                btnSignIn.strokeColor = ColorStateList.valueOf(ContextCompat.getColor(this@AuthActivity,
                    R.color.red_500
                ))
            } else {
                // Unselected: Sign In button
                btnSignIn.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this@AuthActivity,
                    R.color.white
                ))
                btnSignIn.setTextColor(ContextCompat.getColor(this@AuthActivity, R.color.red_500))
                btnSignIn.strokeColor = ColorStateList.valueOf(ContextCompat.getColor(this@AuthActivity,
                    R.color.red_500
                ))

                // Selected: Sign Up button
                btnSignUp.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this@AuthActivity,
                    R.color.red_500
                ))
                btnSignUp.setTextColor(ContextCompat.getColor(this@AuthActivity, R.color.white))
                btnSignUp.strokeColor = ColorStateList.valueOf(ContextCompat.getColor(this@AuthActivity,
                    R.color.red_500
                ))
            }
        }
    }

    private fun performSignIn(email: String, password: String) {
        authRepository.signIn(email, password) { success, message ->
            if (success) {
                redirectToUserDetails()
            } else {
                binding.result.text = message
            }
        }
    }

    private fun performSignUp(name: String, email: String, password: String) {
        authRepository.signUp(name, email, password) { success, message ->
            if (success) {
                redirectToUserDetails()
            } else {
                binding.result.text = message
            }
        }
    }


    private fun performGoogleSignIn() {
        // Implement Google Sign In here
    }

    private fun redirectToUserDetails() {
        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }
}
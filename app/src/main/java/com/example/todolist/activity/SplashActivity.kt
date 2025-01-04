package com.example.todolist.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.os.Handler
import android.os.Looper
import com.example.todolist.R
import com.example.todolist.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth

class SplashActivity : AppCompatActivity() {
    private lateinit var authRepository: AuthRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        authRepository = AuthRepository(FirebaseAuth.getInstance())

        Handler(Looper.getMainLooper()).postDelayed({
            if (authRepository.getCurrentUser() != null)
                startActivity(Intent(this, HomeActivity::class.java))
            else
                startActivity(Intent(this, AuthActivity::class.java))
            finish()
        }, 2000)
    }
}

package com.example.todolist.utils

import android.util.Patterns

object ValidationUtils {
    // Centralized email validation
    private fun validateEmail(email: String): Pair<Boolean, String?> {
        if (email.isEmpty()) return Pair(false, "Email cannot be empty")
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) return Pair(false, "Email is not valid")
        return Pair(true, null)
    }

    // Centralized password validation
    private fun validatePassword(password: String): Pair<Boolean, String?> {
        if (password.isEmpty()) return Pair(false, "Password cannot be empty")
        return Pair(true, null)
    }

    // Validation for sign-in inputs
    fun validateSignInInput(email: String, password: String): Pair<Boolean, String?> {
        validateEmail(email).let {
            if (!it.first) return it
        }
        validatePassword(password).let {
            if (!it.first) return it
        }
        return Pair(true, null)
    }

    // Validation for sign-up inputs
    fun validateSignUpInput(name: String, email: String, password: String): Pair<Boolean, String?> {
        if (name.isEmpty()) return Pair(false, "Name cannot be empty")
        validateEmail(email).let {
            if (!it.first) return it
        }
        validatePassword(password).let {
            if (!it.first) return it
        }
        return Pair(true, null)
    }
}
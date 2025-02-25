package com.example.todolist.utils

import android.util.Patterns

object ValidationUtils {
    // Centralized email validation
    fun validateEmail(email: String): Pair<Boolean, String?> {
        if (email.isEmpty()) return Pair(false, "Email cannot be empty")
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) return Pair(false, "Email is not valid")
        return Pair(true, null)
    }

    // Centralized password validation
    fun validatePassword(password: String): Pair<Boolean, String?> {
        if (password.isEmpty()) return Pair(false, "Password cannot be empty")
        if (password.length < 6) return Pair(false, "Password must be at least 6 characters long")
        return Pair(true, null)
    }

    // Centralized password validation
    fun validateName(name: String): Pair<Boolean, String?> {
        if (name.isEmpty()) return Pair(false, "Name cannot be empty")
        if (name.length >= 20) return Pair(false, "Name cannot be longer than 20 characters")
        return Pair(true, null)
    }

    fun validateBio(bio: String): Pair<Boolean, String?> {
        return if (bio.length >= 30) Pair(false, "Bio cannot be longer than 30 characters") else Pair(true, null)
    }

    fun validatePasswordsMatch(oldPassword: String, newPassword: String): Pair<Boolean, String?> {
        return if (oldPassword == newPassword) return Pair(true, "Passwords can not match") else Pair(false, null)
    }

    fun validatePasswordConfirmation(newPassword: String, confirmPassword: String): Pair<Boolean, String?> {
        if (confirmPassword.isEmpty()) return Pair(false, "Password confirmation cannot be empty")
        if (newPassword != confirmPassword) return Pair(false, "Password is not confirmed")
        return Pair(true, null)
    }

    fun validateAddListInput(name: String): Pair<Boolean, String?>{
        if (name.isEmpty()) return Pair(false, "Name cannot be empty")
        if (name.length >= 20) return Pair(false, "Name cannot be longer than 20 characters")
        return Pair(true, null)
    }
}
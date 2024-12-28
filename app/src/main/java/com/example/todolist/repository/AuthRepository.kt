package com.example.todolist.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.UserProfileChangeRequest

class AuthRepository(private val auth: FirebaseAuth) {

    fun signIn(email: String, password: String, onResult: (Boolean, String?) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onResult(true, "SignIn succeed")
                } else {
                    onResult(false, handleFirebaseException(task.exception))
                }
            }
    }

    fun signUp(name: String, email: String, password: String, onResult: (Boolean, String?) -> Unit) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName(name)
                        .build()

                    user?.updateProfile(profileUpdates)?.addOnCompleteListener { profileTask ->
                        if (profileTask.isSuccessful) {
                            onResult(true, "SignUp succeed")
                        } else {
                            onResult(false,handleFirebaseException(profileTask.exception))
                        }
                    }
                } else {
                    onResult(false, task.exception?.message)
                }
            }
    }

    fun getCurrentUser() = auth.currentUser

    private fun handleFirebaseException(exception: Exception?): String {
        // password and email exception generate own message not the customized one
        return when (exception) {
            is FirebaseAuthInvalidCredentialsException -> "Bad credentials"
            is FirebaseAuthUserCollisionException -> "Email already in use"
            is FirebaseAuthWeakPasswordException -> "Password too weak"
            else -> "An unexpected error occurred :(\nPlease try again later"
        }
    }

}

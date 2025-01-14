package com.example.todolist.repository

import android.widget.Toast
import com.example.todolist.utils.AvatarUtils
import com.google.firebase.auth.ActionCodeSettings
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class AuthRepository(private val auth: FirebaseAuth) {
    private val database = FirebaseDatabase.getInstance("https://todolistv0-default-rtdb.europe-west1.firebasedatabase.app/")

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
        CoroutineScope(Dispatchers.IO).launch {
            val accountCreated = createUserAccount(email, password)
            if (!accountCreated) {
                withContext(Dispatchers.Main) { onResult(false, "Failed to create account") }
                return@launch
            }

            val profileUpdated = updateUserProfile(name)
            val defaultListAdded = addDefaultTaskList()

            if (profileUpdated && defaultListAdded) {
                withContext(Dispatchers.Main) { onResult(true, "Sign-up succeeded") }
            } else {
                withContext(Dispatchers.Main) { onResult(false, "Failed to complete sign-up process") }
            }
        }
    }

    private suspend fun createUserAccount(email: String, password: String): Boolean {
        return try {
            auth.createUserWithEmailAndPassword(email, password).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    private suspend fun updateUserProfile(name: String): Boolean {
        return try {
            val user = auth.currentUser ?: return false
            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(name)
                .build()
            user.updateProfile(profileUpdates).await()

            // Add avatar and bio
            val userDetails = hashMapOf(
                "bio" to "Update your bio...",
                "avatar" to AvatarUtils.getAvatarApiUrl(name)
            )

            val usersDetailsRef = database.getReference("usersDetails")
            usersDetailsRef.child(user.uid).setValue(userDetails).await()

            true
        } catch (e: Exception) {
            false
        }
    }

    private suspend fun addDefaultTaskList(): Boolean {
        return try {
            val userId = auth.currentUser?.uid ?: return false
            val userListsRef = database.getReference("lists").child(userId)
            userListsRef.push().setValue("My tasks").await()

            true
        } catch (e: Exception) {
            false
        }
    }

    fun resetPassword(email: String, onResult: (Boolean, String?) -> Unit) {
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onResult(true, null)
                } else {
                    onResult(false, task.exception.toString())
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
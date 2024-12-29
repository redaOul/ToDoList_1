package com.example.todolist.repository

import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.database

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
                            saveUserDetails(name) { success ->
                                if (success) {
                                    onResult(true, "SignUp succeeded")
                                } else {
                                    onResult(false, handleFirebaseException(exception = null))
                                }
                            }
                        } else {
                            onResult(false,handleFirebaseException(profileTask.exception))
                        }
                    }
                } else {
                    onResult(false, task.exception?.message)
                }
            }
    }

    public fun getCurrentUser() = auth.currentUser

    private fun handleFirebaseException(exception: Exception?): String {
        // password and email exception generate own message not the customized one
        return when (exception) {
            is FirebaseAuthInvalidCredentialsException -> "Bad credentials"
            is FirebaseAuthUserCollisionException -> "Email already in use"
            is FirebaseAuthWeakPasswordException -> "Password too weak"
            else -> "An unexpected error occurred :(\nPlease try again later"
        }
    }

    private fun formatUserNameForApi(fullName: String): String {
        val nameParts = if (!fullName.contains(" ")) {
            // If no spaces, attempt to split by uppercase letters (e.g., "RedaOul" → ["Reda", "Oul"])
            fullName.split(Regex("(?=[A-Z])")).filter { it.isNotBlank() }
        } else {
            // If spaces are present, split by them
            fullName.split(" ").filter { it.isNotBlank() }
        }

        // Capitalize each part and join with "+"
        return nameParts.joinToString("+") { it.trim().replaceFirstChar { it.uppercase() } }
    }

    private fun getAvatarApiUrl(userName: String): String {
        val formattedName = formatUserNameForApi(userName)
        return "https://ui-avatars.com/api/?background=random&name=$formattedName&rounded=true&bold=true"
    }

    private fun saveUserDetails(userName: String, callback: (Boolean) -> Unit) {

        val currentUserId = getCurrentUser()?.uid ?: "not authenticated"

        val userDetails = hashMapOf(
            "bio" to "Update your bio...",
            "avatar" to getAvatarApiUrl(userName)
        )

        val database = Firebase.database("https://todolistv0-default-rtdb.europe-west1.firebasedatabase.app/")
        val usersDetailsRef = database.getReference("usersDetails")

        usersDetailsRef.child(currentUserId).setValue(userDetails)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(true)
                } else {
                    callback(false)
                }
            }
    }


}

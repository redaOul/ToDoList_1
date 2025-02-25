package com.example.todolist.repository

import android.content.Intent
import android.util.Log
import com.example.todolist.activity.AuthActivity
import com.example.todolist.utils.AvatarUtils
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.tasks.await

class EditProfileRepository(private val auth: FirebaseAuth) {
    private val database = FirebaseDatabase.getInstance("https://todolistv0-default-rtdb.europe-west1.firebasedatabase.app/")
    private val user = auth.currentUser ?: throw SecurityException("User not authenticated")

    fun signOut(): Boolean {
        return try {
            auth.signOut()
            true
        } catch (e: Exception) {
            Log.e("Firebase", "Error signing out: ${e.message}")
            false
        }
    }

    fun getUserDetails(callback: (String?, String?) -> Unit) {
        val usersDetailsRef = database.getReference("usersDetails").child(user.uid)

        usersDetailsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val bio = dataSnapshot.child("bio").getValue(String::class.java)
                callback(user.displayName, bio)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w("Firebase", "Failed to read value.", error.toException())
            }
        })
    }

    suspend fun updateUserName(name: String): Boolean {
        return try {
            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(name)
                .build()
            user.updateProfile(profileUpdates).await()

            user.reload().await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun updateUserBio(bio: String): Boolean {
        return try {
            val user = auth.currentUser ?: return false
            val usersDetailsRef = database.getReference("usersDetails")

            usersDetailsRef.child(user.uid).child("bio").setValue(bio).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun checkOldPassword(oldPassword: String) : Boolean {
        return try {
            val credential = EmailAuthProvider.getCredential(user.email!!, oldPassword)
            user.reauthenticate(credential).await()
            true
        } catch (e: Exception){
            false
        }
    }

    suspend fun updatePassword(newPassword: String) : Boolean {
        return try {
            user.updatePassword(newPassword).await()
            true
        } catch (e: Exception){
            false
        }
    }

}
package com.example.todolist.repository

import android.util.Log
import com.example.todolist.model.Task
import com.example.todolist.model.TaskStatus
import com.example.todolist.model.UserList
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.time.LocalDate
import java.time.ZoneId

class HomeRepository (private val auth: FirebaseAuth) {
    private val database =
        FirebaseDatabase.getInstance("https://todolistv0-default-rtdb.europe-west1.firebasedatabase.app/")
    private val user = auth.currentUser ?: throw SecurityException("User not authenticated")

    fun getUserDetails(callback: (String?, String?, String?) -> Unit) {
        val usersDetailsRef = database.getReference("usersDetails").child(user.uid)

        usersDetailsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val bio = dataSnapshot.child("bio").getValue(String::class.java)
                val avatar = dataSnapshot.child("avatar").getValue(String::class.java)
                callback(user.displayName, bio, avatar)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w("Firebase", "Failed to read value.", error.toException())
            }
        })
    }

    fun getUserLists(callback: (List<UserList>) -> Unit) {
        val userListsRef = database.getReference("lists").child(user.uid)

        userListsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val lists = dataSnapshot.children.take(4).map { listSnapshot ->
                    val listKey = listSnapshot.key
                    val listName = listSnapshot.getValue(String::class.java)
                    UserList(listKey ?: "", listName ?: "")
                }
                callback(lists)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w("Firebase", "Failed to read value.", error.toException())
            }
        })
    }
}
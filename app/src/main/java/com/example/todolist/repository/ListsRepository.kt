package com.example.todolist.repository

import android.util.Log
import com.example.todolist.model.UserList
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ListsRepository (private val auth: FirebaseAuth){
    private val database = FirebaseDatabase.getInstance("https://todolistv0-default-rtdb.europe-west1.firebasedatabase.app/")
    private val user = auth.currentUser ?: throw SecurityException("User not authenticated")

    fun getAllLists(callback: (List<UserList>) -> Unit) {
        val userListsRef = database.getReference("lists").child(user.uid)

        userListsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val lists = mutableListOf<UserList>()
                for (listSnapshot in dataSnapshot.children) {
                    val listKey = listSnapshot.key
                    val listName = listSnapshot.getValue(String::class.java)
                    lists.add(UserList(listKey ?: "", listName ?: ""))
                }
                callback(lists)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w("Firebase", "Failed to read value.", error.toException())
            }
        })
    }

    fun addList(listName: String, onResult: (Boolean, String?) -> Unit) {
        val userListsRef = database.getReference("lists").child(user.uid)
        userListsRef.push().setValue(listName)
            .addOnSuccessListener {
//                    callback(listKey)
                onResult(true, "SignUp succeeded")
            }
            .addOnFailureListener {
                onResult(false,"Failed to add list")
            }
    }
}
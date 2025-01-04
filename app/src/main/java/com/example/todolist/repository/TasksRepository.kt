package com.example.todolist.repository

import android.util.Log
import com.example.todolist.model.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class TasksRepository (private val auth: FirebaseAuth){
    private val database = FirebaseDatabase.getInstance("https://todolistv0-default-rtdb.europe-west1.firebasedatabase.app/")
    private val user = auth.currentUser ?: throw SecurityException("User not authenticated")

    fun getListTasks(listId: String, callback: (List<Task>) -> Unit) {
        val userTasksRef = database.getReference("tasks")

        val query = userTasksRef.orderByChild("listId").equalTo(listId)

        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val tasks = mutableListOf<Task>()
                for (taskSnapshot in dataSnapshot.children) {
                    val task = taskSnapshot.getValue(Task::class.java)
                    val taskId = taskSnapshot.key
                    task?.let {
                        it.taskId = taskId ?: ""
                        tasks.add(it)
                    }
                }

                val currentTimestampInSeconds = System.currentTimeMillis() / 1000
                val upcomingTasks = tasks.filter { task ->
                    task.date!! >= currentTimestampInSeconds
                }.sortedBy { it.date }

                callback(upcomingTasks)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w("Firebase", "Failed to read value.", error.toException())
            }
        })
    }

    fun addTask(task: Task, onResult: (Boolean) -> Unit){
        val newTask = task.copy(userId = user.uid)
        val tasksRef = database.getReference("tasks")

        tasksRef.push().setValue(newTask)
            .addOnSuccessListener {
                onResult(true)
            }
            .addOnFailureListener {
                onResult(false)
            }
    }
}
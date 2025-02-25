package com.example.todolist.repository

import android.util.Log
import com.example.todolist.model.Task
import com.example.todolist.model.TaskStatus
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.time.LocalDate
import java.time.ZoneId


class TasksRepository (private val auth: FirebaseAuth){
    private val database = FirebaseDatabase.getInstance("https://todolistv0-default-rtdb.europe-west1.firebasedatabase.app/")
    private val user = auth.currentUser ?: throw SecurityException("User not authenticated")

    private val currentDate = LocalDate.now()
    private val currentDateTimestamp = currentDate.atStartOfDay(ZoneId.systemDefault()).toInstant().epochSecond

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

                        if (it.status == TaskStatus.UPCOMING && it.date!! < currentDateTimestamp) {
                            it.status = TaskStatus.OVERDUE
                        }

                        tasks.add(it)
                    }
                }

                val upcomingTasks = tasks.sortedWith( compareBy<Task> { it.status }.thenBy { it.date } )

                callback(upcomingTasks)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w("Firebase", "Failed to read value.", error.toException())
            }
        })
    }

    fun getUpComingTasks(callback: (List<Task>) -> Unit) {
        val userTasksRef = database.getReference("tasks")
        val query = userTasksRef.orderByChild("userId").equalTo(user.uid)

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

                val upcomingTasks = tasks.filter { task ->
                    task.date!! >= currentDateTimestamp && task.status == TaskStatus.UPCOMING
                }.sortedBy { it.date }

                callback(upcomingTasks)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w("Firebase", "Failed to read value.", error.toException())
            }
        })
    }

    fun addTask(task: Task, onResult: (Boolean) -> Unit){
        val newTask = task.copy(userId = user.uid, status = TaskStatus.UPCOMING)
        val tasksRef = database.getReference("tasks")

        tasksRef.push().setValue(newTask)
            .addOnSuccessListener {
                onResult(true)
            }
            .addOnFailureListener {
                onResult(false)
            }
    }

    fun completeTask(taskId: String) {
        val taskUpdates = mapOf(
            "status" to TaskStatus.COMPLETED,
            "completedAt" to System.currentTimeMillis()
        )
        val tasksRef = database.getReference("tasks")
        tasksRef.child(taskId).updateChildren(taskUpdates)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Task successfully updated
                    Log.d("TaskUpdate", "Task updated successfully.")
                } else {
                    // Handle the error
                    Log.d("TaskUpdate", "Failed to update task.")
                }
            }
    }

    fun deleteTaskFromDatabase(taskId: String) {
        val tasksRef = database.getReference("tasks")
        tasksRef.child(taskId).removeValue()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Task successfully deleted
                    Log.d("TaskDelete", "Task deleted successfully.")
                } else {
                    // Handle the error
                    Log.d("TaskDelete", "Failed to delete task.")
                }
            }
    }


}
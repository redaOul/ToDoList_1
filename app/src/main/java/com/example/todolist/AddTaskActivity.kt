package com.example.todolist

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.todolist.databinding.ActivityAddTaskBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import com.google.firebase.database.getValue

class AddTaskActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddTaskBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddTaskBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        readTaskFrom()
        saveTaskTo()
    }

    private fun saveTaskTo(){
        Log.d("Firebase", "Start adding task")
        binding.textView.text = "Start adding task"

        val taskName = "taskName1"
        val taskDescription = "taskDescription"
        val currentUserId = auth.currentUser?.uid ?: "not authenticated"

        val newTask = Task(
            name = taskName,
            description = taskDescription,
            userId = currentUserId
        )

        val database = Firebase.database("https://todolistv0-default-rtdb.europe-west1.firebasedatabase.app/")
        val tasksRef = database.getReference("tasks")

        Log.d("Firebase", "Object initiated")
        binding.textView.text = "Object initiated"

        tasksRef.push().setValue(newTask)
            .addOnSuccessListener {
                Log.d("Firebase", "Success adding task")
                binding.textView.text = "Task saved!"
            }
            .addOnFailureListener { exception ->
                Log.e("Firebase", "Error adding task", exception)
                binding.textView.text = "Task not saved!"
            }
    }

    private fun readTaskFrom(){
        val database = Firebase.database("https://todolistv0-default-rtdb.europe-west1.firebasedatabase.app/")
        val tasksRef = database.getReference("tasks")

        tasksRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val tasks = mutableListOf<Task>()
                for (taskSnapshot in dataSnapshot.children) {
                    val task = taskSnapshot.getValue<Task>()
                    task?.let { tasks.add(it) }
                }
                Log.d("Firebase", "Tasks: $tasks")
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w("Firebase", "Failed to read value.", error.toException())
            }
        })
    }
}

// [START read_message]
// Read from the database
//        myRef.addValueEventListener(object : ValueEventListener {
//            override fun onDataChange(dataSnapshot: DataSnapshot) {
//                // This method is called once with the initial value and again
//                // whenever data at this location is updated.
//                val value = dataSnapshot.getValue<String>()
//                Log.d(TAG, "Value is: $value")
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//                // Failed to read value
//                Log.w(TAG, "Failed to read value.", error.toException())
//            }
//        })
// [END read_message]
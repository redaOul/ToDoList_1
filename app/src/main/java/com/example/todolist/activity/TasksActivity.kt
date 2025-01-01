package com.example.todolist.activity

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.todolist.databinding.ActivityTasksBinding

class TasksActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTasksBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTasksBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val listId = intent.getStringExtra("ListId")
        Log.d("Lists", "$listId")
    }
}
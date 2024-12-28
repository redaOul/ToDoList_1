package com.example.todolist.model

data class Task(
    val taskId: String = "", // Unique ID for the task
    val name: String = "", // Required task name
    val description: String? = null, // Optional task description
    val userId: String = "" // ID of the user who created the task
)
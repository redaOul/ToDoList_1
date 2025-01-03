package com.example.todolist.model
import com.google.firebase.Timestamp

data class Task(
    var taskId: String? = null,
    val title: String = "", //
    val description: String? = null, //
    var date: Timestamp? = null, //
    val userId: String? = null, //
    val listId: String? = null //
)
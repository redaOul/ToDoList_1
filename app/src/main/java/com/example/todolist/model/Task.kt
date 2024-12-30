package com.example.todolist.model

data class Task(
    var taskId: String = "",
    val name: String = "",
    val description: String = "",
    var date: String = "Mon, 30 Dec",
    val userId: String = "",
    val listId: String = ""
)
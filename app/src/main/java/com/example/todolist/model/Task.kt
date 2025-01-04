package com.example.todolist.model

data class Task(
    var taskId: String? = null,
    val title: String = "", //
    val description: String? = null, //
    var date: Long? = null, //
    val userId: String? = null, //
    val listId: String? = null //
)
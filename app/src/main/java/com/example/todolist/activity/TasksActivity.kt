package com.example.todolist.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.todolist.databinding.ActivityTasksBinding
import com.example.todolist.databinding.ItemTaskCardBinding
import com.example.todolist.model.Task
import com.example.todolist.repository.TasksRepository
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TasksActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTasksBinding
    private lateinit var tasksRepository: TasksRepository
    private lateinit var listId: String
    private lateinit var listName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTasksBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tasksRepository = TasksRepository(FirebaseAuth.getInstance())

        val tmpListId = intent.getStringExtra("listId")
        val tmpListName = intent.getStringExtra("listName")

        if (tmpListId == null || tmpListName == null){
            throw Exception("ListId or ListName is null")
        }

        listId = tmpListId.toString()
        listName = tmpListName.toString()

        binding.listName.text = listName

        getTasks()
    }

    private fun getTasks(){
        tasksRepository.getListTasks(listId) { tasks ->
            updateTasksUI(tasks)
        }
    }

    private fun updateTasksUI(tasks: List<Task>) {
        val adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

            inner class TaskViewHolder(val binding: ItemTaskCardBinding) : RecyclerView.ViewHolder(binding.root)

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
                val binding = ItemTaskCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                return TaskViewHolder(binding)
            }

            override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
                val dateFormat = SimpleDateFormat("EEE, dd MMM", Locale.getDefault())
                val task = tasks[position]
                (holder as TaskViewHolder).binding.apply {
                    taskTitleText.text = task.title
                    descTask.text = task.description
                    val date = task.date
                    if (date != null){
                        val formattedDate = Date(date * 1000)
                        taskDateText.text = dateFormat.format(formattedDate)
                    }
                }
            }

            override fun getItemCount(): Int = tasks.size
        }

        binding.tasksRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.tasksRecyclerView.adapter = adapter
    }

}
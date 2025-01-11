package com.example.todolist.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.todolist.adapter.TaskAdapter
import com.example.todolist.databinding.ActivityTasksBinding
import com.example.todolist.model.Task
import com.example.todolist.model.TaskStatus
import com.example.todolist.repository.TasksRepository
import com.example.todolist.utils.TaskSwipeCallback
import com.google.firebase.auth.FirebaseAuth

class TasksActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTasksBinding
    private lateinit var tasksRepository: TasksRepository
    private lateinit var adapter: TaskAdapter
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

        binding.headerBar.setTitle(listName)

        getTasks()

        setupClickListeners()
    }

    private fun setupClickListeners(){
        binding.apply {
            headerBar.setOnBackClickListener {
                finish()
            }
        }
    }

    private fun getTasks(){
        tasksRepository.getListTasks(listId) { tasks ->
            updateTasksUI(tasks)
        }
    }

    private fun updateTasksUI(tasks: List<Task>) {
        val taskList = tasks.toMutableList()
        adapter = TaskAdapter(
            tasks = taskList,
            onTaskComplete = { position ->
                val task = taskList[position]
                tasksRepository.completeTask(task.taskId!!)
                task.status = TaskStatus.COMPLETED
                adapter.notifyItemChanged(position)
            },
            onTaskDelete = { position ->
                val task = taskList[position]
                tasksRepository.deleteTaskFromDatabase(task.taskId!!)
                taskList.removeAt(position)
                adapter.notifyItemRemoved(position)
            }
        )

        binding.tasksRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.tasksRecyclerView.adapter = adapter

        val itemTouchHelper = ItemTouchHelper(TaskSwipeCallback(
            onSwipeLeft = { position ->
                val task = adapter.getTaskAt(position)
                if (task.status != TaskStatus.COMPLETED) {
                    tasksRepository.completeTask(task.taskId!!)
                    task.status = TaskStatus.COMPLETED
                    adapter.notifyItemChanged(position)
                }
            },
            onSwipeRight = { position ->
                val task = adapter.getTaskAt(position)
                tasksRepository.deleteTaskFromDatabase(task.taskId!!)
                taskList.removeAt(position)
                adapter.notifyItemRemoved(position)
            },
            taskList = taskList
        ))
        itemTouchHelper.attachToRecyclerView(binding.tasksRecyclerView)
    }

}
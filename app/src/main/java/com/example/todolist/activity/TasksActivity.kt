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

        binding.listName.text = listName

        getTasks()
    }

    private fun getTasks(){
        tasksRepository.getListTasks(listId) { tasks ->
            updateTasksUI(tasks)
        }
    }

    private fun updateTasksUI(tasks: List<Task>) {
//        val adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
//
//            inner class TaskViewHolder(val binding: ItemTaskCardBinding) : RecyclerView.ViewHolder(binding.root)
//
//            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
//                val binding = ItemTaskCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
//                return TaskViewHolder(binding)
//            }
//
//            override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
//                val dateFormat = SimpleDateFormat("EEE, dd MMM", Locale.getDefault())
//                val task = tasks[position]
//                (holder as TaskViewHolder).binding.apply {
//                    taskTitleText.text = task.title
//                    descTask.text = task.description
//                    val date = task.date
//                    if (date != null){
//                        val formattedDate = Date(date * 1000)
//                        taskDateText.text = dateFormat.format(formattedDate)
//                    }
//                }
//            }
//
//            override fun getItemCount(): Int = tasks.size
//        }
//
//        binding.tasksRecyclerView.layoutManager = LinearLayoutManager(this)
//        binding.tasksRecyclerView.adapter = adapter

        val adapter = TaskAdapter(
            tasks = tasks.toMutableList(),
            onTaskComplete = { position ->
                val task = tasks[position]
                task.status = TaskStatus.COMPLETED
                task.completedAt = System.currentTimeMillis() // Set the completed time
                adapter.updateTask(position) // Update the task
                tasksRepository.completeTask(task.taskId!!) // Update the task in the database
            },
            onTaskDelete = { position ->
                val task = tasks[position]
                adapter.removeTaskAt(position) // Remove the task from the adapter
                tasksRepository.deleteTaskFromDatabase(task.taskId!!) // Delete the task from the database
            }
        )

        binding.tasksRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.tasksRecyclerView.adapter = adapter

        val itemTouchHelper = ItemTouchHelper(
            TaskSwipeCallback(
                onSwipeLeft = { position ->
                    val task = adapter.getTaskAt(position)
                    val taskId = task.taskId ?: return@TaskSwipeCallback
                    task.status = TaskStatus.COMPLETED
                    task.completedAt = System.currentTimeMillis()
                    adapter.updateTask(position)
                    tasksRepository.completeTask(taskId)
                },
                onSwipeRight = { position ->
                    val task = adapter.getTaskAt(position)
                    val taskId = task.taskId ?: return@TaskSwipeCallback
                    adapter.removeTaskAt(position)
                    tasksRepository.deleteTaskFromDatabase(taskId)
                },
                taskList = tasks
            )
        )
        itemTouchHelper.attachToRecyclerView(binding.tasksRecyclerView)
    }

}
package com.example.todolist.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.todolist.adapter.ListAdapter
import com.example.todolist.adapter.TaskAdapter
import com.example.todolist.databinding.ActivityHomeBinding
import com.example.todolist.model.Task
import com.example.todolist.model.TaskStatus
import com.example.todolist.model.UserList
import com.example.todolist.repository.HomeRepository
import com.example.todolist.repository.ListsRepository
import com.example.todolist.repository.TasksRepository
import com.example.todolist.utils.AvatarUtils
import com.example.todolist.utils.TaskSwipeCallback
import com.google.firebase.auth.FirebaseAuth

class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding
    private lateinit var homeRepository: HomeRepository
    private lateinit var listsRepository: ListsRepository
    private lateinit var tasksRepository: TasksRepository
    private lateinit var adapter: TaskAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        homeRepository = HomeRepository(FirebaseAuth.getInstance())
        listsRepository = ListsRepository(FirebaseAuth.getInstance())
        tasksRepository = TasksRepository(FirebaseAuth.getInstance())

        loadProfile()
        getLists()
        getTasks()

        setupClickListeners()
    }

    private fun setupClickListeners(){
        binding.apply {
            // Set up edit profile button click
            editProfile.setOnClickListener {
                startActivity(Intent(this@HomeActivity, EditProfileActivity::class.java))
            }

            // set up see all lists button click
            seeAllButton.setOnClickListener {
                startActivity(Intent(this@HomeActivity, ListsActivity::class.java))
            }

            // set up add task button click
            addTaskButton.setOnClickListener {
                startActivity(Intent(this@HomeActivity, AddTaskActivity::class.java))
            }
        }
    }

//    ========================================
//    first section logic
//    ========================================
    private fun loadProfile() {
        homeRepository.getUserDetails { name, bio, avatar ->
            updateProfileUI(name, bio)
            AvatarUtils.loadAvatarIntoImageView(avatar, binding.profileImage)
        }
    }

    private fun updateProfileUI(userNameParam: String?, userBioParam: String?) {
        binding.apply {
            userName.text = userNameParam
            userBio.text = userBioParam
        }
    }

//    ========================================
//    second section logic
//    ========================================
    private fun getLists() {
        listsRepository.getUserLists { lists ->
            updateListsUI(lists)
        }
    }

    private fun updateListsUI(lists: List<UserList>) {
        val adapter = ListAdapter(lists = lists,
            onItemClick = { clickedList ->
                redirectToTasksList(clickedList.id, clickedList.name)
            },
            onDeleteList = null
        )
        binding.listsRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.listsRecyclerView.adapter = adapter
    }

    private fun redirectToTasksList(id: String, listName: String){
        val intent = Intent(this, TasksActivity::class.java)
        intent.putExtra("listId", id)
        intent.putExtra("listName", listName)
        startActivity(intent)
    }

//    ========================================
//    third section logic
//    ========================================
    private fun getTasks(){
        tasksRepository.getUpComingTasks { tasks ->
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
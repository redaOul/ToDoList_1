package com.example.todolist.activity

//import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.todolist.R
import com.example.todolist.databinding.ActivityHomeBinding
import com.example.todolist.databinding.ItemTaskCardBinding
import com.example.todolist.model.Task
import com.example.todolist.repository.HomeRepository
import com.example.todolist.utils.AvatarUtils
import com.google.firebase.auth.FirebaseAuth

class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding
    private lateinit var taskRepository: HomeRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Auth
        taskRepository = HomeRepository(FirebaseAuth.getInstance())

//        auth = Firebase.auth
//        val user = auth.currentUser ?: return

        loadUserProfile()
        getUserLists()
        getUserTasks()

        // Set up edit profile button click
        binding.editProfile.setOnClickListener {
//            startActivity(Intent(this, EditProfileActivity::class.java))
        }

        // set up see all lists button click
        binding.seeAllButton.setOnClickListener {
//            startActivity(Intent(this, AllListsActivity::class.java))
        }

        // set up add task button click
        binding.addTaskButton.setOnClickListener {
//            startActivity(Intent(this, AddTaskActivity::class.java))
        }
    }

//    ========================================
//    first section logic
//    ========================================
    private fun loadUserProfile() {
        taskRepository.getUserDetails { name, bio, avatar ->
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
    private fun getUserLists() {
        taskRepository.getUserLists { lists ->
            updateListsUI(lists)
        }
    }

    private fun updateListsUI(lists: List<Pair<String, String>>) {
        binding.ListsContainer.removeAllViews()

        lists.forEach { (listKey, listName) ->
            val cardView = createListCard(listKey, listName)
            binding.ListsContainer.addView(cardView)
        }
    }

    private fun createListCard(listKey: String, listName: String): CardView {
        val cardView = CardView(this).apply {
            setCardBackgroundColor(ContextCompat.getColor(context, R.color.purple))
            cardElevation = 2f
            radius = 15f
        }

        val textView = TextView(this).apply {
            text = listName
            setTextColor(Color.WHITE)
            setPadding(16, 16, 16, 16)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
            setTypeface(null, Typeface.BOLD)
        }

        cardView.addView(textView)

        cardView.setOnClickListener {
//        val intent = Intent(this@HomeActivity, TasksActivity::class.java)
//        intent.putExtra("LIST_ID", listKey)
//        intent.putExtra("LIST_NAME", listName)
//        startActivity(intent)
            Log.e("HomeActivity", "List clicked: $listName | $listKey")
        }

        return cardView

//            cardView.layoutParams = LinearLayout.LayoutParams(
//                LinearLayout.LayoutParams.MATCH_PARENT,
//                LinearLayout.LayoutParams.WRAP_CONTENT
//            )
//            cardView.addView(textView)
//
//            cardView.setOnClickListener {
//                redirectMethod(listKey, listName)
//            }
//
//            binding.ListsContainer.addView(cardView)
//        }
    }

//    ========================================
//    third section logic
//    ========================================
    private fun getUserTasks() {
        taskRepository.getUserTasks { tasks ->
            updateUpComingTasksUI(tasks)
        }
    }

    private fun updateUpComingTasksUI(tasks: List<Task>) {
        val adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

            inner class TaskViewHolder(val binding: ItemTaskCardBinding) : RecyclerView.ViewHolder(binding.root)

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
                val binding = ItemTaskCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                return TaskViewHolder(binding)
            }

            override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
                val task = tasks[position]
                (holder as TaskViewHolder).binding.apply {
                    taskTitleText.text = task.name
                    taskDateText.text = task.date
                    descTask.text = task.description
                }
            }

            override fun getItemCount(): Int = tasks.size
        }

        binding.tasksRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.tasksRecyclerView.adapter = adapter
    }
}
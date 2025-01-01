package com.example.todolist.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.todolist.databinding.ActivityListsBinding
import com.example.todolist.databinding.ItemListCardBinding
import com.example.todolist.model.UserList
import com.example.todolist.repository.ListsRepository
import com.example.todolist.utils.ValidationUtils
import com.google.firebase.auth.FirebaseAuth

class ListsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityListsBinding
    private lateinit var listsRepository: ListsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityListsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        listsRepository = ListsRepository(FirebaseAuth.getInstance())

        getUserLists()

        setupClickListeners()
    }

    private fun setupClickListeners(){
        binding.apply {
            backButton.setOnClickListener {
                finish()
            }

            addButton.setOnClickListener {
                val name = editTextListName.text.toString()

                val (isValid, error) = ValidationUtils.validateAddListInput(name)
                if (isValid) {
                    performAddList(name)
                } else {
                    // Handle invalid input via alert message
                    Log.d("Lists", "$error")
                }
            }
        }
    }

    private fun performAddList(name: String) {
        listsRepository.addList(name) { success, message ->
            if (success) {
                // success alert message
//                    binding.result.text = error
                Log.d("Lists", "List added successfully")
            } else {
                // failed alert message
                Log.d("Lists", "$message")
            }
        }
    }

    private fun getUserLists() {
        listsRepository.getAllLists { lists ->
            updateListsUI(lists)
        }
    }

    private fun updateListsUI(lists: List<UserList>) {
        val adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

            inner class ListViewHolder(val binding: ItemListCardBinding) : RecyclerView.ViewHolder(binding.root) {
                init {
                    // Set click listener in the ViewHolder initialization
                    itemView.setOnClickListener {
                        val position = adapterPosition
                        if (position != RecyclerView.NO_POSITION) {
                            val clickedList = lists[position]
                            redirectToTasksList(clickedList.id)
                        }
                    }
                }
            }
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
                val binding = ItemListCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                return ListViewHolder(binding)
            }

            override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
                val list = lists[position]
                (holder as ListViewHolder).binding.apply {
                    taskName.text = list.name
                }
            }

            override fun getItemCount(): Int = lists.size
        }

        binding.listsRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.listsRecyclerView.adapter = adapter
    }

    private fun redirectToTasksList(id: String){
        val intent = Intent(this, TasksActivity::class.java)
        intent.putExtra("ListId", id)
        startActivity(intent)
    }
}
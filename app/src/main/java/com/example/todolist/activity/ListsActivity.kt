package com.example.todolist.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.todolist.R
import com.example.todolist.adapter.ListAdapter
import com.example.todolist.databinding.ActivityListsBinding
import com.example.todolist.model.UserList
import com.example.todolist.repository.ListsRepository
import com.example.todolist.utils.ValidationUtils
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth

class ListsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityListsBinding
    private lateinit var listsRepository: ListsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityListsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.headerBar.setTitle("Lists")

        listsRepository = ListsRepository(FirebaseAuth.getInstance())

        getUserLists()

        setupClickListeners()
    }

    private fun setupClickListeners(){
        binding.apply {
            headerBar.setOnBackClickListener {
                finish()
            }

            addButton.setOnClickListener {
                val name = editTextListName.text.toString()

                val (isValid, error) = ValidationUtils.validateAddListInput(name)
                if (isValid) {
                    performAddList(name)
                } else {
                    showSnackBar("$error", false)
                }
            }
        }
    }

    private fun performAddList(name: String) {
        listsRepository.addList(name) { success, message ->
            if (success) {
                showSnackBar("$message", )
            } else {
                showSnackBar("$message", false)
            }
        }
    }

    private fun getUserLists() {
        listsRepository.getAllLists { lists ->
            updateListsUI(lists)
        }
    }

    private fun updateListsUI(lists: List<UserList>) {
        val adapter = ListAdapter(
            lists = lists,
            onItemClick = { clickedList ->
                redirectToTasksList(clickedList.id, clickedList.name)
            },
            onDeleteList = { listId ->
                listsRepository.deleteListAndItsTasks(listId) { success, message ->
                    if (success) {
                        showSnackBar(message)
                    } else {
                        showSnackBar(message, false)
                    }
                }
            }
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

    private fun showSnackBar(message: String, state: Boolean = true) {
        val textColor: Int
        val backgroundColor: Int

        if (state){
            textColor = ContextCompat.getColor(this, R.color.medium_green)
            backgroundColor = ContextCompat.getColor(this, R.color.mint_cream)
        } else {
            textColor = ContextCompat.getColor(this, R.color.deep_red)
            backgroundColor = ContextCompat.getColor(this, R.color.rose_white)
        }

        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).apply {
            setTextColor(textColor)
            setBackgroundTint(backgroundColor)
            show()
        }
    }
}
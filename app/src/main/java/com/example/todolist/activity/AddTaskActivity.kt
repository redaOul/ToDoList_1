package com.example.todolist.activity

import android.os.Bundle
import android.util.Log
import java.util.Calendar
import android.app.DatePickerDialog
import android.widget.ArrayAdapter
import java.text.SimpleDateFormat
import java.util.Locale
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.todolist.R
import com.example.todolist.databinding.ActivityAddTaskBinding
import com.example.todolist.model.Task
import com.example.todolist.model.UserList
import com.example.todolist.repository.ListsRepository
import com.example.todolist.repository.TasksRepository
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth


class AddTaskActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddTaskBinding
    private lateinit var listsRepository: ListsRepository
    private lateinit var tasksRepository: TasksRepository
    private var selectedListId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddTaskBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.headerBar.setTitle("Add a task")

        listsRepository = ListsRepository(FirebaseAuth.getInstance())
        tasksRepository = TasksRepository(FirebaseAuth.getInstance())

        setupDatePicker()
        setupDropdown()
        setupBackButton()
        setupSaveButton()
    }

    private fun setupDatePicker() {
        binding.deadlineInput.setOnClickListener {
            val calendar = Calendar.getInstance()
            val datePickerDialog = DatePickerDialog(
                this,
                { _, year, month, day ->
                    calendar.set(year, month, day)
                    binding.deadlineInput.setText(
                        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                            .format(calendar.time)
                    )
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )

            datePickerDialog.datePicker.minDate = System.currentTimeMillis()
            datePickerDialog.show()
        }

        binding.deadlineLayout.setEndIconOnClickListener {
            binding.deadlineInput.performClick()
        }
    }

    private fun setupDropdown() {
        listsRepository.getAllLists { lists ->
            setupDropdownUI(lists)
        }
    }

    private fun setupDropdownUI(userLists: List<UserList>) {
        val adapter = ArrayAdapter(
            this,
            R.layout.dropdown_item_list,
            userLists.map { it.name }
        )
        binding.listDropdown.setAdapter(adapter)

        binding.listDropdown.setOnItemClickListener { _, _, position, _ ->
            selectedListId = userLists[position].id
        }

        // set "My tasks" as a default list
        val defaultIndex = userLists.indexOfFirst { it.name == "My tasks" }
        if (defaultIndex != -1) {
            binding.listDropdown.setText(userLists[defaultIndex].name, false)
            selectedListId = userLists[defaultIndex].id
        }
    }

    private fun setupBackButton() {
        binding.apply {
            headerBar.setOnBackClickListener {
                finish()
            }
        }
    }

    private fun setupSaveButton() {
        binding.saveButton.setOnClickListener {

            // Validate and save task
            val title = binding.taskTitleInput.text.toString()
            val deadline = binding.deadlineInput.text.toString()
            val listId = selectedListId
            val description = binding.descriptionInput.text.toString()

            if (title.isEmpty()) {
                binding.taskTitleLayout.error = "Task title is required"
                return@setOnClickListener
            } else {
                binding.taskTitleLayout.error = null
            }

            if (deadline.isEmpty()) {
                binding.deadlineLayout.error = "Deadline is required"
                return@setOnClickListener
            } else {
                binding.deadlineLayout.error = null
            }

            try {
                val deadlineDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(deadline)
                val deadlineTimestamp = deadlineDate.time / 1000

                val task = Task(
                    title = title,
                    date = deadlineTimestamp,
                    listId = listId ?: "",
                    description = description
                )

                saveTask(task)
            } catch (e: Exception) {
                Log.e("AddTask", "Error parsing deadline: ${e.message}")
            }
        }
    }

    private fun saveTask(task: Task) {
        tasksRepository.addTask(task) { success ->
            if (success) {
                prepareActivityUIAfterAddingTask()
            } else {
                showSnackBar("Could not add your task", false)
            }
        }
    }

    private fun prepareActivityUIAfterAddingTask(){
        // set the fields to the default state
        binding.taskTitleInput.setText("")
        binding.deadlineInput.setText("")
        binding.descriptionInput.setText("")


        showSnackBar("Task added successfully" )
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
package com.example.todolist.activity

import android.os.Bundle
import android.util.Log
import java.util.Calendar
import android.app.DatePickerDialog
import android.widget.ArrayAdapter
import java.text.SimpleDateFormat
import java.util.Locale
import androidx.appcompat.app.AppCompatActivity
import com.example.todolist.R
import com.example.todolist.databinding.ActivityAddTaskBinding
import com.example.todolist.model.Task
import com.example.todolist.model.UserList
import com.example.todolist.repository.ListsRepository
import com.example.todolist.repository.TasksRepository
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

        binding.textFieldDeadline.setEndIconOnClickListener {
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

        val defaultIndex = userLists.indexOfFirst { it.name == "My tasks" }
        if (defaultIndex != -1) {
            binding.listDropdown.setText(userLists[defaultIndex].name, false) // Show "My tasks"
            selectedListId = userLists[defaultIndex].id // Store the ID of "My tasks" as selectedListId
        }
    }

    private fun setupBackButton() {
        binding.backButton.setOnClickListener {
            finish()
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
                binding.taskTitleLayout.error = "Task title is required" // Display error
                return@setOnClickListener
            } else {
                binding.taskTitleLayout.error = null // Clear the error if valid
            }

            try {
                val deadlineDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(deadline)

                if (deadlineDate != null) {
                    val deadlineTimestamp = deadlineDate.time / 1000

                    val task = Task(
                        title = title,
                        date = deadlineTimestamp,
                        listId = listId ?: "",
                        description = description
                    )

                    saveTask(task)
                } else {
                    Log.e("AddTask", "Invalid deadline date format")
                }
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
                // alert failed message
                Log.d("Add task", "Failed to add task")
            }
        }
    }

    private fun prepareActivityUIAfterAddingTask(){
        // set the fields to the default state
        binding.taskTitleInput.text?.clear()
        binding.deadlineInput.text?.clear()
        binding.descriptionInput.text?.clear()

        // alert the success of operation
    }
}
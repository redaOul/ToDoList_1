package com.example.todolist.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.todolist.R
import com.example.todolist.databinding.ItemTaskCardBinding
import com.example.todolist.model.Task
import com.example.todolist.model.TaskStatus
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// The TaskAdapter class is now responsible for managing the tasks and rendering them on the RecyclerView.
class TaskAdapter(
    private val tasks: MutableList<Task>,
    private val onTaskComplete: (Int) -> Unit,
    private val onTaskDelete: (Int) -> Unit
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    inner class TaskViewHolder(val binding: ItemTaskCardBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding = ItemTaskCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TaskViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val dateFormat = SimpleDateFormat("EEE, dd MMM", Locale.getDefault())
        val task = tasks[position]

        holder.binding.apply {
            taskTitleText.text = task.title
            descTask.text = task.description
            val date = task.date
            if (date != null){
                val formattedDate = Date(date * 1000)
                taskDateText.text = dateFormat.format(formattedDate)
            }

            // Set task styles based on status
            when (task.status) {
                TaskStatus.COMPLETED -> {
                    taskLayout.setBackgroundColor(ContextCompat.getColor(root.context, R.color.mint_cream))
                    taskTitleText.setTextColor(ContextCompat.getColor(root.context, R.color.forest_green))
                    taskDateText.setTextColor(ContextCompat.getColor(root.context, R.color.medium_green))
                    descCircle.setColorFilter(ContextCompat.getColor(root.context, R.color.light_green))
                }
                TaskStatus.UPCOMING -> {
                    taskLayout.setBackgroundColor(ContextCompat.getColor(root.context, R.color.white))
                    taskTitleText.setTextColor(ContextCompat.getColor(root.context, R.color.deep_blue))
                    taskDateText.setTextColor(ContextCompat.getColor(root.context, R.color.gray))
                    descCircle.setColorFilter(ContextCompat.getColor(root.context, R.color.sky_blue))
                }
                else -> {
                    taskLayout.setBackgroundColor(ContextCompat.getColor(root.context, R.color.rose_white))
                    taskTitleText.setTextColor(ContextCompat.getColorStateList(root.context, R.color.deep_red))
                    taskDateText.setTextColor(ContextCompat.getColorStateList(root.context, R.color.brick_red))
                    descCircle.setColorFilter(ContextCompat.getColor(root.context, R.color.soft_red))
                }
            }
        }
    }

    override fun getItemCount(): Int = tasks.size

    fun getTaskAt(position: Int): Task = tasks[position]
}

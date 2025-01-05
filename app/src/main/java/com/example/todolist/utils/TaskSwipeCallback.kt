package com.example.todolist.utils

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.todolist.adapter.TaskAdapter
import com.example.todolist.model.Task
import com.example.todolist.model.TaskStatus

class TaskSwipeCallback (
        private val onSwipeLeft: (Int) -> Unit,
        private val onSwipeRight: (Int) -> Unit,
        private val taskList: List<Task>
    ) : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {

    override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
        val task = taskList[viewHolder.adapterPosition]
        val swipeFlags = if (task.status == TaskStatus.COMPLETED) {
            // If the task is completed, only allow swipe-right (delete)
            ItemTouchHelper.RIGHT
        } else {
            // Allow both swipe-left (to complete) and swipe-right (to delete)
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        }
        return makeMovementFlags(0, swipeFlags)
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        return false
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        val task = taskList[viewHolder.adapterPosition]
        when (direction) {
            ItemTouchHelper.LEFT -> {
                if (task.status != TaskStatus.COMPLETED) {
                    // Call the onSwipeLeft action only if task is not completed
                } else {
                    // If the task is completed, do not allow swipe left, restore the task
                    viewHolder.itemView.isEnabled = false
                }
            }
            ItemTouchHelper.RIGHT -> {
                // Call the onSwipeRight action to delete the task
                onSwipeRight(viewHolder.adapterPosition)
            }
        }
    }

    override fun isItemViewSwipeEnabled(): Boolean {
        return true
    }

    override fun getSwipeDirs(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
        ): Int {
        // Check if the task is completed; disable swipe if it is
        val adapter = recyclerView.adapter as TaskAdapter
        val task = adapter.getTaskAt(viewHolder.adapterPosition)
        return if (task.status == TaskStatus.COMPLETED) 0 else super.getSwipeDirs(recyclerView, viewHolder)
    }
}
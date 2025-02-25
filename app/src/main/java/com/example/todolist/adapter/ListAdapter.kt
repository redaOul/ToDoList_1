package com.example.todolist.adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.todolist.databinding.ItemListCardBinding
import com.example.todolist.model.UserList
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class ListAdapter (
    private val lists: List<UserList>,
    private val onItemClick: (UserList) -> Unit,
    private val onDeleteList: ((String) -> Unit)? = null
) : RecyclerView.Adapter<ListAdapter.ListViewHolder>() {

    val colorPalette = listOf(
        Color.parseColor("#7FD8BE"),
        Color.parseColor("#FF5733"),
        Color.parseColor("#C8A2C8"),
        Color.parseColor("#C70039"),
        Color.parseColor("#00A676"),
        Color.parseColor("#8A2BE2"),
        Color.parseColor("#C2B97F"),
    )

    inner class ListViewHolder(val binding: ItemListCardBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val clickedList = lists[position]
                    onItemClick(clickedList)
                }
            }

            itemView.setOnLongClickListener {
                val position = adapterPosition
                if (onDeleteList != null && position != RecyclerView.NO_POSITION) {
                    val listToDelete = lists[position]
                    showDeleteConfirmationDialog(itemView.context, listToDelete)
                }
                true // Indicate the event is handled
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {
        val binding = ItemListCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ListViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        val list = lists[position]
        holder.binding.apply {
            listName.text = list.name
            listLayout.setBackgroundColor(getColorForItem(position))
        }
    }

    override fun getItemCount(): Int = lists.size

    private fun getColorForItem(position: Int): Int {
        val colorIndex = position % colorPalette.size
        return colorPalette[colorIndex]
    }

    private fun showDeleteConfirmationDialog(context: Context, list: UserList) {
        MaterialAlertDialogBuilder(context)
            .setTitle("Delete List")
            .setMessage("Are you sure you want to delete the list \"${list.name}\"?")
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton("Delete") { _, _ ->
                onDeleteList?.invoke(list.id)
            }
            .show()
    }
}
package com.example.todolist.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.todolist.databinding.ViewTopAppBarBinding

class TopAppBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val binding: ViewTopAppBarBinding =
        ViewTopAppBarBinding.inflate(LayoutInflater.from(context), this, true)

    fun setTitle(title: String) {
        binding.tvTitle.text = title
    }

    fun setOnBackClickListener(listener: () -> Unit) {
        binding.btnBack.setOnClickListener { listener() }
    }
}

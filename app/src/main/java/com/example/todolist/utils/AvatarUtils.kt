package com.example.todolist.utils

import android.widget.ImageView
import com.bumptech.glide.Glide

object AvatarUtils {
    fun loadAvatarIntoImageView(avatarURL: String?, imageView: ImageView) {
        Glide.with(imageView.context)
            .load(avatarURL)
            .into(imageView)
    }

    fun getAvatarApiUrl(userName: String): String {
        val formattedName = formatUserNameForApi(userName)
        return "https://ui-avatars.com/api/?background=random&name=$formattedName&rounded=true&bold=true"
    }

    private fun formatUserNameForApi(fullName: String): String {
        val nameParts = if (!fullName.contains(" ")) {
            fullName.split(Regex("(?=[A-Z])")).filter { it.isNotBlank() }
        } else {
            fullName.split(" ").filter { it.isNotBlank() }
        }

        return nameParts.joinToString("+") { it.trim().replaceFirstChar { it.uppercase() } }
    }
}
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
        val formattedName = ValidationUtils.formatUserNameForApi(userName)
        return "https://ui-avatars.com/api/?background=random&name=$formattedName&rounded=true&bold=true"
    }
}
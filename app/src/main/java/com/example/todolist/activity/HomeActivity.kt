package com.example.todolist.activity

import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.todolist.databinding.ActivityHomeBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database

class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth
        val user = auth.currentUser

        loadUserProfile(user)

        // Set up edit profile button click
        binding.editProfile.setOnClickListener {
//            startActivity(Intent(this, EditProfileActivity::class.java))
        }
    }

    private fun loadUserProfile(user: FirebaseUser?) {
        user?.let {
            getUserDetails(it) { bio, avatar ->
                updateProfileUI(it.displayName, bio)
                loadAvatarIntoImageView(avatar, binding.profileImage)
            }
        }
    }

    private fun updateProfileUI(userNameParam: String?, userBioParam: String?) {
        binding.apply {
            userName.text = userNameParam
            userBio.text = userBioParam
        }
    }

    private fun loadAvatarIntoImageView(avatarURL: String?, imageView: ImageView) {
        Glide.with(imageView.context)
            .load(avatarURL)
            .into(imageView)
    }

    private fun getUserDetails(uid: FirebaseUser, callback: (String?, String?) -> Unit){

        val database = Firebase.database("https://todolistv0-default-rtdb.europe-west1.firebasedatabase.app/")
        val usersDetailsRef = database.getReference("usersDetails").child(uid.uid)

        usersDetailsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val bio = dataSnapshot.child("bio").getValue(String::class.java)
                val avatar = dataSnapshot.child("avatar").getValue(String::class.java)
                callback(bio, avatar)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w("Firebase", "Failed to read value.", error.toException())
            }
        })
    }
}
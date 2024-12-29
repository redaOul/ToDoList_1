package com.example.todolist.activity

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.todolist.R
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
        val user = auth.currentUser ?: return

        loadUserProfile(user)
        Log.e("Lists", "================ Start ==========")
        getUserLists(user)
        Log.e("Lists", "================ Finish ==========")

        // Set up edit profile button click
        binding.editProfile.setOnClickListener {
//            startActivity(Intent(this, EditProfileActivity::class.java))
        }

        // set up see all lists button click
        binding.seeAllButton.setOnClickListener {
//            startActivity(Intent(this, AllListsActivity::class.java))
        }
    }

//    ========================================
//    first section logic
//    ========================================
    private fun loadUserProfile(user: FirebaseUser) {

        Log.e("User Profile", "generation user profile started")
        user.let {
            getUserDetails(it) { bio, avatar ->
                updateProfileUI(it.displayName, bio)
                loadAvatarIntoImageView(avatar, binding.profileImage)
            }
        }
        Log.e("User Profile", "generation user profile done")
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

//    ========================================
//    second section logic
//    ========================================
    private fun getUserLists(user: FirebaseUser){
        // TODO: get only 4 lists
        Log.e("Lists", "start getting the lists")

        Log.e("Lists", "User ID: ${user.uid}")
        val database = Firebase.database("https://todolistv0-default-rtdb.europe-west1.firebasedatabase.app/")
        val userListsRef = database.getReference("lists").child(user.uid)

        userListsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val lists = dataSnapshot.children.mapNotNull { listSnapshot ->
                    val listKey = listSnapshot.key
                    val listName = listSnapshot.child("name").getValue(String::class.java)
                    Log.e("Lists", "List ID: ${listKey} | List name: ${listName}")
                    if (listKey != null && listName != null) Pair(listKey, listName)
                    else null
                }
                updateListsUI(lists)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w("Firebase", "Failed to read value.", error.toException())
            }
        })
    }

    private fun updateListsUI(lists: List<Pair<String, String>>) {
        Log.e("Lists", "start updating the UI")
        binding.ListsContainer.removeAllViews()

        lists.forEach { (listKey, listName) ->
            val cardView = CardView(this@HomeActivity)
            val textView = TextView(this@HomeActivity).apply {
                text = listName
                setTextColor(Color.WHITE)
                setPadding(16, 16, 16, 16)
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
                setTypeface(null, Typeface.BOLD)
            }

            cardView.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            cardView.setCardBackgroundColor(ContextCompat.getColor(this@HomeActivity, R.color.purple))
            cardView.cardElevation = 2f
            cardView.radius = 15f
            cardView.addView(textView)

            cardView.setOnClickListener {
                redirectMethod(listKey, listName)
            }

            binding.ListsContainer.addView(cardView)
            Log.e("Lists", "Finish updating the UI")
        }
    }

    private fun redirectMethod(listKey: String, listName: String) {
//        val intent = Intent(this@HomeActivity, TasksActivity::class.java)
//        intent.putExtra("LIST_ID", listKey)
//        intent.putExtra("LIST_NAME", listName)
//        startActivity(intent)
        // log list clicked
        Log.e("Home Activity", "List clicked: $listName")
    }
}
package com.example.todolist.activity

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.todolist.R
import com.example.todolist.databinding.ActivityEditProfileBinding
import com.example.todolist.repository.EditProfileRepository
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class EditProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditProfileBinding
    private lateinit var editProfileRepository: EditProfileRepository

    // Store original values to compare against
    private var originalUsername = ""
    private var originalBio = ""
    private var isPasswordModified = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.headerBar.setTitle("Settings")

        editProfileRepository = EditProfileRepository(FirebaseAuth.getInstance())

        prepareFields()
        setupTextWatchers()
        setupClickListeners()
    }

    private fun setupClickListeners(){
        binding.apply {
            headerBar.setOnBackClickListener {
                finish()
            }

            saveButton.setOnClickListener {
                saveProfile()
            }

            signOutButton.setOnClickListener {
                performSignOut()
            }
        }
    }

    private fun prepareFields(){
        editProfileRepository.getUserDetails { name, bio ->
            originalUsername = name.toString()
            originalBio = bio.toString()
            updateFieldsUI(name, bio)
        }
        updateSaveButtonState(false)
    }

    private fun updateFieldsUI(name: String?, bio: String?){
        binding.apply {
            usernameInput.setText(name)
            bioInput.setText(bio)
        }
    }

    private fun setupTextWatchers() {
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                validateInputs()
            }
        }

        binding.apply {
            usernameInput.addTextChangedListener(textWatcher)
            bioInput.addTextChangedListener(textWatcher)
            oldPasswordInput.addTextChangedListener(textWatcher)
            newPasswordInput.addTextChangedListener(textWatcher)
            confirmPasswordInput.addTextChangedListener(textWatcher)
        }
    }

    private fun validateInputs() {
        val isUsernameModified = binding.usernameInput.text.toString() != originalUsername
        val isBioModified = binding.bioInput.text.toString() != originalBio &&
                binding.bioInput.text.toString().length <= 30 // Bio character limit
        val isPasswordMatching = binding.newPasswordInput.text.toString() ==
                binding.confirmPasswordInput.text.toString()
        val isPasswordDifferent = binding.oldPasswordInput.text.toString() !=
                binding.newPasswordInput.text.toString()
        val isPasswordNotEmpty = binding.oldPasswordInput.text.toString().isNotEmpty() &&
                binding.newPasswordInput.text.toString().isNotEmpty()
        val isPasswordLongEnough = binding.newPasswordInput.text.toString().length >= 6
        val isNewPasswordNotEmpty = binding.newPasswordInput.text.toString().isNotEmpty()
        val isConfirmPasswordNotEmpty = binding.confirmPasswordInput.text.toString().isNotEmpty()

        if (binding.bioInput.text.toString().length > 30)
            binding.bioLayout.error = "Bio must be less than 30 characters"
        else
            binding.bioLayout.error = null

        if (isConfirmPasswordNotEmpty && !isPasswordMatching) {
            binding.confirmPasswordLayout.error = "Passwords do not match"
            binding.confirmPasswordLayout.endIconMode = TextInputLayout.END_ICON_CUSTOM
        } else {
            binding.confirmPasswordLayout.error = null
            binding.confirmPasswordLayout.endIconMode = TextInputLayout.END_ICON_PASSWORD_TOGGLE
        }

        if (isNewPasswordNotEmpty && !isPasswordDifferent) {
            binding.newPasswordLayout.error = "New password must be different"
            binding.newPasswordLayout.endIconMode = TextInputLayout.END_ICON_CUSTOM
        } else if (!isPasswordLongEnough && binding.newPasswordInput.text.toString().isNotEmpty()) {
            binding.newPasswordLayout.error = "Password must be at least 6 characters"
            binding.newPasswordLayout.endIconMode = TextInputLayout.END_ICON_CUSTOM
        } else {
            binding.newPasswordLayout.error = null
            binding.newPasswordLayout.endIconMode = TextInputLayout.END_ICON_PASSWORD_TOGGLE
        }

        isPasswordModified = isPasswordNotEmpty && isPasswordDifferent && isPasswordMatching

        val shouldEnableSave = isUsernameModified || isBioModified || isPasswordModified

        updateSaveButtonState(shouldEnableSave)
    }

    private fun updateSaveButtonState(isEnabled: Boolean) {
        binding.saveButton.isEnabled = isEnabled

        if (isEnabled)
            binding.saveButton.backgroundTintList = ContextCompat.getColorStateList(this, R.color.red_500)
        else
            binding.saveButton.backgroundTintList = ContextCompat.getColorStateList(this, android.R.color.darker_gray)
    }

    private fun saveProfile(){
        var userNameUpdated = false
        var userBioUpdated = false
        var passwordUpdated = false
        CoroutineScope(Dispatchers.IO).launch {
            val username = binding.usernameInput.text.toString()
            if (username != originalUsername) {
                userNameUpdated = editProfileRepository.updateUserName(username)
            }
            val bio = binding.bioInput.text.toString()
            if (bio != originalBio) {
                userBioUpdated = editProfileRepository.updateUserBio(bio)
            }
            if (isPasswordModified) {
                val oldPassword = binding.oldPasswordInput.text.toString()
                val newPassword = binding.newPasswordInput.text.toString()
                val passwordChecked = editProfileRepository.checkOldPassword(oldPassword)
                if (!passwordChecked){
                    Log.d("EditProfile", "Old password is incorrect")
                    return@launch
                } else {
                    passwordUpdated = editProfileRepository.updatePassword(newPassword)
                }
            }

            if (userNameUpdated) Log.d("EditProfile", "User name updated")
            else Log.d("EditProfile", "User name not updated")

            if (userBioUpdated) Log.d("EditProfile", "User bio updated")
            else Log.d("EditProfile", "User bio not updated")

            if (passwordUpdated) Log.d("EditProfile", "User password updated")
            else Log.d("EditProfile", "User password not updated")

            val intent = Intent(this@EditProfileActivity, HomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }

    private fun performSignOut(){
        val userSignedOut = editProfileRepository.signOut()
        if (userSignedOut){
            val intent = Intent(this@EditProfileActivity, AuthActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }
}
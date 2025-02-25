package com.example.todolist.activity

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.todolist.R
import com.example.todolist.databinding.ActivityEditProfileBinding
import com.example.todolist.repository.EditProfileRepository
import com.example.todolist.utils.ValidationUtils
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EditProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditProfileBinding
    private lateinit var editProfileRepository: EditProfileRepository

    // Store original values to compare against
    private var originalUsername = ""
    private var originalBio = ""
    private var isUsernameModified = false
    private var isBioModified = false
    private var isPasswordModified = false

    private var fieldsUpdated = false


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
                handleBackPress()
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

            binding.apply {
                usernameInput.setText(name)
                bioInput.setText(bio)
            }
        }
    }

    private fun handleBackPress() {
        if (fieldsUpdated) {
            val intent = Intent(this@EditProfileActivity, HomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        finish()
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

    private fun validateInputs(){
        val username = binding.usernameInput.text.toString()
        val bio = binding.bioInput.text.toString()
        val oldPassword = binding.oldPasswordInput.text.toString()
        val newPassword = binding.newPasswordInput.text.toString()
        val confirmPassword = binding.confirmPasswordInput.text.toString()

        isUsernameModified = username != originalUsername
        isBioModified = bio != originalBio
        isPasswordModified = oldPassword.isNotEmpty() || newPassword.isNotEmpty()

        val isUserNameValid = isUserNameValid(username)
        val isBioValid = isBioValid(bio)
        val isPasswordValid = isPasswordValid(oldPassword, newPassword, confirmPassword)

        val shouldEnableSave = (isUsernameModified || isBioModified || isPasswordModified) &&
                isUserNameValid && isBioValid && isPasswordValid

        updateSaveButtonState(shouldEnableSave)
    }

    private fun isUserNameValid(username: String): Boolean {
        if (!isUsernameModified) return true

        val (isValid, message) = ValidationUtils.validateName(username)
        binding.usernameLayout.error = if (isValid) null else message
        return isValid
    }

    private fun isBioValid(bio: String): Boolean {
        if (!isBioModified) return true

        val (isValid, message) = ValidationUtils.validateBio(bio)
        binding.bioLayout.error = if (isValid) null else message
        return isValid
    }

    private fun isPasswordValid(oldPassword: String, newPassword: String, confirmPassword: String): Boolean {
        binding.apply {
            newPasswordLayout.error = null
            newPasswordLayout.isErrorEnabled = false
            confirmPasswordLayout.error = null
            confirmPasswordLayout.isErrorEnabled = false
        }

        if (!isPasswordModified) return true

        // Validate new password if provided
        val (isNewPasswordValid, newPasswordError) = ValidationUtils.validatePassword(newPassword)
        if (!isNewPasswordValid) {
            binding.newPasswordLayout.error = newPasswordError
            return false
        }

        // Validate new password do not match old password if provided
        val (isPasswordsMatch, passwordMatchError) = ValidationUtils.validatePasswordsMatch(oldPassword, newPassword)
        if (isPasswordsMatch) {
            binding.newPasswordLayout.error = passwordMatchError
            return false
        }

        // Validate confirm password if provided
        val (isPasswordConfirmed, passwordConfirmationError) = ValidationUtils.validatePasswordConfirmation(newPassword, confirmPassword)
        if (!isPasswordConfirmed) {
            binding.confirmPasswordLayout.error = passwordConfirmationError
            return false
        }

        return true
    }

    private fun updateSaveButtonState(isEnabled: Boolean) {
        binding.saveButton.isEnabled = isEnabled

        binding.saveButton.backgroundTintList = ContextCompat.getColorStateList(
            this,
            if (isEnabled) R.color.red_500 else android.R.color.darker_gray
        )
    }

    private fun saveProfile(){
        CoroutineScope(Dispatchers.IO).launch {
            try {
                var userNameUpdated = false
                var userBioUpdated = false
                var passwordUpdated = false
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
                        withContext(Dispatchers.Main) {
                            showSnackBar("Incorrect old password", false)
                        }
                    } else {
                        passwordUpdated = editProfileRepository.updatePassword(newPassword)
                    }
                }

                withContext(Dispatchers.Main) {
                    if (userNameUpdated) originalUsername = username
                    if (userBioUpdated) originalBio = bio
                    if (passwordUpdated) isPasswordModified = false

                    // Re-validate inputs to disable the save button if no changes remain
                    validateInputs()

                    if (userNameUpdated) showSnackBar("Name updated successfully")
                    if (userBioUpdated) showSnackBar("Bio updated successfully")
                    if (passwordUpdated) showSnackBar("Password updated successfully")
                }

                fieldsUpdated = true
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showSnackBar("An error occurred while saving changes", false)
                }
            }
        }
    }

    private suspend fun showSnackBar(message: String, state: Boolean = true) {
        val textColor: Int
        val backgroundColor: Int

        if (state){
            textColor = ContextCompat.getColor(this, R.color.medium_green)
            backgroundColor = ContextCompat.getColor(this, R.color.mint_cream)
        } else {
            textColor = ContextCompat.getColor(this, R.color.deep_red)
            backgroundColor = ContextCompat.getColor(this, R.color.rose_white)
        }

        val snackBar = Snackbar.make(binding.root, message, Snackbar.LENGTH_INDEFINITE).apply {
            setTextColor(textColor)
            setBackgroundTint(backgroundColor)
            show()
        }

        delay(1400)
        snackBar.dismiss()
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
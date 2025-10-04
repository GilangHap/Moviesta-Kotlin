package com.unsoed.moviesta

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    
    private lateinit var btnBack: ImageView
    private lateinit var etName: TextInputEditText
    private lateinit var etEmail: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var etConfirmPassword: TextInputEditText
    private lateinit var btnRegister: MaterialButton
    private lateinit var tvLogin: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        initViews()
        setupClickListeners()
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btnBack)
        etName = findViewById(R.id.etName)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)
        btnRegister = findViewById(R.id.btnRegister)
        tvLogin = findViewById(R.id.tvLogin)
    }

    private fun setupClickListeners() {
        btnBack.setOnClickListener {
            finish()
        }

        btnRegister.setOnClickListener {
            val name = etName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val confirmPassword = etConfirmPassword.text.toString().trim()

            if (validateInput(name, email, password, confirmPassword)) {
                createAccount(name, email, password)
            }
        }

        tvLogin.setOnClickListener {
            finish()
        }
    }

    private fun validateInput(name: String, email: String, password: String, confirmPassword: String): Boolean {
        if (name.isEmpty()) {
            etName.error = "Name is required"
            etName.requestFocus()
            return false
        }

        if (email.isEmpty()) {
            etEmail.error = "Email is required"
            etEmail.requestFocus()
            return false
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.error = "Please enter a valid email"
            etEmail.requestFocus()
            return false
        }

        if (password.isEmpty()) {
            etPassword.error = "Password is required"
            etPassword.requestFocus()
            return false
        }

        if (password.length < 6) {
            etPassword.error = "Password must be at least 6 characters"
            etPassword.requestFocus()
            return false
        }

        if (confirmPassword.isEmpty()) {
            etConfirmPassword.error = "Please confirm your password"
            etConfirmPassword.requestFocus()
            return false
        }

        if (password != confirmPassword) {
            etConfirmPassword.error = "Passwords do not match"
            etConfirmPassword.requestFocus()
            return false
        }

        return true
    }

    private fun createAccount(name: String, email: String, password: String) {
        btnRegister.isEnabled = false
        btnRegister.text = "Creating Account..."

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "createUserWithEmail:success")
                    val user = auth.currentUser
                    
                    // Update user profile with name
                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName(name)
                        .build()

                    user?.updateProfile(profileUpdates)
                        ?.addOnCompleteListener { updateTask ->
                            btnRegister.isEnabled = true
                            btnRegister.text = "Create Account"

                            if (updateTask.isSuccessful) {
                                Log.d(TAG, "User profile updated.")
                                Toast.makeText(this, "Account created successfully!", Toast.LENGTH_SHORT).show()
                                
                                // Navigate to MainActivity
                                startActivity(Intent(this, MainActivity::class.java))
                                finish()
                            } else {
                                Log.w(TAG, "updateProfile:failure", updateTask.exception)
                                Toast.makeText(this, "Account created but profile update failed", Toast.LENGTH_SHORT).show()
                                
                                // Still navigate to MainActivity
                                startActivity(Intent(this, MainActivity::class.java))
                                finish()
                            }
                        }
                } else {
                    btnRegister.isEnabled = true
                    btnRegister.text = "Create Account"
                    
                    Log.w(TAG, "createUserWithEmail:failure", task.exception)
                    Toast.makeText(this, "Registration failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }

    companion object {
        private const val TAG = "RegisterActivity"
    }
}
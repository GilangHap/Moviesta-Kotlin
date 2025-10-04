package com.unsoed.moviesta

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import android.widget.TextView
import android.widget.ImageView

class LoginActivityAlt : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    
    private lateinit var etEmail: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var btnLogin: MaterialButton
    private lateinit var btnGoogleSignIn: MaterialButton
    private lateinit var tvRegister: TextView

    private val googleSignInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            val account = task.getResult(ApiException::class.java)
            
            if (account != null) {
                Log.d(TAG, "Google Sign-In successful: ${account.email}")
                // Simple login without Firebase for testing
                Toast.makeText(this, "Welcome, ${account.displayName}!", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        } catch (e: ApiException) {
            Log.w(TAG, "Google sign in failed with code: ${e.statusCode}", e)
            Toast.makeText(this, "Google Sign-In failed", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        initViews()
        configureGoogleSignIn()
        setupClickListeners()
    }

    private fun initViews() {
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        btnGoogleSignIn = findViewById(R.id.btnGoogleSignIn)
        tvRegister = findViewById(R.id.tvRegister)
    }

    private fun configureGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    private fun setupClickListeners() {
        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (validateInput(email, password)) {
                signInWithEmailAndPassword(email, password)
            }
        }

        btnGoogleSignIn.setOnClickListener {
            signInWithGoogle()
        }

        tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun validateInput(email: String, password: String): Boolean {
        if (email.isEmpty()) {
            etEmail.error = "Email is required"
            etEmail.requestFocus()
            return false
        }

        if (password.isEmpty()) {
            etPassword.error = "Password is required"
            etPassword.requestFocus()
            return false
        }

        return true
    }

    private fun signInWithEmailAndPassword(email: String, password: String) {
        btnLogin.isEnabled = false
        btnLogin.text = "Logging in..."

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                btnLogin.isEnabled = true
                btnLogin.text = "Login"

                if (task.isSuccessful) {
                    val user = auth.currentUser
                    Toast.makeText(this, "Welcome back, ${user?.email}!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this, "Authentication failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        googleSignInLauncher.launch(signInIntent)
    }

    companion object {
        private const val TAG = "LoginActivityAlt"
    }
}
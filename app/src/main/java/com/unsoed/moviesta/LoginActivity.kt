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
import com.unsoed.moviesta.utils.GoogleServicesHelper
import com.unsoed.moviesta.utils.OnboardingManager
import com.unsoed.moviesta.repository.UserPreferencesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var userPreferencesRepository: UserPreferencesRepository
    
    private lateinit var etEmail: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var btnLogin: MaterialButton
    private lateinit var btnGoogleSignIn: MaterialButton
    private lateinit var tvRegister: TextView

    companion object {
        private const val TAG = "LoginActivity"
    }

    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)!!
            Log.d(TAG, "firebaseAuthWithGoogle:" + account.id)
            firebaseAuthWithGoogle(account.idToken!!)
        } catch (e: ApiException) {
            Log.w(TAG, "Google sign in failed", e)
            val errorMessage = when (e.statusCode) {
                12501 -> "Sign in was cancelled"
                12502 -> "Sign in currently in progress" 
                12500 -> "Sign in failed - please try again"
                10 -> {
                    Log.e(TAG, "DEVELOPER_ERROR: Check SHA-1 certificate, package name, and Web Client ID configuration")
                    "Konfigurasi Firebase error. Periksa SHA-1 certificate dan Web Client ID."
                }
                else -> "Google sign in failed: ${e.message}"
            }
            Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error during Google Sign-In", e)
            Toast.makeText(this, "Unexpected error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Initialize Firebase Auth and repositories
        auth = FirebaseAuth.getInstance()
        userPreferencesRepository = UserPreferencesRepository()

        // Check if user is already signed in
        if (auth.currentUser != null) {
            checkOnboardingStatus()
            return
        }

        // Check Google Play Services availability
        if (!GoogleServicesHelper.checkGooglePlayServices(this)) {
            Toast.makeText(this, "Google Play Services not available", Toast.LENGTH_LONG).show()
            return
        }

        initializeViews()
        setupClickListeners()
        configureGoogleSignIn()
    }

    private fun initializeViews() {
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        btnGoogleSignIn = findViewById(R.id.btnGoogleSignIn)
        tvRegister = findViewById(R.id.tvRegister)
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

        if (password.length < 6) {
            etPassword.error = "Password must be at least 6 characters"
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
                    Log.d(TAG, "signInWithEmail:success")
                    val user = auth.currentUser
                    Toast.makeText(this, "Welcome back, ${user?.email}!", Toast.LENGTH_SHORT).show()
                    
                    checkOnboardingStatus()
                } else {
                    Log.w(TAG, "signInWithEmail:failure", task.exception)
                    Toast.makeText(this, "Authentication failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun signInWithGoogle() {
        try {
            if (!GoogleServicesHelper.checkGooglePlayServices(this)) {
                Toast.makeText(this, "Google Play Services not available", Toast.LENGTH_SHORT).show()
                return
            }

            val signInIntent = googleSignInClient.signInIntent
            googleSignInLauncher.launch(signInIntent)
        } catch (e: Exception) {
            Log.e(TAG, "Error launching Google Sign-In", e)
            Toast.makeText(this, "Error starting Google Sign-In: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "signInWithCredential:success")
                    val user = auth.currentUser
                    Toast.makeText(this, "Welcome, ${user?.displayName}!", Toast.LENGTH_SHORT).show()
                    
                    checkOnboardingStatus()
                } else {
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    Toast.makeText(this, "Authentication failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun configureGoogleSignIn() {
        try {
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()

            googleSignInClient = GoogleSignIn.getClient(this, gso)
            Log.d(TAG, "Google Sign-In configured successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error configuring Google Sign-In", e)
            Toast.makeText(this, "Google Sign-In configuration error", Toast.LENGTH_SHORT).show()
            showFirebaseConfigHelp()
        }
    }

    private fun showFirebaseConfigHelp() {
        val helpMessage = """
            Firebase Configuration Error (Code 10):
            
            1. Periksa SHA-1 Certificate:
               - Generate: ./gradlew signingReport
               - Add ke Firebase Console
            
            2. Periksa Package Name:
               - Harus match: com.unsoed.moviesta
            
            3. Download ulang google-services.json
               dari Firebase Console
            
            4. Update Web Client ID di strings.xml
        """.trimIndent()
        
        Log.e(TAG, helpMessage)
    }
    
    /**
     * Check if user has completed onboarding and navigate accordingly
     */
    private fun checkOnboardingStatus() {
        Log.d(TAG, "Starting onboarding status check...")
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // First, verify user is authenticated
                val currentUser = FirebaseAuth.getInstance().currentUser
                if (currentUser == null) {
                    Log.e(TAG, "User not authenticated during onboarding check")
                    withContext(Dispatchers.Main) {
                        // Restart login process
                        recreate()
                    }
                    return@launch
                }
                
                Log.d(TAG, "User authenticated: ${currentUser.uid}, Email: ${currentUser.email}")
                
                // Check local SharedPreferences first (faster)
                val localOnboardingCompleted = OnboardingManager.isOnboardingCompleted(
                    this@LoginActivity, 
                    currentUser.uid
                )
                
                Log.d(TAG, "Local onboarding status: $localOnboardingCompleted")
                
                if (localOnboardingCompleted) {
                    // Local says completed, go to MainActivity
                    Log.d(TAG, "✅ Local onboarding completed, navigating to MainActivity")
                    withContext(Dispatchers.Main) {
                        val intent = Intent(this@LoginActivity, MainActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    }
                    return@launch
                }
                
                // Local says not completed, double-check with Firebase
                Log.d(TAG, "Local says not completed, checking Firebase...")
                val result = userPreferencesRepository.isOnboardingCompleted()
                
                withContext(Dispatchers.Main) {
                    if (result.isSuccess) {
                        val isCompleted = result.getOrNull() ?: false
                        Log.d(TAG, "Firebase onboarding check result - isCompleted: $isCompleted")
                        
                        if (isCompleted) {
                            // Firebase says completed, update local and go to MainActivity
                            Log.d(TAG, "✅ Firebase onboarding completed, updating local and navigating to MainActivity")
                            OnboardingManager.markOnboardingCompleted(this@LoginActivity, currentUser.uid)
                            
                            val intent = Intent(this@LoginActivity, MainActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                        } else {
                            // Firebase also says not completed, show onboarding
                            Log.d(TAG, "❌ Firebase onboarding not completed, navigating to OnboardingWelcomeActivity")
                            val intent = Intent(this@LoginActivity, OnboardingWelcomeActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                        }
                        finish()
                    } else {
                        // Error checking Firebase, assume not completed for safety
                        val error = result.exceptionOrNull()
                        Log.e(TAG, "❌ Error checking Firebase onboarding status: ${error?.message}", error)
                        
                        Log.d(TAG, "Defaulting to onboarding due to Firebase error")
                        val intent = Intent(this@LoginActivity, OnboardingWelcomeActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    }
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "Exception during onboarding check", e)
                withContext(Dispatchers.Main) {
                    // On error, show onboarding to be safe
                    Log.d(TAG, "Exception occurred, defaulting to onboarding")
                    val intent = Intent(this@LoginActivity, OnboardingWelcomeActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }
            }
        }
    }
}
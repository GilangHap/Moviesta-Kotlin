package com.unsoed.moviesta

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth

class OnboardingWelcomeActivity : AppCompatActivity() {
    
    private lateinit var auth: FirebaseAuth
    private lateinit var tvWelcomeMessage: TextView
    private lateinit var btnGetStarted: MaterialButton
    private lateinit var btnSkip: MaterialButton
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding_welcome)
        
        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()
        
        initializeViews()
        setupClickListeners()
        setupWelcomeMessage()
    }
    
    private fun initializeViews() {
        tvWelcomeMessage = findViewById(R.id.tv_welcome_message)
        btnGetStarted = findViewById(R.id.btn_get_started)
        btnSkip = findViewById(R.id.btn_skip)
    }
    
    private fun setupClickListeners() {
        btnGetStarted.setOnClickListener {
            startOnboardingFlow()
        }
        
        btnSkip.setOnClickListener {
            skipOnboarding()
        }
    }
    
    private fun setupWelcomeMessage() {
        val user = auth.currentUser
        val userName = user?.displayName ?: "Movie Lover"
        tvWelcomeMessage.text = "Selamat datang, $userName!\n\nAyuk personalisasi pengalaman Moviesta kamu dengan memilih genre favorit dan film yang sudah pernah kamu tonton."
    }
    
    private fun startOnboardingFlow() {
        val intent = Intent(this, OnboardingGenreActivity::class.java)
        startActivity(intent)
        finish()
    }
    
    private fun skipOnboarding() {
        // Skip to main activity but mark onboarding as skipped
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
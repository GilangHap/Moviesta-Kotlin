package com.unsoed.moviesta.base

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.unsoed.moviesta.LoginActivity

/**
 * Base Activity yang mengecek authentication status
 * Semua activity yang memerlukan login harus extend dari class ini
 */
abstract class BaseAuthActivity : AppCompatActivity() {

    protected lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()
        
        // Check authentication status
        checkAuthenticationStatus()
    }

    override fun onStart() {
        super.onStart()
        // Check again when activity starts
        checkAuthenticationStatus()
    }

    override fun onResume() {
        super.onResume()
        // Check again when activity resumes
        checkAuthenticationStatus()
    }

    private fun checkAuthenticationStatus() {
        val currentUser = auth.currentUser
        
        if (currentUser == null) {
            // User is not logged in, redirect to login
            redirectToLogin()
        } else {
            // User is logged in, allow activity to continue
            onAuthenticationSuccess()
        }
    }

    private fun redirectToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    /**
     * Called when user is authenticated
     * Override this method to perform any action after successful authentication
     */
    protected open fun onAuthenticationSuccess() {
        // Default implementation - do nothing
        // Override in child activities if needed
    }

    /**
     * Get current user safely
     */
    protected fun getCurrentUser() = auth.currentUser

    /**
     * Check if user is logged in
     */
    protected fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    /**
     * Sign out user and redirect to login
     */
    protected fun signOutUser() {
        auth.signOut()
        redirectToLogin()
    }
}
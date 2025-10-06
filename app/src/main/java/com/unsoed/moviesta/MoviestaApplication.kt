package com.unsoed.moviesta

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.unsoed.moviesta.utils.FirebaseConfig

class MoviestaApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // Force disable dark mode
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        
        // Initialize Firebase configuration
        FirebaseConfig.initialize(this)
        
        // Enable Firestore logging in debug mode
        FirebaseConfig.enableFirestoreLogging(true)
    }
}
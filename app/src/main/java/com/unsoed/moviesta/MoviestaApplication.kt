package com.unsoed.moviesta

import android.app.Application
import com.unsoed.moviesta.utils.FirebaseConfig

class MoviestaApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize Firebase configuration
        FirebaseConfig.initialize(this)
        
        // Enable Firestore logging in debug mode
        FirebaseConfig.enableFirestoreLogging(true)
    }
}
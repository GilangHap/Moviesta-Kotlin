package com.unsoed.moviesta.utils

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings

object FirebaseConfig {
    
    private var isInitialized = false
    
    fun initialize(context: Context) {
        if (isInitialized) return
        
        try {
            // Initialize Firebase if not already initialized
            if (FirebaseApp.getApps(context).isEmpty()) {
                FirebaseApp.initializeApp(context)
            }
            
            // Configure Firestore settings
            configureFirestore()
            
            isInitialized = true
        } catch (e: Exception) {
            throw Exception("Failed to initialize Firebase", e)
        }
    }
    
    private fun configureFirestore() {
        try {
            val firestore = FirebaseFirestore.getInstance()
            
            // Configure Firestore settings for better performance
            val settings = FirebaseFirestoreSettings.Builder()
                .build()
                
            firestore.firestoreSettings = settings
            
            // Test connectivity
            firestore.enableNetwork()
            
            android.util.Log.d("FirebaseConfig", "Firestore configured successfully")
        } catch (e: Exception) {
            android.util.Log.e("FirebaseConfig", "Failed to configure Firestore", e)
        }
    }
    
    fun enableFirestoreLogging(enable: Boolean) {
        FirebaseFirestore.setLoggingEnabled(enable)
    }
    
    fun isUserAuthenticated(): Boolean {
        return FirebaseAuth.getInstance().currentUser != null
    }
    
    fun getCurrentUserId(): String? {
        return FirebaseAuth.getInstance().currentUser?.uid
    }
}
package com.unsoed.moviesta.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log

object OnboardingManager {
    
    private const val PREF_NAME = "onboarding_prefs"
    private const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"
    private const val KEY_USER_ID = "user_id"
    
    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }
    
    /**
     * Mark onboarding as completed for current user
     */
    fun markOnboardingCompleted(context: Context, userId: String) {
        Log.d("OnboardingManager", "Marking onboarding completed for user: $userId")
        getPreferences(context).edit()
            .putBoolean(KEY_ONBOARDING_COMPLETED, true)
            .putString(KEY_USER_ID, userId)
            .apply()
    }
    
    /**
     * Check if onboarding is completed for current user
     */
    fun isOnboardingCompleted(context: Context, userId: String): Boolean {
        val prefs = getPreferences(context)
        val isCompleted = prefs.getBoolean(KEY_ONBOARDING_COMPLETED, false)
        val savedUserId = prefs.getString(KEY_USER_ID, "")
        
        // Check if onboarding is completed AND it's for the same user
        val result = isCompleted && savedUserId == userId
        Log.d("OnboardingManager", "Onboarding completed check: $result (saved for user: $savedUserId, current: $userId)")
        
        return result
    }
    
    /**
     * Clear onboarding status (for logout)
     */
    fun clearOnboardingStatus(context: Context) {
        Log.d("OnboardingManager", "Clearing onboarding status")
        getPreferences(context).edit()
            .remove(KEY_ONBOARDING_COMPLETED)
            .remove(KEY_USER_ID)
            .apply()
    }
    
    /**
     * Reset for different user
     */
    fun resetForNewUser(context: Context) {
        clearOnboardingStatus(context)
    }
}
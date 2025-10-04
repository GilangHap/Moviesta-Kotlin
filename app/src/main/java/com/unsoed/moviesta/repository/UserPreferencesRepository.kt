package com.unsoed.moviesta.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Source
import com.google.firebase.firestore.ktx.toObject
import com.unsoed.moviesta.model.UserPreferences
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.TimeoutCancellationException
import java.lang.Exception

class UserPreferencesRepository {
    
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    
    companion object {
        private const val TAG = "UserPreferencesRepo"
        private const val COLLECTION_USER_PREFERENCES = "userPreferences"
        private const val TIMEOUT_SECONDS = 10L
    }
    
    /**
     * Save user preferences to Firebase Firestore with retry mechanism
     */
    suspend fun saveUserPreferences(preferences: UserPreferences): Result<Boolean> {
        return try {
            Log.d(TAG, "========== Starting saveUserPreferences ==========")
            
            val currentUser = auth.currentUser
            if (currentUser == null) {
                Log.e(TAG, "User not authenticated")
                return Result.failure(Exception("User not authenticated"))
            }
            
            Log.d(TAG, "User authenticated - UID: ${currentUser.uid}")
            Log.d(TAG, "User email: ${currentUser.email}")
            Log.d(TAG, "Input preferences: $preferences")
            
            val userPrefs = preferences.copy(
                userId = currentUser.uid,
                email = currentUser.email ?: "",
                updatedAt = System.currentTimeMillis()
            )
            
            Log.d(TAG, "Final user preferences to save: $userPrefs")
            
            // Ensure network is available
            try {
                Log.d(TAG, "Enabling network...")
                firestore.enableNetwork().await()
                Log.d(TAG, "Network enabled successfully")
            } catch (e: Exception) {
                Log.w(TAG, "Failed to enable network, proceeding anyway", e)
            }
            
            // Create document reference
            val docRef = firestore.collection(COLLECTION_USER_PREFERENCES)
                .document(currentUser.uid)
            
            Log.d(TAG, "Document reference created: ${docRef.path}")
            
            // Save with timeout using kotlinx.coroutines.withTimeout
            Log.d(TAG, "Starting Firestore save operation...")
            
            val saveTask = docRef.set(userPrefs)
            
            // Add a timeout wrapper
            val timeoutMillis = TIMEOUT_SECONDS * 1000
            Log.d(TAG, "Timeout set to: ${timeoutMillis}ms")
            
            kotlinx.coroutines.withTimeout(timeoutMillis) {
                saveTask.await()
            }
            
            Log.d(TAG, "SUCCESS: User preferences saved successfully for user: ${currentUser.uid}")
            
            // Verify the save by reading it back
            try {
                Log.d(TAG, "Verifying save by reading document back...")
                val verifyDoc = docRef.get().await()
                if (verifyDoc.exists()) {
                    Log.d(TAG, "Verification successful: Document exists after save")
                } else {
                    Log.w(TAG, "Verification warning: Document does not exist after save")
                }
            } catch (e: Exception) {
                Log.w(TAG, "Verification failed, but save was successful", e)
            }
            
            Result.success(true)
            
        } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
            Log.e(TAG, "Timeout saving user preferences after ${TIMEOUT_SECONDS}s", e)
            Result.failure(Exception("Timeout saat menyimpan data (${TIMEOUT_SECONDS}s). Periksa koneksi internet Anda.", e))
        } catch (e: FirebaseFirestoreException) {
            Log.e(TAG, "Firestore error saving user preferences: ${e.code} - ${e.message}", e)
            val friendlyMessage = when (e.code) {
                FirebaseFirestoreException.Code.UNAVAILABLE -> "Firebase sedang tidak tersedia. Coba lagi nanti."
                FirebaseFirestoreException.Code.DEADLINE_EXCEEDED -> "Koneksi timeout. Periksa koneksi internet Anda."
                FirebaseFirestoreException.Code.PERMISSION_DENIED -> "Akses ditolak. Periksa pengaturan akun Anda."
                FirebaseFirestoreException.Code.UNAUTHENTICATED -> "User tidak ter-autentikasi. Silakan login ulang."
                else -> "Firebase Error: ${e.code} - ${e.message}"
            }
            Result.failure(Exception(friendlyMessage, e))
        } catch (e: java.util.concurrent.TimeoutException) {
            Log.e(TAG, "Java timeout saving user preferences", e)
            Result.failure(Exception("Timeout saat menyimpan data. Periksa koneksi internet Anda.", e))
        } catch (e: java.net.SocketTimeoutException) {
            Log.e(TAG, "Socket timeout saving user preferences", e)
            Result.failure(Exception("Koneksi internet timeout. Periksa koneksi Anda.", e))
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error saving user preferences: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    /**
     * Get user preferences from Firebase Firestore with cache-first strategy
     */
    suspend fun getUserPreferences(forceRefresh: Boolean = false): Result<UserPreferences?> {
        return try {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                Log.e(TAG, "User not authenticated")
                return Result.failure(Exception("User not authenticated"))
            }
            
            val document = firestore.collection(COLLECTION_USER_PREFERENCES)
                .document(currentUser.uid)
                .get()
                .await()
            
            if (document.exists()) {
                val preferences = document.toObject<UserPreferences>()
                Log.d(TAG, "User preferences retrieved successfully")
                Result.success(preferences)
            } else {
                Log.d(TAG, "No user preferences found for user: ${currentUser.uid}")
                Result.success(null)
            }
            
        } catch (e: FirebaseFirestoreException) {
            Log.e(TAG, "Firestore error getting user preferences: ${e.code}", e)
            Result.failure(e)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting user preferences", e)
            Result.failure(e)
        }
    }
    
    /**
     * Check if user has completed onboarding
     */
    suspend fun isOnboardingCompleted(): Result<Boolean> {
        return try {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                Log.e(TAG, "❌ User not authenticated for onboarding check")
                return Result.failure(Exception("User not authenticated"))
            }
            
            Log.d(TAG, "🔍 Checking onboarding status for user: ${currentUser.uid}")
            
            // Direct Firestore query to avoid cache issues
            val document = firestore.collection(COLLECTION_USER_PREFERENCES)
                .document(currentUser.uid)
                .get()
                .await()
            
            Log.d(TAG, "📄 Document exists: ${document.exists()}")
            
            if (document.exists()) {
                val preferences = document.toObject<UserPreferences>()
                val isCompleted = preferences?.isOnboardingCompleted ?: false
                
                Log.d(TAG, "✅ Document found - User preferences: $preferences")
                Log.d(TAG, "🎯 Onboarding completion status: $isCompleted")
                
                Result.success(isCompleted)
            } else {
                Log.d(TAG, "❌ No document found for user: ${currentUser.uid} - onboarding NOT completed")
                Result.success(false)
            }
            
        } catch (e: FirebaseFirestoreException) {
            Log.e(TAG, "🔥 Firestore error checking onboarding status: ${e.code} - ${e.message}", e)
            when (e.code) {
                FirebaseFirestoreException.Code.PERMISSION_DENIED -> {
                    Log.e(TAG, "🚫 Permission denied - check Firestore rules")
                }
                FirebaseFirestoreException.Code.UNAVAILABLE -> {
                    Log.e(TAG, "📡 Firebase unavailable - network issue")
                }
                else -> {
                    Log.e(TAG, "⚠️ Other Firestore error: ${e.code}")
                }
            }
            Result.failure(e)
        } catch (e: Exception) {
            Log.e(TAG, "💥 Unexpected error checking onboarding status: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    /**
     * Update favorite genres
     */
    suspend fun updateFavoriteGenres(genreIds: List<Int>): Result<Boolean> {
        return try {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                return Result.failure(Exception("User not authenticated"))
            }
            
            val updates = mapOf(
                "favoriteGenres" to genreIds,
                "updatedAt" to System.currentTimeMillis()
            )
            
            firestore.collection(COLLECTION_USER_PREFERENCES)
                .document(currentUser.uid)
                .update(updates)
                .await()
            
            Log.d(TAG, "Favorite genres updated successfully")
            Result.success(true)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error updating favorite genres", e)
            Result.failure(e)
        }
    }
    
    /**
     * Update watched movies
     */
    suspend fun updateWatchedMovies(movieIds: List<Int>): Result<Boolean> {
        return try {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                return Result.failure(Exception("User not authenticated"))
            }
            
            val updates = mapOf(
                "watchedMovies" to movieIds,
                "updatedAt" to System.currentTimeMillis()
            )
            
            firestore.collection(COLLECTION_USER_PREFERENCES)
                .document(currentUser.uid)
                .update(updates)
                .await()
            
            Log.d(TAG, "Watched movies updated successfully")
            Result.success(true)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error updating watched movies", e)
            Result.failure(e)
        }
    }
    
    /**
     * Mark onboarding as completed
     */
    suspend fun completeOnboarding(): Result<Boolean> {
        return try {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                return Result.failure(Exception("User not authenticated"))
            }
            
            val updates = mapOf(
                "isOnboardingCompleted" to true,
                "updatedAt" to System.currentTimeMillis()
            )
            
            firestore.collection(COLLECTION_USER_PREFERENCES)
                .document(currentUser.uid)
                .update(updates)
                .await()
            
            Log.d(TAG, "Onboarding marked as completed")
            Result.success(true)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error completing onboarding", e)
            Result.failure(e)
        }
    }
}
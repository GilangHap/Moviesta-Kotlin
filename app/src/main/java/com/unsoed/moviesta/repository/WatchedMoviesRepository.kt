package com.unsoed.moviesta.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.unsoed.moviesta.model.Film
import com.unsoed.moviesta.model.WatchedMovieInfo
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WatchedMoviesRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val userPreferencesRepository: UserPreferencesRepository
) {
    companion object {
        private const val TAG = "WatchedMoviesRepo"
    }
    
    /**
     * Add movie to watched list using UserPreferences only
     */
    suspend fun addWatchedMovie(film: Film): Result<String> {
        return try {
            Log.d(TAG, "Adding movie to watched list: ${film.title}")
            
            val currentUser = auth.currentUser
            if (currentUser == null) {
                Log.e(TAG, "User not authenticated")
                return Result.failure(Exception("User not authenticated"))
            }
            
            // Get current preferences
            val preferencesResult = userPreferencesRepository.getUserPreferences()
            if (preferencesResult.isFailure) {
                Log.e(TAG, "Failed to get user preferences")
                return Result.failure(preferencesResult.exceptionOrNull() ?: Exception("Failed to get preferences"))
            }
            
            val currentPreferences = preferencesResult.getOrNull()
            if (currentPreferences == null) {
                Log.e(TAG, "User preferences is null")
                return Result.failure(Exception("User preferences not found"))
            }
            
            // Check if already watched
            if (currentPreferences.watchedMovies.contains(film.id)) {
                Log.d(TAG, "Movie already in watched list")
                return Result.success("already_exists")
            }
            
            // Create watched movie info
            val watchedMovieInfo = WatchedMovieInfo(
                movieId = film.id,
                title = film.title ?: "Unknown Title",
                posterUrl = film.posterPath ?: "",
                genre = "", // Will be filled later if needed
                releaseYear = film.releaseDate?.substring(0, 4) ?: "",
                rating = film.rating,
                watchedDate = System.currentTimeMillis(),
                notes = "",
                personalRating = 0.0
            )
            
            // Update preferences with new watched movie
            val updatedWatchedMovies = currentPreferences.watchedMovies.toMutableList()
            updatedWatchedMovies.add(film.id)
            
            val updatedWatchedDetails = currentPreferences.watchedMoviesDetails.toMutableList()
            updatedWatchedDetails.add(watchedMovieInfo)
            
            val updatedPreferences = currentPreferences.copy(
                watchedMovies = updatedWatchedMovies,
                watchedMoviesDetails = updatedWatchedDetails,
                updatedAt = System.currentTimeMillis()
            )
            
            // Save to UserPreferences
            val saveResult = userPreferencesRepository.saveUserPreferences(updatedPreferences)
            if (saveResult.isSuccess) {
                Log.d(TAG, "Successfully added movie to watched list in UserPreferences")
                Result.success("success")
            } else {
                Log.e(TAG, "Failed to save updated preferences")
                Result.failure(saveResult.exceptionOrNull() ?: Exception("Save failed"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error adding watched movie", e)
            Result.failure(e)
        }
    }
    
    /**
     * Check if movie is already watched
     */
    suspend fun isMovieWatched(movieId: String): Result<Boolean> {
        return try {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                return Result.failure(Exception("User not authenticated"))
            }
            
            val preferencesResult = userPreferencesRepository.getUserPreferences()
            if (preferencesResult.isSuccess) {
                val preferences = preferencesResult.getOrNull()
                val isWatched = preferences?.watchedMovies?.contains(movieId.toInt()) ?: false
                Log.d(TAG, "Movie $movieId watched status: $isWatched")
                Result.success(isWatched)
            } else {
                Result.success(false)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking watched status", e)
            Result.failure(e)
        }
    }
    
    /**
     * Remove movie from watched list
     */
    suspend fun removeWatchedMovie(movieId: String): Result<Boolean> {
        return try {
            Log.d(TAG, "Removing movie from watched list: $movieId")
            
            val currentUser = auth.currentUser
            if (currentUser == null) {
                Log.e(TAG, "User not authenticated for remove operation")
                return Result.failure(Exception("User not authenticated"))
            }
            
            // Get current preferences
            val preferencesResult = userPreferencesRepository.getUserPreferences()
            if (preferencesResult.isFailure) {
                Log.e(TAG, "Failed to get user preferences")
                return Result.failure(preferencesResult.exceptionOrNull() ?: Exception("Failed to get preferences"))
            }
            
            val currentPreferences = preferencesResult.getOrNull()
            if (currentPreferences == null) {
                Log.e(TAG, "User preferences is null")
                return Result.failure(Exception("User preferences not found"))
            }
            
            val movieIdInt = movieId.toInt()
            
            // Check if movie exists in watched list
            if (!currentPreferences.watchedMovies.contains(movieIdInt)) {
                Log.w(TAG, "Movie $movieId not found in watched list")
                return Result.success(false)
            }
            
            // Remove from both lists
            val updatedWatchedMovies = currentPreferences.watchedMovies.toMutableList()
            updatedWatchedMovies.remove(movieIdInt)
            
            val updatedWatchedDetails = currentPreferences.watchedMoviesDetails.toMutableList()
            updatedWatchedDetails.removeAll { it.movieId == movieIdInt }
            
            val updatedPreferences = currentPreferences.copy(
                watchedMovies = updatedWatchedMovies,
                watchedMoviesDetails = updatedWatchedDetails,
                updatedAt = System.currentTimeMillis()
            )
            
            // Save updated preferences
            val saveResult = userPreferencesRepository.saveUserPreferences(updatedPreferences)
            if (saveResult.isSuccess) {
                Log.d(TAG, "Successfully removed movie from watched list")
                Result.success(true)
            } else {
                Log.e(TAG, "Failed to save updated preferences after removal")
                Result.failure(saveResult.exceptionOrNull() ?: Exception("Save failed"))
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error removing watched movie", e)
            Result.failure(e)
        }
    }
    
    /**
     * Get all watched movies for current user
     */
    suspend fun getUserWatchedMovies(): Result<List<WatchedMovieInfo>> {
        return try {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                return Result.failure(Exception("User not authenticated"))
            }
            
            val preferencesResult = userPreferencesRepository.getUserPreferences()
            if (preferencesResult.isSuccess) {
                val preferences = preferencesResult.getOrNull()
                val watchedMovies = preferences?.watchedMoviesDetails ?: emptyList()
                Log.d(TAG, "Retrieved ${watchedMovies.size} watched movies from UserPreferences")
                Result.success(watchedMovies)
            } else {
                Log.e(TAG, "Failed to get user preferences for watched movies")
                Result.failure(preferencesResult.exceptionOrNull() ?: Exception("Failed to get preferences"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting user watched movies", e)
            Result.failure(e)
        }
    }
    
    /**
     * Get watched movies from user preferences (for quick access)
     */
    suspend fun getWatchedMoviesFromPreferences(): Result<List<Int>> {
        return try {
            val preferencesResult = userPreferencesRepository.getUserPreferences()
            if (preferencesResult.isSuccess) {
                val preferences = preferencesResult.getOrNull()
                Result.success(preferences?.watchedMovies ?: emptyList())
            } else {
                Result.success(emptyList())
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting watched movies from preferences", e)
            Result.failure(e)
        }
    }
    
    /**
     * Add movie to user preferences (public method for sync)
     */
    suspend fun addToUserPreferences(movieId: Int) {
        // This method is now integrated into addWatchedMovie
        Log.d(TAG, "Direct preference update for movie ID: $movieId")
    }
    
    /**
     * Remove movie from user preferences (public method for sync)
     */
    suspend fun removeFromUserPreferences(movieId: Int) {
        // This method is now integrated into removeWatchedMovie
        Log.d(TAG, "Direct preference removal for movie ID: $movieId")
    }
    
    /**
     * Get user's favorite genres for recommendations
     */
    suspend fun getUserFavoriteGenres(): Result<List<Int>> {
        return try {
            val preferencesResult = userPreferencesRepository.getUserPreferences()
            if (preferencesResult.isSuccess) {
                val preferences = preferencesResult.getOrNull()
                Result.success(preferences?.favoriteGenres ?: emptyList())
            } else {
                Result.success(emptyList())
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting favorite genres", e)
            Result.failure(e)
        }
    }
}
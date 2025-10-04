package com.unsoed.moviesta.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import com.google.firebase.firestore.PropertyName

@Parcelize
data class UserPreferences(
    var userId: String = "",
    var username: String = "",
    var email: String = "",
    var favoriteGenres: List<Int> = emptyList(), // Genre IDs
    var watchedMovies: List<Int> = emptyList(), // Movie IDs (for quick check)
    var watchedMoviesDetails: List<WatchedMovieInfo> = emptyList(), // Detailed info
    @PropertyName("onboardingCompleted")
    var isOnboardingCompleted: Boolean = false,
    var createdAt: Long = System.currentTimeMillis(),
    var updatedAt: Long = System.currentTimeMillis()
) : Parcelable

@Parcelize
data class WatchedMovieInfo(
    val movieId: Int = 0,
    val title: String = "",
    val posterUrl: String = "",
    val genre: String = "",
    val releaseYear: String = "",
    val rating: Double = 0.0,
    val watchedDate: Long = System.currentTimeMillis(),
    val notes: String = "",
    val personalRating: Double = 0.0
) : Parcelable

@Parcelize
data class OnboardingData(
    val selectedGenres: List<Genre> = emptyList(),
    val watchedMovies: List<Film> = emptyList(),
    val currentStep: Int = 1,
    val totalSteps: Int = 3
) : Parcelable

@Parcelize
data class GenrePreference(
    val genreId: Int,
    val genreName: String,
    val isSelected: Boolean = false
) : Parcelable

@Parcelize
data class MovieSelection(
    val movieId: Int,
    val title: String,
    val posterPath: String,
    val isWatched: Boolean = false
) : Parcelable
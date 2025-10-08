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
    var watchlistMovies: List<Int> = emptyList(), // Watchlist Movie IDs (for quick check)
    var watchlistMoviesDetails: List<WatchlistMovieInfo> = emptyList(), // Detailed watchlist info
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
data class WatchlistMovieInfo(
    val movieId: Int = 0,
    val title: String = "",
    val overview: String = "",
    val posterPath: String = "",
    val voteAverage: Double = 0.0,
    val releaseDate: String = "",
    val addedDate: Long = System.currentTimeMillis()
) : Parcelable {
    
    companion object {
        // Convert WatchlistItem to WatchlistMovieInfo
        fun fromWatchlistItem(watchlistItem: WatchlistItem): WatchlistMovieInfo {
            return WatchlistMovieInfo(
                movieId = watchlistItem.id,
                title = watchlistItem.title,
                overview = watchlistItem.overview ?: "",
                posterPath = watchlistItem.posterPath ?: "",
                voteAverage = watchlistItem.voteAverage,
                releaseDate = watchlistItem.releaseDate ?: "",
                addedDate = watchlistItem.addedDate
            )
        }
        
        // Convert Film to WatchlistMovieInfo
        fun fromFilm(film: Film): WatchlistMovieInfo {
            return WatchlistMovieInfo(
                movieId = film.id,
                title = film.safeTitle,
                overview = film.sinopsis ?: "",
                posterPath = film.posterPath ?: "",
                voteAverage = film.rating,
                releaseDate = "",
                addedDate = System.currentTimeMillis()
            )
        }
        
        // Convert FilmDetail to WatchlistMovieInfo
        fun fromFilmDetail(filmDetail: FilmDetail): WatchlistMovieInfo {
            return WatchlistMovieInfo(
                movieId = filmDetail.id,
                title = filmDetail.safeTitle,
                overview = filmDetail.overview ?: "",
                posterPath = filmDetail.posterPath ?: "",
                voteAverage = filmDetail.rating,
                releaseDate = filmDetail.releaseDate ?: "",
                addedDate = System.currentTimeMillis()
            )
        }
    }
    
    // Convert to WatchlistItem for compatibility
    fun toWatchlistItem(): WatchlistItem {
        return WatchlistItem(
            id = movieId,
            title = title,
            overview = overview,
            posterPath = posterPath,
            voteAverage = voteAverage,
            releaseDate = releaseDate,
            addedDate = addedDate
        )
    }
}

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
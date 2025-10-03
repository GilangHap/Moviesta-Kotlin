package com.unsoed.moviesta.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Entity(tableName = "watchlist")
@Parcelize
data class WatchlistItem(
    @PrimaryKey
    val id: Int,
    val title: String,
    val overview: String?,
    val posterPath: String?,
    val voteAverage: Double,
    val releaseDate: String?,
    val addedDate: Long = System.currentTimeMillis()
) : Parcelable {
    
    // Computed property untuk compatibility dengan Film model
    val rating: Double get() = voteAverage
    val sinopsis: String? get() = overview
    
    companion object {
        // Convert Film to WatchlistItem
        fun fromFilm(film: Film): WatchlistItem {
            return WatchlistItem(
                id = film.id,
                title = film.safeTitle,
                overview = film.sinopsis,
                posterPath = film.posterPath,
                voteAverage = film.rating, // Use computed property that handles null
                releaseDate = null // Film model doesn't have release date
            )
        }
        
        // Convert FilmDetail to WatchlistItem
        fun fromFilmDetail(filmDetail: FilmDetail): WatchlistItem {
            return WatchlistItem(
                id = filmDetail.id,
                title = filmDetail.safeTitle, // Use safeTitle instead of nullable title
                overview = filmDetail.overview,
                posterPath = filmDetail.posterPath,
                voteAverage = filmDetail.rating, // Use computed property that handles null
                releaseDate = filmDetail.releaseDate
            )
        }
    }
    
    // Convert WatchlistItem to Film for compatibility
    fun toFilm(): Film {
        return Film(
            id = this.id,
            title = this.title,
            sinopsis = this.overview,
            posterPath = this.posterPath,
            voteAverage = this.voteAverage
        )
    }
}
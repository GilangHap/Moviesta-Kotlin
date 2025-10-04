package com.unsoed.moviesta.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName

data class WatchedMovie(
    @PropertyName("movie_id")
    val movieId: String = "",
    
    @PropertyName("title")
    val title: String = "",
    
    @PropertyName("poster_url")
    val posterUrl: String = "",
    
    @PropertyName("genre")
    val genre: String = "",
    
    @PropertyName("release_year")
    val releaseYear: String = "",
    
    @PropertyName("rating")
    val rating: Double = 0.0,
    
    @PropertyName("watched_date")
    val watchedDate: Timestamp = Timestamp.now(),
    
    @PropertyName("user_id")
    val userId: String = "",
    
    @PropertyName("notes")
    val notes: String = "",
    
    @PropertyName("personal_rating")
    val personalRating: Double = 0.0
)
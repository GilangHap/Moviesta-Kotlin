package com.unsoed.moviesta.network

import com.unsoed.moviesta.model.FilmResponse
import com.unsoed.moviesta.model.CreditsResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface TmdbApiService {

    // GANTI dengan API KEY V3 ANDA YANG SEBENARNYA!
    companion object {
        const val API_KEY = "b6c962675e06cdc62d53c04310038ae6"
        const val BASE_URL = "https://api.themoviedb.org/3/"
    }

    @GET("movie/popular")
    suspend fun getPopularFilms(
        @Query("api_key") apiKey: String = API_KEY
    ): FilmResponse

    @GET("search/movie")
    suspend fun searchFilms(
        @Query("api_key") apiKey: String = API_KEY,
        @Query("query") query: String
    ): FilmResponse

    @GET("movie/{movie_id}/credits")
    suspend fun getFilmCredits(
        @Path("movie_id") movieId: Int, // Menggunakan @Path untuk ID film
        @Query("api_key") apiKey: String = API_KEY
    ): CreditsResponse // Mengembalikan objek CreditsResponse
}
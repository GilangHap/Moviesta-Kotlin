package com.unsoed.moviesta.network

import com.unsoed.moviesta.model.FilmResponse
import com.unsoed.moviesta.model.FilmDetail
import com.unsoed.moviesta.model.CreditsResponse
import com.unsoed.moviesta.model.GenreResponse
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

    @GET("movie/{movie_id}")
    suspend fun getFilmDetail(
        @Path("movie_id") movieId: Int,
        @Query("api_key") apiKey: String = API_KEY
    ): FilmDetail

    @GET("movie/{movie_id}/credits")
    suspend fun getFilmCredits(
        @Path("movie_id") movieId: Int, // Menggunakan @Path untuk ID film
        @Query("api_key") apiKey: String = API_KEY
    ): CreditsResponse // Mengembalikan objek CreditsResponse

    @GET("movie/{movie_id}/similar")
    suspend fun getSimilarFilms(
        @Path("movie_id") movieId: Int,
        @Query("api_key") apiKey: String = API_KEY,
        @Query("page") page: Int = 1
    ): FilmResponse

    @GET("movie/{movie_id}/recommendations")
    suspend fun getRecommendedFilms(
        @Path("movie_id") movieId: Int,
        @Query("api_key") apiKey: String = API_KEY,
        @Query("page") page: Int = 1
    ): FilmResponse

    @GET("genre/movie/list")
    suspend fun getGenres(
        @Query("api_key") apiKey: String = API_KEY,
        @Query("language") language: String = "id-ID"
    ): GenreResponse

    @GET("discover/movie")
    suspend fun getMoviesByGenre(
        @Query("api_key") apiKey: String = API_KEY,
        @Query("with_genres") genreId: Int,
        @Query("sort_by") sortBy: String = "popularity.desc",
        @Query("page") page: Int = 1
    ): FilmResponse

    @GET("movie/top_rated")
    suspend fun getTopRatedFilms(
        @Query("api_key") apiKey: String = API_KEY,
        @Query("page") page: Int = 1
    ): FilmResponse

    @GET("movie/upcoming")
    suspend fun getUpcomingFilms(
        @Query("api_key") apiKey: String = API_KEY,
        @Query("page") page: Int = 1
    ): FilmResponse

    @GET("movie/now_playing")
    suspend fun getNowPlayingFilms(
        @Query("api_key") apiKey: String = API_KEY,
        @Query("page") page: Int = 1
    ): FilmResponse

    @GET("trending/movie/week")
    suspend fun getTrendingFilms(
        @Query("api_key") apiKey: String = API_KEY
    ): FilmResponse
}
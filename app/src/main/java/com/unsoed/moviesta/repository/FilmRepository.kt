package com.unsoed.moviesta.repository

import com.unsoed.moviesta.model.Film
import com.unsoed.moviesta.model.Cast
import com.unsoed.moviesta.network.TmdbApiService

class FilmRepository(private val apiService: TmdbApiService) {

    suspend fun getPopularFilms(): List<Film> {
        return apiService.getPopularFilms().films
    }

    suspend fun searchFilms(query: String): List<Film> {
        return apiService.searchFilms(query = query).films
    }

    suspend fun getFilmCast(movieId: Int): List<Cast> {
        return apiService.getFilmCredits(movieId).cast
    }
}
package com.unsoed.moviesta.repository

import com.unsoed.moviesta.model.Film
import com.unsoed.moviesta.model.FilmDetail
import com.unsoed.moviesta.model.Cast
import com.unsoed.moviesta.model.Genre
import com.unsoed.moviesta.network.TmdbApiService

class FilmRepository(private val apiService: TmdbApiService) {

    suspend fun getPopularFilms(): List<Film> {
        return apiService.getPopularFilms().films
    }

    suspend fun searchFilms(query: String): List<Film> {
        return apiService.searchFilms(query = query).films
    }

    suspend fun getFilmDetail(movieId: Int): FilmDetail {
        return apiService.getFilmDetail(movieId)
    }

    suspend fun getFilmCast(movieId: Int): List<Cast> {
        return apiService.getFilmCredits(movieId).cast
    }

    suspend fun getSimilarFilms(movieId: Int): List<Film> {
        return try {
            apiService.getSimilarFilms(movieId).films
        } catch (e: Exception) {
            // Fallback to recommendations if similar films fail
            getRecommendedFilms(movieId)
        }
    }

    suspend fun getRecommendedFilms(movieId: Int): List<Film> {
        return try {
            apiService.getRecommendedFilms(movieId).films
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getGenres(): List<Genre> {
        return try {
            apiService.getGenres().genres
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getMoviesByGenre(genreId: Int): List<Film> {
        return try {
            apiService.getMoviesByGenre(genreId = genreId).films
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getTopRatedFilms(): List<Film> {
        return try {
            apiService.getTopRatedFilms().films
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getUpcomingFilms(): List<Film> {
        return try {
            apiService.getUpcomingFilms().films
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getNowPlayingFilms(): List<Film> {
        return try {
            apiService.getNowPlayingFilms().films
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getTrendingFilms(): List<Film> {
        return try {
            apiService.getTrendingFilms().films
        } catch (e: Exception) {
            emptyList()
        }
    }
}
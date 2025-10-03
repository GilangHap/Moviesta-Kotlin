package com.unsoed.moviesta.repository

import com.unsoed.moviesta.model.Film
import com.unsoed.moviesta.model.FilmDetail
import com.unsoed.moviesta.model.Cast
import com.unsoed.moviesta.model.Genre
import com.unsoed.moviesta.model.Actor
import com.unsoed.moviesta.model.ActorDetail
import com.unsoed.moviesta.network.TmdbApiService

class FilmRepository(private val apiService: TmdbApiService) {

    suspend fun getPopularFilms(): List<Film> {
        return try {
            val allFilms = mutableListOf<Film>()
            // Load 3 pages to get 60 films (20 per page)
            for (page in 1..3) {
                val films = apiService.getPopularFilms(page = page).films
                allFilms.addAll(films)
            }
            allFilms
        } catch (e: Exception) {
            // Fallback to single page if error
            try {
                apiService.getPopularFilms().films
            } catch (e2: Exception) {
                emptyList()
            }
        }
    }

    suspend fun searchFilms(query: String): List<Film> {
        return try {
            val allFilms = mutableListOf<Film>()
            // Load 2 pages for search to get 40 films (20 per page)
            for (page in 1..2) {
                val films = apiService.searchFilms(query = query, page = page).films
                allFilms.addAll(films)
                // Stop if we get less than 20 films (end of results)
                if (films.size < 20) break
            }
            allFilms
        } catch (e: Exception) {
            // Fallback to single page if error
            try {
                apiService.searchFilms(query = query).films
            } catch (e2: Exception) {
                emptyList()
            }
        }
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
            val allFilms = mutableListOf<Film>()
            // Load 3 pages to get 60 films (20 per page)
            for (page in 1..3) {
                val films = apiService.getMoviesByGenre(genreId = genreId, page = page).films
                allFilms.addAll(films)
            }
            allFilms
        } catch (e: Exception) {
            // Fallback to single page if error
            try {
                apiService.getMoviesByGenre(genreId = genreId).films
            } catch (e2: Exception) {
                emptyList()
            }
        }
    }

    suspend fun getTopRatedFilms(): List<Film> {
        return try {
            val allFilms = mutableListOf<Film>()
            // Load 3 pages to get 60 films (20 per page)
            for (page in 1..3) {
                val films = apiService.getTopRatedFilms(page = page).films
                allFilms.addAll(films)
            }
            allFilms
        } catch (e: Exception) {
            // Fallback to single page if error
            try {
                apiService.getTopRatedFilms().films
            } catch (e2: Exception) {
                emptyList()
            }
        }
    }

    suspend fun getUpcomingFilms(): List<Film> {
        return try {
            val allFilms = mutableListOf<Film>()
            // Load 3 pages to get 60 films (20 per page)
            for (page in 1..3) {
                val films = apiService.getUpcomingFilms(page = page).films
                allFilms.addAll(films)
            }
            allFilms
        } catch (e: Exception) {
            // Fallback to single page if error
            try {
                apiService.getUpcomingFilms().films
            } catch (e2: Exception) {
                emptyList()
            }
        }
    }

    suspend fun getNowPlayingFilms(): List<Film> {
        return try {
            val allFilms = mutableListOf<Film>()
            // Load 3 pages to get 60 films (20 per page)
            for (page in 1..3) {
                val films = apiService.getNowPlayingFilms(page = page).films
                allFilms.addAll(films)
            }
            allFilms
        } catch (e: Exception) {
            // Fallback to single page if error
            try {
                apiService.getNowPlayingFilms().films
            } catch (e2: Exception) {
                emptyList()
            }
        }
    }

    suspend fun getTrendingFilms(): List<Film> {
        return try {
            apiService.getTrendingFilms().films
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Actor methods
    suspend fun getPopularActors(): List<Actor> {
        return try {
            val allActors = mutableListOf<Actor>()
            // Load 3 pages to get 60 actors (20 per page)
            for (page in 1..3) {
                val actors = apiService.getPopularActors(page = page).actors
                allActors.addAll(actors)
            }
            allActors
        } catch (e: Exception) {
            // Fallback to single page if error
            try {
                apiService.getPopularActors().actors
            } catch (e2: Exception) {
                emptyList()
            }
        }
    }

    suspend fun searchActors(query: String): List<Actor> {
        return try {
            val allActors = mutableListOf<Actor>()
            // Load 2 pages for search to get 40 actors (20 per page)
            for (page in 1..2) {
                val actors = apiService.searchActors(query = query, page = page).actors
                allActors.addAll(actors)
                // Stop if we get less than 20 actors (end of results)
                if (actors.size < 20) break
            }
            allActors
        } catch (e: Exception) {
            // Fallback to single page if error
            try {
                apiService.searchActors(query = query).actors
            } catch (e2: Exception) {
                emptyList()
            }
        }
    }

    suspend fun getActorDetail(actorId: Int): ActorDetail {
        return apiService.getActorDetail(actorId)
    }

    suspend fun getActorMovies(actorId: Int): List<Film> {
        return try {
            apiService.getActorMovieCredits(actorId).films
        } catch (e: Exception) {
            emptyList()
        }
    }
}
package com.unsoed.moviesta.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unsoed.moviesta.model.Film
import com.unsoed.moviesta.model.FilmDetail
import com.unsoed.moviesta.model.Actor
import com.unsoed.moviesta.model.Genre
import com.unsoed.moviesta.view.Category
import com.unsoed.moviesta.view.CategoryType
import com.unsoed.moviesta.repository.FilmRepository
import kotlinx.coroutines.launch

class FilmViewModel(private val repository: FilmRepository) : ViewModel() {

    private val _films = MutableLiveData<List<Film>>()
    val films: LiveData<List<Film>> = _films

    private val _upcomingFilms = MutableLiveData<List<Film>>()
    val upcomingFilms: LiveData<List<Film>> = _upcomingFilms

    private val _popularFilms = MutableLiveData<List<Film>>()
    val popularFilms: LiveData<List<Film>> = _popularFilms

    private val _genreBasedFilms = MutableLiveData<List<Film>>()
    val genreBasedFilms: LiveData<List<Film>> = _genreBasedFilms

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    private val _genres = MutableLiveData<List<Genre>>()
    val genres: LiveData<List<Genre>> = _genres

    private val _actors = MutableLiveData<List<Actor>>()
    val actors: LiveData<List<Actor>> = _actors

    private val _filmDetail = MutableLiveData<FilmDetail>()
    val filmDetail: LiveData<FilmDetail> = _filmDetail

    private val _categories = MutableLiveData<List<Category>>()
    val categories: LiveData<List<Category>> = _categories

    private val _selectedCategory = MutableLiveData<Category>()
    val selectedCategory: LiveData<Category> = _selectedCategory

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        loadPopularFilms()
        loadUpcomingFilms()
        loadGenres()
        setupCategories()
    }

    fun loadPopularFilms() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = repository.getPopularFilms()
                _popularFilms.value = result
                _films.value = result
            } catch (e: Exception) {
                _error.value = e.message ?: "An error occurred"
                _popularFilms.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadTopRatedFilms() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = repository.getTopRatedFilms()
                _films.value = result
            } catch (e: Exception) {
                _error.value = e.message ?: "An error occurred"
                _films.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadUpcomingFilms() {
        viewModelScope.launch {
            try {
                val result = repository.getUpcomingFilms()
                _upcomingFilms.value = result
            } catch (e: Exception) {
                _error.value = e.message ?: "An error occurred"
                _upcomingFilms.value = emptyList()
            }
        }
    }

    fun getMoviesByGenres(genreIds: List<Int>) {
        viewModelScope.launch {
            try {
                // Get movies from first genre (can be expanded to combine multiple genres)
                if (genreIds.isNotEmpty()) {
                    val result = repository.getMoviesByGenre(genreIds.first())
                    _genreBasedFilms.value = result
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "An error occurred"
                _genreBasedFilms.value = emptyList()
            }
        }
    }

    fun loadNowPlayingFilms() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = repository.getNowPlayingFilms()
                _films.value = result
            } catch (e: Exception) {
                _error.value = e.message ?: "An error occurred"
                _films.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadTrendingFilms() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = repository.getTrendingFilms()
                _films.value = result
            } catch (e: Exception) {
                _error.value = e.message ?: "An error occurred"
                _films.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun searchFilms(query: String) {
        if (query.isBlank()) {
            loadPopularFilms()
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = repository.searchFilms(query)
                _films.value = result
            } catch (e: Exception) {
                _error.value = e.message ?: "An error occurred"
                _films.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun setupCategories() {
        val defaultCategories = listOf(
            Category("all", "Semua", CategoryType.ALL, true),
            Category("popular", "Populer", CategoryType.POPULAR),
            Category("top_rated", "Rating Tertinggi", CategoryType.TOP_RATED),
            Category("upcoming", "Akan Datang", CategoryType.UPCOMING),
            Category("now_playing", "Sedang Tayang", CategoryType.NOW_PLAYING)
        )
        _categories.value = defaultCategories
        _selectedCategory.value = defaultCategories.first()
    }

    fun selectCategory(category: Category) {
        val currentCategories = _categories.value ?: return
        val updatedCategories = currentCategories.map { 
            it.copy(isSelected = it.id == category.id) 
        }
        _categories.value = updatedCategories
        _selectedCategory.value = category
        
        // Load films based on category
        when (category.type) {
            CategoryType.ALL -> loadPopularFilms()
            CategoryType.POPULAR -> loadPopularFilms()
            CategoryType.TOP_RATED -> loadTopRatedFilms()
            CategoryType.UPCOMING -> loadUpcomingFilms()
            CategoryType.NOW_PLAYING -> loadNowPlayingFilms()
            CategoryType.GENRE -> {
                // For genre categories, load by genre ID
                val genreId = category.id.toIntOrNull()
                if (genreId != null) {
                    loadMoviesByGenre(genreId)
                }
            }
        }
    }

    fun loadMoviesByGenre(genreId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = repository.getMoviesByGenre(genreId)
                _films.value = result
            } catch (e: Exception) {
                _error.value = e.message ?: "An error occurred"
                _films.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addGenreCategories() {
        val genreList = _genres.value ?: return
        val currentCategories = _categories.value?.toMutableList() ?: mutableListOf()
        
        // Add genre categories
        val genreCategories = genreList.map { genre ->
            Category(
                id = genre.id.toString(),
                name = genre.name,
                type = CategoryType.GENRE
            )
        }
        
        currentCategories.addAll(genreCategories)
        _categories.value = currentCategories
    }

    fun loadPopularActors() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = repository.getPopularActors()
                _actors.value = result
            } catch (e: Exception) {
                _error.value = e.message ?: "An error occurred"
                _actors.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getFilmDetail(filmId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = repository.getFilmDetail(filmId)
                _filmDetail.value = result
            } catch (e: Exception) {
                _error.value = e.message ?: "An error occurred"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadGenres() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = repository.getGenres()
                _genres.value = result
                addGenreCategories()
            } catch (e: Exception) {
                _error.value = e.message ?: "An error occurred"
                _genres.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }
}
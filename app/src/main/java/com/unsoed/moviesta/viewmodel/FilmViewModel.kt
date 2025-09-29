package com.unsoed.moviesta.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unsoed.moviesta.model.Film
import com.unsoed.moviesta.repository.FilmRepository
import kotlinx.coroutines.launch

class FilmViewModel(private val repository: FilmRepository) : ViewModel() {

    private val _films = MutableLiveData<List<Film>>()
    val films: LiveData<List<Film>> = _films

    init {
        loadPopularFilms()
    }

    fun loadPopularFilms() {
        viewModelScope.launch {
            try {
                val result = repository.getPopularFilms()
                _films.value = result
            } catch (e: Exception) {
                // Log atau tampilkan Toast Error di sini
                _films.value = emptyList()
            }
        }
    }

    fun searchFilms(query: String) {
        if (query.isBlank()) {
            loadPopularFilms() // Kembali ke populer jika query kosong
            return
        }

        viewModelScope.launch {
            try {
                val result = repository.searchFilms(query)
                _films.value = result
            } catch (e: Exception) {
                _films.value = emptyList()
            }
        }
    }
}
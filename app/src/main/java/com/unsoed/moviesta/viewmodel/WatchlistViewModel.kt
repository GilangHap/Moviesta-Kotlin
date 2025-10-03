package com.unsoed.moviesta.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.unsoed.moviesta.database.MoviestaDatabase
import com.unsoed.moviesta.model.WatchlistItem
import com.unsoed.moviesta.model.Film
import com.unsoed.moviesta.model.FilmDetail
import com.unsoed.moviesta.repository.WatchlistRepository
import kotlinx.coroutines.launch

class WatchlistViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository: WatchlistRepository
    
    // LiveData for watchlist items
    val watchlistItems: LiveData<List<WatchlistItem>>
    val watchlistCount: LiveData<Int>
    
    // Loading and error states
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage
    
    // Success messages
    private val _successMessage = MutableLiveData<String?>()
    val successMessage: LiveData<String?> = _successMessage
    
    init {
        val watchlistDao = MoviestaDatabase.getDatabase(application).watchlistDao()
        repository = WatchlistRepository(watchlistDao)
        watchlistItems = repository.getAllWatchlistItems()
        watchlistCount = repository.getWatchlistCountLiveData()
    }
    
    // Check if film is in watchlist
    fun isInWatchlist(filmId: Int): LiveData<Boolean> {
        return repository.isInWatchlistLiveData(filmId)
    }
    
    // Add film to watchlist
    fun addToWatchlist(film: Film) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                repository.addToWatchlist(film)
                _successMessage.value = "${film.safeTitle} ditambahkan ke Watchlist"
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = "Gagal menambahkan ke watchlist: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    // Add film detail to watchlist
    fun addToWatchlist(filmDetail: FilmDetail) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                repository.addToWatchlist(filmDetail)
                _successMessage.value = "${filmDetail.title} ditambahkan ke Watchlist"
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = "Gagal menambahkan ke watchlist: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    // Remove from watchlist
    fun removeFromWatchlist(filmId: Int, filmTitle: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                repository.removeFromWatchlist(filmId)
                _successMessage.value = "$filmTitle dihapus dari Watchlist"
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = "Gagal menghapus dari watchlist: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    // Remove watchlist item
    fun removeFromWatchlist(watchlistItem: WatchlistItem) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                repository.removeFromWatchlist(watchlistItem)
                _successMessage.value = "${watchlistItem.title} dihapus dari Watchlist"
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = "Gagal menghapus dari watchlist: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    // Toggle watchlist status
    fun toggleWatchlist(film: Film) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val isAdded = repository.toggleWatchlist(film)
                _successMessage.value = if (isAdded) {
                    "${film.safeTitle} ditambahkan ke Watchlist"
                } else {
                    "${film.safeTitle} dihapus dari Watchlist"
                }
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = "Gagal mengubah status watchlist: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    // Toggle watchlist status for FilmDetail
    fun toggleWatchlist(filmDetail: FilmDetail) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val isAdded = repository.toggleWatchlist(filmDetail)
                _successMessage.value = if (isAdded) {
                    "${filmDetail.title} ditambahkan ke Watchlist"
                } else {
                    "${filmDetail.title} dihapus dari Watchlist"
                }
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = "Gagal mengubah status watchlist: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    // Clear all watchlist
    fun clearWatchlist() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                repository.clearWatchlist()
                _successMessage.value = "Watchlist telah dibersihkan"
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = "Gagal membersihkan watchlist: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    // Clear messages
    fun clearMessages() {
        _errorMessage.value = null
        _successMessage.value = null
    }
}
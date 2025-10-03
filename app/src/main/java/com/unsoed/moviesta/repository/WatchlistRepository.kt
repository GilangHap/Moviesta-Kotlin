package com.unsoed.moviesta.repository

import androidx.lifecycle.LiveData
import com.unsoed.moviesta.database.WatchlistDao
import com.unsoed.moviesta.model.WatchlistItem
import com.unsoed.moviesta.model.Film
import com.unsoed.moviesta.model.FilmDetail
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WatchlistRepository(private val watchlistDao: WatchlistDao) {
    
    // Get all watchlist items as LiveData
    fun getAllWatchlistItems(): LiveData<List<WatchlistItem>> {
        return watchlistDao.getAllWatchlistItems()
    }
    
    // Get all watchlist items synchronously
    suspend fun getAllWatchlistItemsSync(): List<WatchlistItem> = withContext(Dispatchers.IO) {
        watchlistDao.getAllWatchlistItemsSync()
    }
    
    // Check if film is in watchlist
    suspend fun isInWatchlist(filmId: Int): Boolean = withContext(Dispatchers.IO) {
        watchlistDao.isInWatchlist(filmId)
    }
    
    // Check if film is in watchlist as LiveData
    fun isInWatchlistLiveData(filmId: Int): LiveData<Boolean> {
        return watchlistDao.isInWatchlistLiveData(filmId)
    }
    
    // Add film to watchlist
    suspend fun addToWatchlist(film: Film) = withContext(Dispatchers.IO) {
        val watchlistItem = WatchlistItem.fromFilm(film)
        watchlistDao.insertWatchlistItem(watchlistItem)
    }
    
    // Add film detail to watchlist
    suspend fun addToWatchlist(filmDetail: FilmDetail) = withContext(Dispatchers.IO) {
        val watchlistItem = WatchlistItem.fromFilmDetail(filmDetail)
        watchlistDao.insertWatchlistItem(watchlistItem)
    }
    
    // Add watchlist item directly
    suspend fun addToWatchlist(watchlistItem: WatchlistItem) = withContext(Dispatchers.IO) {
        watchlistDao.insertWatchlistItem(watchlistItem)
    }
    
    // Remove from watchlist by film ID
    suspend fun removeFromWatchlist(filmId: Int) = withContext(Dispatchers.IO) {
        watchlistDao.deleteWatchlistItemById(filmId)
    }
    
    // Remove from watchlist by WatchlistItem
    suspend fun removeFromWatchlist(watchlistItem: WatchlistItem) = withContext(Dispatchers.IO) {
        watchlistDao.deleteWatchlistItem(watchlistItem)
    }
    
    // Toggle watchlist status
    suspend fun toggleWatchlist(film: Film): Boolean = withContext(Dispatchers.IO) {
        if (isInWatchlist(film.id)) {
            removeFromWatchlist(film.id)
            false // Removed from watchlist
        } else {
            addToWatchlist(film)
            true // Added to watchlist
        }
    }
    
    // Toggle watchlist status for FilmDetail
    suspend fun toggleWatchlist(filmDetail: FilmDetail): Boolean = withContext(Dispatchers.IO) {
        if (isInWatchlist(filmDetail.id)) {
            removeFromWatchlist(filmDetail.id)
            false // Removed from watchlist
        } else {
            addToWatchlist(filmDetail)
            true // Added to watchlist
        }
    }
    
    // Clear all watchlist
    suspend fun clearWatchlist() = withContext(Dispatchers.IO) {
        watchlistDao.clearWatchlist()
    }
    
    // Get watchlist count
    suspend fun getWatchlistCount(): Int = withContext(Dispatchers.IO) {
        watchlistDao.getWatchlistCount()
    }
    
    // Get watchlist count as LiveData
    fun getWatchlistCountLiveData(): LiveData<Int> {
        return watchlistDao.getWatchlistCountLiveData()
    }
}
package com.unsoed.moviesta.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.unsoed.moviesta.model.WatchlistItem

@Dao
interface WatchlistDao {
    
    @Query("SELECT * FROM watchlist ORDER BY addedDate DESC")
    fun getAllWatchlistItems(): LiveData<List<WatchlistItem>>
    
    @Query("SELECT * FROM watchlist ORDER BY addedDate DESC")
    suspend fun getAllWatchlistItemsSync(): List<WatchlistItem>
    
    @Query("SELECT * FROM watchlist WHERE id = :filmId")
    suspend fun getWatchlistItem(filmId: Int): WatchlistItem?
    
    @Query("SELECT EXISTS(SELECT * FROM watchlist WHERE id = :filmId)")
    suspend fun isInWatchlist(filmId: Int): Boolean
    
    @Query("SELECT EXISTS(SELECT * FROM watchlist WHERE id = :filmId)")
    fun isInWatchlistLiveData(filmId: Int): LiveData<Boolean>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWatchlistItem(item: WatchlistItem)
    
    @Delete
    suspend fun deleteWatchlistItem(item: WatchlistItem)
    
    @Query("DELETE FROM watchlist WHERE id = :filmId")
    suspend fun deleteWatchlistItemById(filmId: Int)
    
    @Query("DELETE FROM watchlist")
    suspend fun clearWatchlist()
    
    @Query("SELECT COUNT(*) FROM watchlist")
    suspend fun getWatchlistCount(): Int
    
    @Query("SELECT COUNT(*) FROM watchlist")
    fun getWatchlistCountLiveData(): LiveData<Int>
}
package com.unsoed.moviesta.database

import android.content.Context
import androidx.room.*
import com.unsoed.moviesta.model.WatchlistItem

@Database(
    entities = [WatchlistItem::class],
    version = 1,
    exportSchema = false
)
abstract class MoviestaDatabase : RoomDatabase() {
    
    abstract fun watchlistDao(): WatchlistDao
    
    companion object {
        @Volatile
        private var INSTANCE: MoviestaDatabase? = null
        
        fun getDatabase(context: Context): MoviestaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MoviestaDatabase::class.java,
                    "moviesta_database"
                )
                .fallbackToDestructiveMigration() // For development only
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
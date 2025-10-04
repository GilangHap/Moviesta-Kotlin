package com.unsoed.moviesta

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.firebase.auth.FirebaseAuth
import com.unsoed.moviesta.adapter.WatchedMoviesAdapter
import com.unsoed.moviesta.base.BaseAuthActivity
import com.unsoed.moviesta.repository.UserPreferencesRepository
import com.unsoed.moviesta.repository.WatchedMoviesRepository
import kotlinx.coroutines.launch

class WatchedMoviesActivity : BaseAuthActivity() {
    
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressIndicator: CircularProgressIndicator
    private lateinit var adapter: WatchedMoviesAdapter
    private lateinit var repository: WatchedMoviesRepository
    
    companion object {
        private const val TAG = "WatchedMoviesActivity"
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_watched_movies)
        
        setupToolbar()
        setupRecyclerView()
        setupRepository()
        loadWatchedMovies()
    }
    
    private fun setupToolbar() {
        supportActionBar?.apply {
            title = "Watched Movies"
            setDisplayHomeAsUpEnabled(true)
        }
    }
    
    private fun setupRecyclerView() {
        recyclerView = findViewById(R.id.recycler_watched_movies)
        progressIndicator = findViewById(R.id.progress_indicator)
        
        adapter = WatchedMoviesAdapter(emptyList()) { watchedMovie ->
            // Handle click - navigate to detail
            // You can implement navigation to DetailActivity here
        }
        
        recyclerView.layoutManager = GridLayoutManager(this, 2)
        recyclerView.adapter = adapter
    }
    
    private fun setupRepository() {
        repository = WatchedMoviesRepository(
            auth = FirebaseAuth.getInstance(),
            userPreferencesRepository = UserPreferencesRepository()
        )
    }
    
    private fun loadWatchedMovies() {
        lifecycleScope.launch {
            try {
                progressIndicator.visibility = View.VISIBLE
                recyclerView.visibility = View.GONE
                
                Log.d(TAG, "Loading watched movies...")
                
                val result = repository.getUserWatchedMovies()
                if (result.isSuccess) {
                    val watchedMovies = result.getOrDefault(emptyList())
                    Log.d(TAG, "Loaded ${watchedMovies.size} watched movies")
                    
                    adapter.updateMovies(watchedMovies)
                    
                    if (watchedMovies.isEmpty()) {
                        // Show empty state
                        findViewById<View>(R.id.empty_state).visibility = View.VISIBLE
                        recyclerView.visibility = View.GONE
                    } else {
                        findViewById<View>(R.id.empty_state).visibility = View.GONE
                        recyclerView.visibility = View.VISIBLE
                    }
                } else {
                    Log.e(TAG, "Failed to load watched movies", result.exceptionOrNull())
                    // Show error state
                    findViewById<View>(R.id.error_state).visibility = View.VISIBLE
                    recyclerView.visibility = View.GONE
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception loading watched movies", e)
            } finally {
                progressIndicator.visibility = View.GONE
            }
        }
    }
    
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
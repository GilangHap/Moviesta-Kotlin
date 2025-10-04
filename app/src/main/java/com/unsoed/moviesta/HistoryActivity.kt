package com.unsoed.moviesta

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.unsoed.moviesta.adapter.WatchedHistoryAdapter
import com.unsoed.moviesta.model.Film
import com.unsoed.moviesta.model.WatchedMovieInfo
import com.unsoed.moviesta.repository.UserPreferencesRepository
import com.unsoed.moviesta.repository.WatchedMoviesRepository
import com.unsoed.moviesta.view.CustomBottomNavigation
import kotlinx.coroutines.launch

class HistoryActivity : AppCompatActivity() {

    private lateinit var toolbar: Toolbar
    private lateinit var recyclerView: RecyclerView
    private lateinit var layoutEmpty: View
    private lateinit var layoutLoading: View
    private lateinit var tvEmptyTitle: TextView
    private lateinit var tvEmptySubtitle: TextView
    private lateinit var ivEmpty: ImageView
    private lateinit var bottomNavigation: CustomBottomNavigation
    
    private lateinit var watchedHistoryAdapter: WatchedHistoryAdapter
    private lateinit var watchedMoviesRepository: WatchedMoviesRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        initViews()
        setupToolbar()
        setupRecyclerView()
        setupRepository()
        setupBottomNavigation()
        loadWatchedHistory()
    }

    private fun initViews() {
        toolbar = findViewById(R.id.toolbar_history)
        recyclerView = findViewById(R.id.rv_watched_history)
        layoutEmpty = findViewById(R.id.layout_empty_history)
        layoutLoading = findViewById(R.id.layout_loading_history)
        tvEmptyTitle = findViewById(R.id.tv_empty_title)
        tvEmptySubtitle = findViewById(R.id.tv_empty_subtitle)
        ivEmpty = findViewById(R.id.iv_empty_history)
        bottomNavigation = findViewById(R.id.bottom_navigation)
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = "Watch History"
        }
        
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    private fun setupRecyclerView() {
        watchedHistoryAdapter = WatchedHistoryAdapter { watchedMovie ->
            // Navigate to detail activity
            val intent = Intent(this, DetailActivity::class.java)
            // Create Film object from WatchedMovieInfo
            val film = Film(
                id = watchedMovie.movieId,
                title = watchedMovie.title,
                sinopsis = "", // overview is named sinopsis in Film model
                posterPath = watchedMovie.posterUrl,
                voteAverage = watchedMovie.rating,
                releaseDate = watchedMovie.releaseYear
            )
            intent.putExtra(DetailActivity.EXTRA_FILM, film)
            startActivity(intent)
        }
        
        recyclerView.adapter = watchedHistoryAdapter
        recyclerView.layoutManager = GridLayoutManager(this, 2)
    }

    private fun setupRepository() {
        watchedMoviesRepository = WatchedMoviesRepository(
            auth = FirebaseAuth.getInstance(),
            userPreferencesRepository = UserPreferencesRepository()
        )
    }

    private fun loadWatchedHistory() {
        showLoading()
        
        lifecycleScope.launch {
            try {
                val result = watchedMoviesRepository.getUserWatchedMovies()
                
                if (result.isSuccess) {
                    val watchedMovies = result.getOrNull() ?: emptyList()
                    if (watchedMovies.isNotEmpty()) {
                        showHistory(watchedMovies.sortedByDescending { it.watchedDate })
                    } else {
                        showEmpty()
                    }
                } else {
                    showEmpty()
                }
                
            } catch (e: Exception) {
                showEmpty()
            }
        }
    }

    private fun showLoading() {
        layoutLoading.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
        layoutEmpty.visibility = View.GONE
    }

    private fun showHistory(watchedMovies: List<WatchedMovieInfo>) {
        layoutLoading.visibility = View.GONE
        recyclerView.visibility = View.VISIBLE
        layoutEmpty.visibility = View.GONE
        
        watchedHistoryAdapter.updateWatchedMovies(watchedMovies)
    }

    private fun showEmpty() {
        layoutLoading.visibility = View.GONE
        recyclerView.visibility = View.GONE
        layoutEmpty.visibility = View.VISIBLE
        
        tvEmptyTitle.text = "No Watch History"
        tvEmptySubtitle.text = "You haven't watched any movies yet.\nStart exploring and watching movies!"
    }

    override fun onResume() {
        super.onResume()
        // Refresh data when returning to this activity
        loadWatchedHistory()
    }
    
    private fun setupBottomNavigation() {
        bottomNavigation.setOnTabSelectedListener { tab ->
            when (tab) {
                CustomBottomNavigation.NavigationTab.GENRE -> {
                    navigateToGenre()
                }
                CustomBottomNavigation.NavigationTab.ACTOR -> {
                    navigateToActor()
                }
                CustomBottomNavigation.NavigationTab.HOME -> {
                    navigateToHome()
                }
                CustomBottomNavigation.NavigationTab.HISTORY -> {
                    // Already on history, do nothing
                }
                CustomBottomNavigation.NavigationTab.PROFILE -> {
                    navigateToProfile()
                }
            }
        }
        
        // Set initial selection to HISTORY
        bottomNavigation.selectTab(CustomBottomNavigation.NavigationTab.HISTORY)
    }
    
    private fun navigateToGenre() {
        val intent = Intent(this, GenreActivity::class.java)
        startActivity(intent)
        finish()
    }
    
    private fun navigateToActor() {
        val intent = Intent(this, ActorActivity::class.java)
        startActivity(intent)
        finish()
    }
    
    private fun navigateToHome() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
    
    private fun navigateToProfile() {
        val intent = Intent(this, ProfileActivity::class.java)
        startActivity(intent)
        finish()
    }
}
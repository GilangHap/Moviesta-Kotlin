package com.unsoed.moviesta

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.LinearLayout
import android.widget.ImageView
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.unsoed.moviesta.base.BaseAuthActivity
import com.unsoed.moviesta.network.RetrofitClient
import com.unsoed.moviesta.repository.FilmRepository
import com.unsoed.moviesta.repository.UserPreferencesRepository
import com.unsoed.moviesta.viewmodel.FilmViewModel
import com.unsoed.moviesta.viewmodel.FilmViewModelFactory
import com.unsoed.moviesta.adapter.HorizontalMovieAdapter
import kotlinx.coroutines.launch
import com.unsoed.moviesta.view.CustomBottomNavigation
import com.unsoed.moviesta.model.Film

class MainActivity : BaseAuthActivity() {

    private lateinit var filmViewModel: FilmViewModel
    private lateinit var userPreferencesRepository: UserPreferencesRepository
    
    // Adapters
    private lateinit var upcomingAdapter: HorizontalMovieAdapter
    private lateinit var recommendationsAdapter: HorizontalMovieAdapter
    private lateinit var trendingAdapter: HorizontalMovieAdapter
    
    // RecyclerViews
    private lateinit var recyclerViewUpcoming: RecyclerView
    private lateinit var recyclerViewRecommendations: RecyclerView
    private lateinit var recyclerViewTrending: RecyclerView
    
    // Views
    private lateinit var bottomNavigation: CustomBottomNavigation
    private lateinit var layoutLoading: LinearLayout
    private lateinit var tvRecommendationsTitle: TextView
    
    // Action views
    private lateinit var tvSearchHint: TextView
    private lateinit var ivFilter: ImageView
    private lateinit var ivMenu: ImageView
    
    // User preferences
    private var userGenres: List<Int> = emptyList()
    
    private var currentQuery = ""
    private var isSearchMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize views
        initViews()

        // Setup Repository and ViewModel
        val repository = FilmRepository(RetrofitClient.instance)
        val viewModelFactory = FilmViewModelFactory(repository)
        filmViewModel = ViewModelProvider(this, viewModelFactory)[FilmViewModel::class.java]

        // Initialize User Preferences Repository
        userPreferencesRepository = UserPreferencesRepository()

        // Setup RecyclerViews
        setupRecyclerViews()

        // Setup click listeners
        setupClickListeners()

        // Observe data from ViewModel
        observeViewModel()
        
        // Setup Bottom Navigation
        setupBottomNavigation()

        // Load user preferences and initial data
        loadUserPreferencesAndData()
    }

    private fun initViews() {
        recyclerViewUpcoming = findViewById(R.id.recycler_view_upcoming)
        recyclerViewRecommendations = findViewById(R.id.recycler_view_recommendations)
        recyclerViewTrending = findViewById(R.id.recycler_view_trending)
        bottomNavigation = findViewById(R.id.bottom_navigation)
        layoutLoading = findViewById(R.id.layout_loading)
        tvRecommendationsTitle = findViewById(R.id.tv_recommendations_title)
        tvSearchHint = findViewById(R.id.tv_search_hint)
        ivFilter = findViewById(R.id.iv_filter)
        ivMenu = findViewById(R.id.iv_menu)
    }

    private fun setupRecyclerViews() {
        // Setup Upcoming Movies
        upcomingAdapter = HorizontalMovieAdapter { movie ->
            openMovieDetail(movie)
        }
        recyclerViewUpcoming.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recyclerViewUpcoming.adapter = upcomingAdapter

        // Setup Recommendations
        recommendationsAdapter = HorizontalMovieAdapter { movie ->
            openMovieDetail(movie)
        }
        recyclerViewRecommendations.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recyclerViewRecommendations.adapter = recommendationsAdapter

        // Setup Trending
        trendingAdapter = HorizontalMovieAdapter { movie ->
            openMovieDetail(movie)
        }
        recyclerViewTrending.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recyclerViewTrending.adapter = trendingAdapter
    }

    private fun setupClickListeners() {
        // Search click
        tvSearchHint.setOnClickListener {
            // Open search activity or expand search
            openSearchActivity()
        }

        // Filter click
        ivFilter.setOnClickListener {
            // Open filter options
            openFilterOptions()
        }

        // Menu click
        ivMenu.setOnClickListener {
            // Open menu
            openMenu()
        }

        // See all buttons
        findViewById<ImageView>(R.id.iv_see_all_upcoming).setOnClickListener {
            openMovieListActivity("upcoming")
        }

        findViewById<ImageView>(R.id.iv_see_all_recommendations).setOnClickListener {
            openMovieListActivity("recommendations")
        }

        findViewById<ImageView>(R.id.iv_see_all_trending).setOnClickListener {
            openMovieListActivity("trending")
        }

        // Genre and Actor navigation buttons
        findViewById<MaterialButton>(R.id.btn_genre).setOnClickListener {
            navigateToGenre()
        }

        findViewById<MaterialButton>(R.id.btn_actor).setOnClickListener {
            navigateToActor()
        }

        findViewById<MaterialButton>(R.id.btn_watched).setOnClickListener {
            navigateToWatchedMovies()
        }
    }

    private fun loadUserPreferencesAndData() {
        showLoading()
        
        // Load user preferences first
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            lifecycleScope.launch {
                try {
                    val result = userPreferencesRepository.getUserPreferences(forceRefresh = false)
                    if (result.isSuccess) {
                        val preferences = result.getOrNull()
                        if (preferences != null && preferences.favoriteGenres.isNotEmpty()) {
                            userGenres = preferences.favoriteGenres
                            tvRecommendationsTitle.text = "Recommended for You"
                            loadRecommendationsBasedOnPreferences()
                        } else {
                            tvRecommendationsTitle.text = "Trending Movies"
                            loadTrendingAsRecommendations()
                        }
                    } else {
                        tvRecommendationsTitle.text = "Trending Movies"
                        loadTrendingAsRecommendations()
                    }
                } catch (e: Exception) {
                    tvRecommendationsTitle.text = "Trending Movies"
                    loadTrendingAsRecommendations()
                }
            }
        } else {
            tvRecommendationsTitle.text = "Trending Movies"
            loadTrendingAsRecommendations()
        }
        
        // Load other sections
        loadUpcomingMovies()
        loadTrendingMovies()
    }

    private fun loadUpcomingMovies() {
        filmViewModel.loadUpcomingFilms()
    }

    private fun loadTrendingMovies() {
        filmViewModel.loadPopularFilms()
    }

    private fun loadRecommendationsBasedOnPreferences() {
        if (userGenres.isNotEmpty()) {
            // Get movies from user's favorite genres
            filmViewModel.getMoviesByGenres(userGenres)
        } else {
            loadTrendingAsRecommendations()
        }
    }

    private fun loadTrendingAsRecommendations() {
        filmViewModel.loadPopularFilms()
    }

    private fun observeViewModel() {
        filmViewModel.films.observe(this) { films ->
            hideLoading()
            updateMovieAdapters(films)
        }

        filmViewModel.upcomingFilms.observe(this) { films ->
            upcomingAdapter.updateMovies(films)
        }

        filmViewModel.popularFilms.observe(this) { films ->
            if (userGenres.isEmpty()) {
                // Use trending movies as recommendations if no user preferences
                recommendationsAdapter.updateMovies(films)
            }
            trendingAdapter.updateMovies(films)
        }

        filmViewModel.genreBasedFilms.observe(this) { films ->
            if (userGenres.isNotEmpty()) {
                // Use genre-based movies as recommendations
                recommendationsAdapter.updateMovies(films)
            }
        }

        filmViewModel.isLoading.observe(this) { loading ->
            if (loading) {
                showLoading()
            } else {
                hideLoading()
            }
        }

        filmViewModel.error.observe(this) { errorMessage ->
            hideLoading()
            // Handle error - could show a toast or retry option
        }
    }

    private fun updateMovieAdapters(films: List<Film>) {
        // This method can be used for general film updates if needed
        // Currently, specific adapters are updated in their respective observers
    }

    private fun openMovieDetail(movie: Film) {
        val intent = Intent(this, DetailActivity::class.java)
        intent.putExtra(DetailActivity.EXTRA_FILM, movie)
        startActivity(intent)
    }

    private fun openSearchActivity() {
        // TODO: Create SearchActivity
        android.widget.Toast.makeText(this, "Search feature coming soon!", android.widget.Toast.LENGTH_SHORT).show()
    }

    private fun openFilterOptions() {
        // Open filter dialog or activity
        // For now, just a placeholder
    }

    private fun openMenu() {
        // Open navigation drawer or menu
        // For now, just a placeholder
    }

    private fun openMovieListActivity(type: String) {
        // TODO: Create MovieListActivity
        android.widget.Toast.makeText(this, "Movie list ($type) coming soon!", android.widget.Toast.LENGTH_SHORT).show()
    }
    private fun showLoading() {
        layoutLoading.visibility = View.VISIBLE
    }

    private fun hideLoading() {
        layoutLoading.visibility = View.GONE
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
                    navigateToHistory()
                }
                CustomBottomNavigation.NavigationTab.PROFILE -> {
                    navigateToProfile()
                }
            }
        }
        
        // Set initial selection
        bottomNavigation.selectTab(CustomBottomNavigation.NavigationTab.HOME)
    }

    override fun onResume() {
        super.onResume()
        // Refresh data when returning to this activity
        loadUserPreferencesAndData()
    }
    
    private fun navigateToGenre() {
        val intent = Intent(this, GenreActivity::class.java)
        startActivity(intent)
    }
    
    private fun navigateToActor() {
        val intent = Intent(this, ActorActivity::class.java)
        startActivity(intent)
    }
    
    private fun navigateToWatchedMovies() {
        val intent = Intent(this, WatchedMoviesActivity::class.java)
        startActivity(intent)
    }
    
    private fun navigateToHome() {
        // Already on home, refresh data
        loadUserPreferencesAndData()
    }
    
    private fun navigateToHistory() {
        // TODO: Create HistoryActivity
        android.widget.Toast.makeText(this, "History page coming soon!", android.widget.Toast.LENGTH_SHORT).show()
    }
    
    private fun navigateToProfile() {
        // TODO: Create ProfileActivity
        android.widget.Toast.makeText(this, "Profile page coming soon!", android.widget.Toast.LENGTH_SHORT).show()
    }
}
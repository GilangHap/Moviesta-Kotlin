package com.unsoed.moviesta

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import com.google.android.material.appbar.MaterialToolbar
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.unsoed.moviesta.network.RetrofitClient
import com.unsoed.moviesta.repository.FilmRepository
import com.unsoed.moviesta.viewmodel.FilmViewModel
import com.unsoed.moviesta.viewmodel.FilmViewModelFactory
import com.unsoed.moviesta.view.FilmAdapter
import com.unsoed.moviesta.view.CategoryAdapter
import com.unsoed.moviesta.view.Category
import com.unsoed.moviesta.view.CustomBottomNavigation

class MainActivity : AppCompatActivity() {

    private lateinit var filmViewModel: FilmViewModel
    private lateinit var filmAdapter: FilmAdapter
    private lateinit var categoryAdapter: CategoryAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var categoriesRecyclerView: RecyclerView
    private lateinit var searchView: SearchView
    private lateinit var bottomNavigation: CustomBottomNavigation
    
    // Empty state views
    private lateinit var layoutEmptyState: LinearLayout
    private lateinit var layoutLoading: LinearLayout
    private lateinit var tvSectionTitle: TextView
    private lateinit var tvEmptyTitle: TextView
    private lateinit var tvEmptyMessage: TextView
    private lateinit var btnRetry: MaterialButton
    
    // Quick action buttons
    private lateinit var btnTrending: MaterialButton
    private lateinit var btnTopRated: MaterialButton
    private lateinit var btnUpcoming: MaterialButton
    
    private var currentQuery = ""
    private var isSearchMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Setup Toolbar (without ActionBar)
        val toolbar: MaterialToolbar = findViewById(R.id.toolbar_main)
        // setSupportActionBar(toolbar) // Removed - using NoActionBar theme

        // Initialize views
        initViews()

        // Setup Repository and ViewModel
        val repository = FilmRepository(RetrofitClient.instance)
        val viewModelFactory = FilmViewModelFactory(repository)
        filmViewModel = ViewModelProvider(this, viewModelFactory)[FilmViewModel::class.java]

        // Setup RecyclerViews
        setupRecyclerViews()

        // Setup categories
        setupCategories()

        // Setup quick actions
        setupQuickActions()

        // Observe data from ViewModel
        observeViewModel()

        // Setup Search
        setupSearch()
        
        // Setup Bottom Navigation
        setupBottomNavigation()

        // Setup retry button
        btnRetry.setOnClickListener {
            searchView.setQuery("", false)
            currentQuery = ""
            isSearchMode = false
            showLoading()
            val popularCategory = filmViewModel.categories.value?.find { 
                it.type == com.unsoed.moviesta.view.CategoryType.ALL 
            } ?: filmViewModel.categories.value?.firstOrNull()
            popularCategory?.let { filmViewModel.selectCategory(it) }
        }

        // Load initial data
        showLoading()
        filmViewModel.loadPopularFilms()
    }

    private fun initViews() {
        recyclerView = findViewById(R.id.recycler_view_films)
        categoriesRecyclerView = findViewById(R.id.recycler_view_categories)
        searchView = findViewById(R.id.search_view)
        bottomNavigation = findViewById(R.id.bottom_navigation)
        layoutEmptyState = findViewById(R.id.layout_empty_state)
        layoutLoading = findViewById(R.id.layout_loading)
        tvSectionTitle = findViewById(R.id.tv_section_title)
        tvEmptyTitle = findViewById(R.id.tv_empty_title)
        tvEmptyMessage = findViewById(R.id.tv_empty_message)
        btnRetry = findViewById(R.id.btn_retry)
        btnTrending = findViewById(R.id.btn_trending)
        btnTopRated = findViewById(R.id.btn_top_rated)
        btnUpcoming = findViewById(R.id.btn_upcoming)
    }

    private fun setupRecyclerViews() {
        // Setup main film grid
        filmAdapter = FilmAdapter(emptyList())
        val gridLayoutManager = GridLayoutManager(this, 2)
        recyclerView.layoutManager = gridLayoutManager
        recyclerView.adapter = filmAdapter

        // Add item spacing if GridSpacingItemDecoration is available
        try {
            val spacing = resources.getDimensionPixelSize(R.dimen.grid_spacing)
            recyclerView.addItemDecoration(GridSpacingItemDecoration(2, spacing, true))
        } catch (e: Exception) {
            // GridSpacingItemDecoration not found, continue without spacing
        }

        // Add item animation
        recyclerView.itemAnimator = androidx.recyclerview.widget.DefaultItemAnimator()

        // Setup categories horizontal list
        categoriesRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        categoryAdapter = CategoryAdapter(emptyList()) { category ->
            handleCategorySelection(category)
        }
        categoriesRecyclerView.adapter = categoryAdapter
    }

    private fun setupCategories() {
        // Categories will be loaded from ViewModel
    }

    private fun setupQuickActions() {
        btnTrending.setOnClickListener {
            val trendingCategory = Category("trending", "Trending", com.unsoed.moviesta.view.CategoryType.POPULAR)
            handleCategorySelection(trendingCategory)
        }

        btnTopRated.setOnClickListener {
            val topRatedCategory = filmViewModel.categories.value?.find { 
                it.type == com.unsoed.moviesta.view.CategoryType.TOP_RATED 
            }
            topRatedCategory?.let { handleCategorySelection(it) }
        }

        btnUpcoming.setOnClickListener {
            val upcomingCategory = filmViewModel.categories.value?.find { 
                it.type == com.unsoed.moviesta.view.CategoryType.UPCOMING 
            }
            upcomingCategory?.let { handleCategorySelection(it) }
        }
    }

    private fun handleCategorySelection(category: Category) {
        searchView.setQuery("", false)
        currentQuery = ""
        isSearchMode = false
        showLoading()
        filmViewModel.selectCategory(category)
    }


    private fun observeViewModel() {
        filmViewModel.films.observe(this) { films ->
            hideLoading()
            if (films.isEmpty()) {
                showEmptyState()
            } else {
                showContent()
                filmAdapter.updateFilms(films)
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
            showEmptyState()
        }

        // Observe categories
        filmViewModel.categories.observe(this) { categories ->
            categoryAdapter.updateCategories(categories)
        }

        // Observe selected category
        filmViewModel.selectedCategory.observe(this) { category ->
            category?.let {
                tvSectionTitle.text = when (it.type) {
                    com.unsoed.moviesta.view.CategoryType.ALL -> "Semua Film"
                    com.unsoed.moviesta.view.CategoryType.POPULAR -> "Film Populer"
                    com.unsoed.moviesta.view.CategoryType.TOP_RATED -> "Film Rating Tertinggi"
                    com.unsoed.moviesta.view.CategoryType.UPCOMING -> "Film Akan Datang"
                    com.unsoed.moviesta.view.CategoryType.NOW_PLAYING -> "Film Sedang Tayang"
                    com.unsoed.moviesta.view.CategoryType.GENRE -> "Genre: ${it.name}"
                }
            }
        }

        // Observe genres and add them to categories
        filmViewModel.genres.observe(this) { genres ->
            if (genres.isNotEmpty()) {
                filmViewModel.addGenreCategories()
            }
        }
    }

    private fun setupSearch() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let {
                    currentQuery = it
                    if (it.isNotEmpty()) {
                        isSearchMode = true
                        tvSectionTitle.text = "Search Results for \"$it\""
                        showLoading()
                        filmViewModel.searchFilms(it)
                    } else {
                        isSearchMode = false
                        val selectedCategory = filmViewModel.selectedCategory.value
                        selectedCategory?.let { filmViewModel.selectCategory(it) }
                    }
                }
                searchView.clearFocus()
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText.isNullOrEmpty() && isSearchMode) {
                    isSearchMode = false
                    val selectedCategory = filmViewModel.selectedCategory.value
                    selectedCategory?.let { filmViewModel.selectCategory(it) }
                }
                return false
            }
        })

        searchView.setOnCloseListener {
            currentQuery = ""
            isSearchMode = false
            val selectedCategory = filmViewModel.selectedCategory.value
            selectedCategory?.let { filmViewModel.selectCategory(it) }
            false
        }
    }

    private fun showContent() {
        recyclerView.visibility = View.VISIBLE
        layoutEmptyState.visibility = View.GONE
        layoutLoading.visibility = View.GONE
    }

    private fun showEmptyState() {
        recyclerView.visibility = View.GONE
        layoutEmptyState.visibility = View.VISIBLE
        layoutLoading.visibility = View.GONE

        // Update empty state message based on search query
        if (currentQuery.isNotEmpty()) {
            tvEmptyTitle.text = "No Results Found"
            tvEmptyMessage.text = "No movies found for \"$currentQuery\".\nTry searching with different keywords."
            btnRetry.text = "Show Popular Movies"
            btnRetry.visibility = View.VISIBLE
        } else {
            tvEmptyTitle.text = "No Movies Available"
            tvEmptyMessage.text = "Unable to load movies at the moment.\nPlease check your internet connection."
            btnRetry.text = "Retry"
            btnRetry.visibility = View.VISIBLE
        }
    }

    private fun showLoading() {
        recyclerView.visibility = View.GONE
        layoutEmptyState.visibility = View.GONE
        layoutLoading.visibility = View.VISIBLE
    }

    private fun hideLoading() {
        layoutLoading.visibility = View.GONE
    }
    
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_watchlist -> {
                val intent = Intent(this, WatchlistActivity::class.java)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
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
    }
    
    private fun navigateToGenre() {
        val intent = Intent(this, GenreActivity::class.java)
        startActivity(intent)
    }
    
    private fun navigateToActor() {
        val intent = Intent(this, ActorActivity::class.java)
        startActivity(intent)
    }
    
    private fun navigateToHome() {
        // Already on home, refresh data
        searchView.setQuery("", false)
        currentQuery = ""
        isSearchMode = false
        showLoading()
        filmViewModel.loadPopularFilms()
    }
    
    private fun navigateToHistory() {
        // TODO: Implementasi navigasi ke halaman history
        android.widget.Toast.makeText(this, "History page coming soon!", android.widget.Toast.LENGTH_SHORT).show()
    }
    
    private fun navigateToProfile() {
        // TODO: Implementasi navigasi ke halaman profile
        android.widget.Toast.makeText(this, "Profile page coming soon!", android.widget.Toast.LENGTH_SHORT).show()
    }
}
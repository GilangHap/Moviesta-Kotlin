package com.unsoed.moviesta

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.LinearLayout
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.unsoed.moviesta.base.BaseAuthActivity
import com.unsoed.moviesta.model.Actor
import com.unsoed.moviesta.network.RetrofitClient
import com.unsoed.moviesta.repository.FilmRepository
import com.unsoed.moviesta.view.ActorAdapter
import com.unsoed.moviesta.view.CustomBottomNavigation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ActorActivity : BaseAuthActivity() {

    private lateinit var toolbar: MaterialToolbar
    private lateinit var searchView: SearchView
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var layoutEmptyState: LinearLayout
    private lateinit var bottomNavigation: CustomBottomNavigation
    private lateinit var actorAdapter: ActorAdapter
    private lateinit var repository: FilmRepository

    private var currentQuery = ""
    private var isSearchMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_actor)

        setupViews()
        setupRecyclerView()
        setupSearch()
        setupBottomNavigation()
        loadPopularActors()
    }

    private fun setupViews() {
        toolbar = findViewById(R.id.toolbar)
        searchView = findViewById(R.id.searchView)
        recyclerView = findViewById(R.id.recyclerViewActors)
        progressBar = findViewById(R.id.progressBar)
        layoutEmptyState = findViewById(R.id.layoutEmptyState)
        bottomNavigation = findViewById(R.id.bottom_navigation)

        // Setup toolbar
        toolbar.setNavigationOnClickListener { finish() }
        toolbar.title = "Popular Actors"

        // Initialize repository
        repository = FilmRepository(RetrofitClient.instance)
    }

    private fun setupRecyclerView() {
        actorAdapter = ActorAdapter(emptyList()) { actor ->
            // Navigate to FilmsByActorActivity when actor is clicked
            val intent = Intent(this, FilmsByActorActivity::class.java).apply {
                putExtra(FilmsByActorActivity.EXTRA_ACTOR, actor)
            }
            startActivity(intent)
        }

        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@ActorActivity)
            adapter = actorAdapter
        }
    }

    private fun setupSearch() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { searchActors(it) }
                searchView.clearFocus()
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                currentQuery = newText ?: ""
                if (newText.isNullOrEmpty()) {
                    isSearchMode = false
                    loadPopularActors()
                }
                return true
            }
        })

        searchView.setOnCloseListener {
            currentQuery = ""
            isSearchMode = false
            loadPopularActors()
            false
        }
    }

    private fun setupBottomNavigation() {
        bottomNavigation.selectTab(CustomBottomNavigation.NavigationTab.ACTOR)
        bottomNavigation.setOnTabSelectedListener { tab ->
            when (tab) {
                CustomBottomNavigation.NavigationTab.HOME -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
                CustomBottomNavigation.NavigationTab.GENRE -> {
                    startActivity(Intent(this, GenreActivity::class.java))
                    finish()
                }
                CustomBottomNavigation.NavigationTab.HISTORY -> {
                    startActivity(Intent(this, HistoryActivity::class.java))
                    finish()
                }
                CustomBottomNavigation.NavigationTab.PROFILE -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    finish()
                }
                else -> {}
            }
        }
    }

    private fun loadPopularActors() {
        showLoading(true)
        toolbar.title = "Popular Actors"

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val actors = repository.getPopularActors()
                
                withContext(Dispatchers.Main) {
                    showLoading(false)
                    if (actors.isNotEmpty()) {
                        actorAdapter.updateActors(actors)
                        showEmptyState(false)
                    } else {
                        showEmptyState(true)
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showLoading(false)
                    showEmptyState(true)
                    Log.e("ActorActivity", "Error loading popular actors: ${e.message}")
                }
            }
        }
    }

    private fun searchActors(query: String) {
        if (query.isBlank()) {
            loadPopularActors()
            return
        }

        showLoading(true)
        isSearchMode = true
        toolbar.title = "Search: $query"

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val actors = repository.searchActors(query)
                
                withContext(Dispatchers.Main) {
                    showLoading(false)
                    if (actors.isNotEmpty()) {
                        actorAdapter.updateActors(actors)
                        showEmptyState(false)
                    } else {
                        showEmptyState(true)
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showLoading(false)
                    showEmptyState(true)
                    Log.e("ActorActivity", "Error searching actors: ${e.message}")
                }
            }
        }
    }

    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
        recyclerView.visibility = if (show) View.GONE else View.VISIBLE
    }

    private fun showEmptyState(show: Boolean) {
        layoutEmptyState.visibility = if (show) View.VISIBLE else View.GONE
        recyclerView.visibility = if (show) View.GONE else View.VISIBLE
    }
}
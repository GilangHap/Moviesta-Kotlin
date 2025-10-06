package com.unsoed.moviesta

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.unsoed.moviesta.view.CustomBottomNavigation
import com.unsoed.moviesta.view.GenreAdapter
import com.unsoed.moviesta.model.Genre
import com.unsoed.moviesta.base.BaseAuthActivity

class GenreActivity : BaseAuthActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var genreAdapter: GenreAdapter
    private lateinit var bottomNavigation: CustomBottomNavigation

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_genre)

        setupViews()
        setupBottomNavigation()
        setupRecyclerView()
        loadGenres()
    }

    private fun setupViews() {
        recyclerView = findViewById(R.id.recyclerViewGenres)
        bottomNavigation = findViewById(R.id.bottom_navigation)

    }

    private fun setupBottomNavigation() {
        bottomNavigation.selectTab(CustomBottomNavigation.NavigationTab.GENRE)
        bottomNavigation.setOnTabSelectedListener { tab ->
            when (tab) {
                CustomBottomNavigation.NavigationTab.HOME -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
                CustomBottomNavigation.NavigationTab.ACTOR -> {
                    startActivity(Intent(this, ActorActivity::class.java))
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

    private fun setupRecyclerView() {
        genreAdapter = GenreAdapter(emptyList())
        recyclerView.apply {
            layoutManager = GridLayoutManager(this@GenreActivity, 2)
            adapter = genreAdapter
        }
    }

    private fun loadGenres() {
        // Sample genres
        val genres = listOf(
            Genre(28, "Action"),
            Genre(12, "Adventure"),
            Genre(16, "Animation"),
            Genre(35, "Comedy"),
            Genre(80, "Crime"),
            Genre(99, "Documentary"),
            Genre(18, "Drama"),
            Genre(10751, "Family"),
            Genre(14, "Fantasy"),
            Genre(36, "History"),
            Genre(27, "Horror"),
            Genre(10402, "Music"),
            Genre(9648, "Mystery"),
            Genre(10749, "Romance"),
            Genre(878, "Science Fiction"),
            Genre(53, "Thriller"),
            Genre(10752, "War"),
            Genre(37, "Western")
        )
        genreAdapter.updateGenres(genres)
    }

    // No longer need onSupportNavigateUp since we're handling navigation manually
}
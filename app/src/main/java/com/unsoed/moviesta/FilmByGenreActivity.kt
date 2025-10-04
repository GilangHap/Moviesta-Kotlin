package com.unsoed.moviesta

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.unsoed.moviesta.base.BaseAuthActivity
import com.unsoed.moviesta.model.Film
import com.unsoed.moviesta.model.Genre
import com.unsoed.moviesta.network.RetrofitClient
import com.unsoed.moviesta.repository.FilmRepository
import com.unsoed.moviesta.view.FilmAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FilmByGenreActivity : BaseAuthActivity() {

    companion object {
        const val EXTRA_GENRE_ID = "extra_genre_id"
        const val EXTRA_GENRE_NAME = "extra_genre_name"
    }

    private lateinit var toolbar: MaterialToolbar
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var layoutEmptyState: LinearLayout
    private lateinit var filmAdapter: FilmAdapter
    private lateinit var repository: FilmRepository

    private var genreId: Int = 0
    private var genreName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_film_by_genre)

        // Get genre data from intent
        genreId = intent.getIntExtra(EXTRA_GENRE_ID, 0)
        genreName = intent.getStringExtra(EXTRA_GENRE_NAME) ?: "Genre"

        Log.d("FilmByGenreActivity", "Activity started with genre: $genreName (ID: $genreId)")

        setupViews()
        setupRecyclerView()
        loadFilmsByGenre()
    }

    private fun setupViews() {
        toolbar = findViewById(R.id.toolbar)
        recyclerView = findViewById(R.id.recyclerViewFilms)
        progressBar = findViewById(R.id.progressBar)
        layoutEmptyState = findViewById(R.id.tvEmptyState)

        // Setup toolbar
        toolbar.setNavigationOnClickListener { finish() }
        toolbar.title = "Film $genreName"

        // Initialize repository
        repository = FilmRepository(RetrofitClient.instance)
    }

    private fun setupRecyclerView() {
        filmAdapter = FilmAdapter(emptyList())

        recyclerView.apply {
            layoutManager = GridLayoutManager(this@FilmByGenreActivity, 2)
            adapter = filmAdapter
        }
    }

    private fun loadFilmsByGenre() {
        showLoading(true)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val films = repository.getMoviesByGenre(genreId)
                
                withContext(Dispatchers.Main) {
                    showLoading(false)
                    if (films.isNotEmpty()) {
                        filmAdapter.updateFilms(films)
                        showEmptyState(false)
                    } else {
                        showEmptyState(true)
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showLoading(false)
                    showEmptyState(true)
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
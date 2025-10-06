package com.unsoed.moviesta

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.unsoed.moviesta.base.BaseAuthActivity
import com.unsoed.moviesta.model.Film
import com.unsoed.moviesta.model.Genre
import com.unsoed.moviesta.network.RetrofitClient
import com.unsoed.moviesta.repository.FilmRepository
import com.unsoed.moviesta.adapter.MovieGridAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FilmByGenreActivity : BaseAuthActivity() {

    companion object {
        const val EXTRA_GENRE_ID = "extra_genre_id"
        const val EXTRA_GENRE_NAME = "extra_genre_name"
    }

    private lateinit var toolbar: Toolbar
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var layoutEmptyState: LinearLayout
    private lateinit var movieAdapter: MovieGridAdapter
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

        // Setup toolbar with back button
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = "Film $genreName"
            setHomeAsUpIndicator(R.drawable.ic_arrow_back)
        }
        
        toolbar.setNavigationOnClickListener { finish() }

        // Initialize repository
        repository = FilmRepository(RetrofitClient.instance)
    }

    private fun setupRecyclerView() {
        movieAdapter = MovieGridAdapter { movie ->
            // Navigate to movie detail
            val intent = Intent(this, DetailActivity::class.java).apply {
                putExtra(DetailActivity.EXTRA_FILM, movie)
            }
            startActivity(intent)
        }

        recyclerView.apply {
            layoutManager = GridLayoutManager(this@FilmByGenreActivity, 2)
            adapter = movieAdapter
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
                        movieAdapter.updateMovies(films)
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
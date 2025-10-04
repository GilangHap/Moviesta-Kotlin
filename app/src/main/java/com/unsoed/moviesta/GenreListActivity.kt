package com.unsoed.moviesta

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.unsoed.moviesta.adapter.GenreAdapter
import com.unsoed.moviesta.base.BaseAuthActivity
import com.unsoed.moviesta.model.Genre
import com.unsoed.moviesta.network.RetrofitClient
import com.unsoed.moviesta.repository.FilmRepository
import com.unsoed.moviesta.viewmodel.FilmViewModel
import com.unsoed.moviesta.viewmodel.FilmViewModelFactory

class GenreListActivity : BaseAuthActivity() {
    
    private lateinit var filmViewModel: FilmViewModel
    private lateinit var genreAdapter: GenreAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvTitle: TextView
    private lateinit var ivBack: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_genre_list)

        initViews()
        setupViewModel()
        setupRecyclerView()
        setupClickListeners()
        loadGenres()
    }

    private fun initViews() {
        recyclerView = findViewById(R.id.recycler_view_genres)
        progressBar = findViewById(R.id.progress_bar)
        tvTitle = findViewById(R.id.tv_title)
        ivBack = findViewById(R.id.iv_back)
        
        tvTitle.text = "Movie Genres"
    }

    private fun setupViewModel() {
        val repository = FilmRepository(RetrofitClient.instance)
        val viewModelFactory = FilmViewModelFactory(repository)
        filmViewModel = ViewModelProvider(this, viewModelFactory)[FilmViewModel::class.java]
        
        // Observe genres
        filmViewModel.genres.observe(this) { genres ->
            progressBar.visibility = View.GONE
            genreAdapter.updateGenres(genres)
        }
        
        filmViewModel.error.observe(this) { error ->
            progressBar.visibility = View.GONE
            // Handle error
        }
    }

    private fun setupRecyclerView() {
        genreAdapter = GenreAdapter { genre ->
            openGenreMovies(genre)
        }
        
        recyclerView.apply {
            layoutManager = GridLayoutManager(this@GenreListActivity, 2)
            adapter = genreAdapter
        }
    }

    private fun setupClickListeners() {
        ivBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun loadGenres() {
        progressBar.visibility = View.VISIBLE
        filmViewModel.loadGenres()
    }

    private fun openGenreMovies(genre: Genre) {
        val intent = Intent(this, GenreActivity::class.java)
        intent.putExtra("GENRE_ID", genre.id)
        intent.putExtra("GENRE_NAME", genre.name)
        startActivity(intent)
    }
}
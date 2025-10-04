package com.unsoed.moviesta

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.unsoed.moviesta.model.Genre
import com.unsoed.moviesta.model.GenrePreference
import com.unsoed.moviesta.network.RetrofitClient
import com.unsoed.moviesta.repository.FilmRepository
import com.unsoed.moviesta.view.OnboardingGenreAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class OnboardingGenreActivity : AppCompatActivity() {
    
    private lateinit var toolbar: MaterialToolbar
    private lateinit var rvGenres: RecyclerView
    private lateinit var btnNext: MaterialButton
    private lateinit var btnSkip: MaterialButton
    private lateinit var progressBar: ProgressBar
    
    private lateinit var genreAdapter: OnboardingGenreAdapter
    private lateinit var filmRepository: FilmRepository
    
    private val selectedGenres = mutableListOf<Genre>()
    
    companion object {
        private const val TAG = "OnboardingGenre"
        private const val MIN_GENRE_SELECTION = 3
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding_genre)
        
        initializeViews()
        setupToolbar()
        setupRecyclerView()
        setupClickListeners()
        loadGenres()
        updateNextButton()
    }
    
    private fun initializeViews() {
        toolbar = findViewById(R.id.toolbar)
        rvGenres = findViewById(R.id.rv_genres)
        btnNext = findViewById(R.id.btn_next)
        btnSkip = findViewById(R.id.btn_skip)
        progressBar = findViewById(R.id.progress_bar)
        
        filmRepository = FilmRepository(RetrofitClient.instance)
    }
    
    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Pilih Genre Favorit"
        supportActionBar?.subtitle = "Minimal pilih $MIN_GENRE_SELECTION genre"
    }
    
    private fun setupRecyclerView() {
        genreAdapter = OnboardingGenreAdapter { genre, isSelected ->
            handleGenreSelection(genre, isSelected)
        }
        
        rvGenres.apply {
            layoutManager = GridLayoutManager(this@OnboardingGenreActivity, 2)
            adapter = genreAdapter
        }
    }
    
    private fun setupClickListeners() {
        btnNext.setOnClickListener {
            if (selectedGenres.size >= MIN_GENRE_SELECTION) {
                proceedToMovieSelection()
            } else {
                Toast.makeText(
                    this,
                    "Pilih minimal $MIN_GENRE_SELECTION genre untuk melanjutkan",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        
        btnSkip.setOnClickListener {
            skipToMain()
        }
    }
    
    private fun handleGenreSelection(genre: Genre, isSelected: Boolean) {
        if (isSelected) {
            if (!selectedGenres.contains(genre)) {
                selectedGenres.add(genre)
            }
        } else {
            selectedGenres.remove(genre)
        }
        updateNextButton()
        
        Log.d(TAG, "Selected genres: ${selectedGenres.size}")
    }
    
    private fun updateNextButton() {
        val isValid = selectedGenres.size >= MIN_GENRE_SELECTION
        btnNext.isEnabled = isValid
        btnNext.text = if (isValid) {
            "Lanjut (${selectedGenres.size} genre dipilih)"
        } else {
            "Pilih minimal $MIN_GENRE_SELECTION genre"
        }
    }
    
    private fun loadGenres() {
        progressBar.visibility = View.VISIBLE
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val genres = filmRepository.getGenres()
                
                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                    
                    if (genres.isNotEmpty()) {
                        val genrePreferences = genres.map { genre ->
                            GenrePreference(
                                genreId = genre.id,
                                genreName = genre.name,
                                isSelected = false
                            )
                        }
                        genreAdapter.submitList(genrePreferences)
                        Log.d(TAG, "Genres loaded: ${genres.size}")
                    } else {
                        Toast.makeText(
                            this@OnboardingGenreActivity,
                            "Gagal memuat genre. Silakan coba lagi.",
                            Toast.LENGTH_SHORT
                        ).show()
                        Log.e(TAG, "No genres loaded")
                    }
                }
                
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                    Toast.makeText(
                        this@OnboardingGenreActivity,
                        "Error: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.e(TAG, "Error loading genres", e)
                }
            }
        }
    }
    
    private fun proceedToMovieSelection() {
        val intent = Intent(this, OnboardingMovieActivity::class.java)
        intent.putParcelableArrayListExtra("selected_genres", ArrayList(selectedGenres))
        startActivity(intent)
        finish()
    }
    
    private fun skipToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
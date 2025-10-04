package com.unsoed.moviesta

import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
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
import com.unsoed.moviesta.model.Film
import com.unsoed.moviesta.model.Genre
import com.unsoed.moviesta.model.UserPreferences
import com.unsoed.moviesta.network.RetrofitClient
import com.unsoed.moviesta.repository.FilmRepository
import com.unsoed.moviesta.repository.UserPreferencesRepository
import com.unsoed.moviesta.utils.OnboardingManager
import com.unsoed.moviesta.view.OnboardingMovieAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class OnboardingMovieActivity : AppCompatActivity() {
    
    private lateinit var toolbar: MaterialToolbar
    private lateinit var rvMovies: RecyclerView
    private lateinit var btnFinish: MaterialButton
    private lateinit var btnSkip: MaterialButton
    private lateinit var progressBar: ProgressBar
    
    private lateinit var movieAdapter: OnboardingMovieAdapter
    private lateinit var filmRepository: FilmRepository
    private lateinit var userPreferencesRepository: UserPreferencesRepository
    
    private var selectedGenres: List<Genre> = emptyList()
    private val watchedMovies = mutableListOf<Film>()
    
    companion object {
        private const val TAG = "OnboardingMovie"
        private const val MIN_MOVIE_SELECTION = 5
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding_movie)
        
        initializeViews()
        getIntentData()
        setupToolbar()
        setupRecyclerView()
        setupClickListeners()
        loadPopularMovies()
        updateFinishButton()
    }
    
    private fun initializeViews() {
        toolbar = findViewById(R.id.toolbar)
        rvMovies = findViewById(R.id.rv_movies)
        btnFinish = findViewById(R.id.btn_finish)
        btnSkip = findViewById(R.id.btn_skip)
        progressBar = findViewById(R.id.progress_bar)
        
        filmRepository = FilmRepository(RetrofitClient.instance)
        userPreferencesRepository = UserPreferencesRepository()
    }
    
    private fun getIntentData() {
        selectedGenres = intent.getParcelableArrayListExtra("selected_genres") ?: emptyList()
        Log.d(TAG, "Received selected genres: ${selectedGenres.size}")
    }
    
    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Film yang Sudah Ditonton"
        supportActionBar?.subtitle = "Pilih film yang sudah pernah kamu tonton"
    }
    
    private fun setupRecyclerView() {
        movieAdapter = OnboardingMovieAdapter { movie, isWatched ->
            handleMovieSelection(movie, isWatched)
        }
        
        rvMovies.apply {
            layoutManager = GridLayoutManager(this@OnboardingMovieActivity, 3)
            adapter = movieAdapter
        }
    }
    
    private fun setupClickListeners() {
        btnFinish.setOnClickListener {
            finishOnboarding()
        }
        
        btnSkip.setOnClickListener {
            finishOnboarding()
        }
    }
    
    private fun handleMovieSelection(movie: Film, isWatched: Boolean) {
        if (isWatched) {
            if (!watchedMovies.contains(movie)) {
                watchedMovies.add(movie)
            }
        } else {
            watchedMovies.remove(movie)
        }
        updateFinishButton()
        
        Log.d(TAG, "Watched movies: ${watchedMovies.size}")
    }
    
    private fun updateFinishButton() {
        btnFinish.text = if (watchedMovies.isNotEmpty()) {
            "Selesai (${watchedMovies.size} film dipilih)"
        } else {
            "Selesai"
        }
    }
    
    private fun loadPopularMovies() {
        progressBar.visibility = View.VISIBLE
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val movies = filmRepository.getPopularFilms()
                
                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                    
                    if (movies.isNotEmpty()) {
                        movieAdapter.submitList(movies)
                        Log.d(TAG, "Movies loaded: ${movies.size}")
                    } else {
                        Toast.makeText(
                            this@OnboardingMovieActivity,
                            "Gagal memuat film. Silakan coba lagi.",
                            Toast.LENGTH_SHORT
                        ).show()
                        Log.e(TAG, "No movies loaded")
                    }
                }
                
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                    Toast.makeText(
                        this@OnboardingMovieActivity,
                        "Error: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.e(TAG, "Error loading movies", e)
                }
            }
        }
    }
    
    private fun finishOnboarding() {
        Log.d(TAG, "========== Starting onboarding completion process ==========")
        
        // Check internet connection first
        if (!isNetworkAvailable()) {
            Log.e(TAG, "No internet connection available")
            Toast.makeText(
                this,
                "Tidak ada koneksi internet. Periksa koneksi Anda dan coba lagi.",
                Toast.LENGTH_LONG
            ).show()
            return
        }
        
        Log.d(TAG, "Internet connection available, proceeding...")
        btnFinish.isEnabled = false
        progressBar.visibility = View.VISIBLE
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d(TAG, "Checking user authentication...")
                // Check if user is authenticated
                val currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
                if (currentUser == null) {
                    Log.e(TAG, "User not authenticated")
                    withContext(Dispatchers.Main) {
                        progressBar.visibility = View.GONE
                        btnFinish.isEnabled = true
                        Toast.makeText(
                            this@OnboardingMovieActivity,
                            "User tidak ter-autentikasi. Silakan login ulang.",
                            Toast.LENGTH_LONG
                        ).show()
                        Log.e(TAG, "User not authenticated during onboarding completion")
                    }
                    return@launch
                }
                
                Log.d(TAG, "User authenticated successfully: ${currentUser.uid}")
                Log.d(TAG, "User email: ${currentUser.email}")
                Log.d(TAG, "Selected genres: ${selectedGenres.size}, Watched movies: ${watchedMovies.size}")
                
                // Prepare user preferences
                val userPreferences = UserPreferences(
                    favoriteGenres = selectedGenres.map { it.id },
                    watchedMovies = watchedMovies.map { it.id },
                    isOnboardingCompleted = true
                )
                
                Log.d(TAG, "Prepared user preferences: $userPreferences")
                Log.d(TAG, "Starting save operation to Firebase...")
                
                // Save to Firebase with timeout
                val result = userPreferencesRepository.saveUserPreferences(userPreferences)
                
                Log.d(TAG, "Save operation completed, processing result...")
                
                withContext(Dispatchers.Main) {
                    Log.d(TAG, "Back on main thread, updating UI...")
                    progressBar.visibility = View.GONE
                    
                    if (result.isSuccess) {
                        Log.d(TAG, "SUCCESS: User preferences saved successfully!")
                        
                        // Double-check that data was actually saved by reading it back
                        Log.d(TAG, "Verifying saved data...")
                        try {
                            val verificationResult = userPreferencesRepository.isOnboardingCompleted()
                            if (verificationResult.isSuccess && verificationResult.getOrNull() == true) {
                                Log.d(TAG, "✅ VERIFICATION PASSED - onboarding status confirmed as completed")
                                
                                // Mark onboarding as completed locally for faster future checks
                                OnboardingManager.markOnboardingCompleted(this@OnboardingMovieActivity, currentUser.uid)
                                
                                Toast.makeText(
                                    this@OnboardingMovieActivity,
                                    "Preferensi berhasil disimpan!",
                                    Toast.LENGTH_SHORT
                                ).show()
                                
                                Log.d(TAG, "Navigating to main activity...")
                                navigateToMain()
                            } else {
                                Log.e(TAG, "❌ VERIFICATION FAILED - data may not have been saved properly")
                                btnFinish.isEnabled = true
                                showErrorWithSkipOption("Data tidak tersimpan dengan benar. Coba lagi atau lewati.")
                            }
                        } catch (verifyError: Exception) {
                            Log.e(TAG, "❌ VERIFICATION ERROR", verifyError)
                            // Proceed anyway since save was successful
                            // Mark locally as completed
                            OnboardingManager.markOnboardingCompleted(this@OnboardingMovieActivity, currentUser.uid)
                            
                            Toast.makeText(
                                this@OnboardingMovieActivity,
                                "Preferensi berhasil disimpan!",
                                Toast.LENGTH_SHORT
                            ).show()
                            navigateToMain()
                        }
                    } else {
                        btnFinish.isEnabled = true
                        val error = result.exceptionOrNull()
                        Log.e(TAG, "FAILED: Error saving preferences", error)
                        
                        val errorMessage = when (error) {
                            is com.google.firebase.firestore.FirebaseFirestoreException -> {
                                "Firebase Error: ${error.code} - ${error.message}"
                            }
                            is java.net.SocketTimeoutException -> {
                                "Koneksi internet timeout. Periksa koneksi Anda."
                            }
                            else -> {
                                error?.message ?: "Error tidak diketahui"
                            }
                        }
                        
                        // Show error with option to skip
                        showErrorWithSkipOption(errorMessage)
                        Log.e(TAG, "Failed to save preferences with message: $errorMessage")
                    }
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "EXCEPTION: Critical error during onboarding completion", e)
                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                    btnFinish.isEnabled = true
                    
                    val errorMessage = "Error: ${e.message}"
                    Log.e(TAG, "Exception during onboarding completion: $errorMessage", e)
                    
                    // Show error with skip option for critical errors too
                    showErrorWithSkipOption(errorMessage)
                }
            }
        }
    }
    
    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) || 
               capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
               capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
    }
    
    private fun showErrorWithSkipOption(errorMessage: String) {
        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Error Menyimpan Preferensi")
            .setMessage("$errorMessage\n\nApakah Anda ingin melewati dan melanjutkan ke aplikasi?")
            .setPositiveButton("Lewati") { _, _ ->
                Log.d(TAG, "User chose to skip error and continue")
                navigateToMain()
            }
            .setNegativeButton("Coba Lagi") { _, _ ->
                Log.d(TAG, "User chose to retry")
                btnFinish.isEnabled = true
            }
            .setCancelable(false)
            .create()
        
        dialog.show()
    }

    private fun navigateToMain() {
        Log.d(TAG, "========== Navigating to MainActivity ==========")
        try {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            Log.d(TAG, "Intent created with flags: NEW_TASK | CLEAR_TASK")
            startActivity(intent)
            Log.d(TAG, "StartActivity called successfully")
            finish()
            Log.d(TAG, "Current activity finished")
        } catch (e: Exception) {
            Log.e(TAG, "Error navigating to MainActivity", e)
            // Fallback: try without flags
            try {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
                Log.d(TAG, "Fallback navigation successful")
            } catch (e2: Exception) {
                Log.e(TAG, "Fallback navigation also failed", e2)
                Toast.makeText(this, "Error navigating to main screen", Toast.LENGTH_LONG).show()
            }
        }
    }
}
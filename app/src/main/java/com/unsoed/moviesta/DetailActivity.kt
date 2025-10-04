package com.unsoed.moviesta

import android.animation.ObjectAnimator
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.unsoed.moviesta.base.BaseAuthActivity
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.unsoed.moviesta.model.Film
import com.unsoed.moviesta.model.FilmDetail
import com.unsoed.moviesta.network.RetrofitClient
import com.unsoed.moviesta.repository.UserPreferencesRepository
import com.unsoed.moviesta.repository.WatchlistRepository
import com.unsoed.moviesta.repository.WatchedMoviesRepository
import com.unsoed.moviesta.repository.FilmRepository
import com.unsoed.moviesta.view.GenreAdapter
import com.unsoed.moviesta.view.GenreChipAdapter
import com.unsoed.moviesta.view.RecommendationAdapter
import com.unsoed.moviesta.viewmodel.WatchlistViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class DetailActivity : BaseAuthActivity() {

    // Konstanta kunci untuk Intent
    companion object {
        const val EXTRA_FILM = "extra_film"
    }

    // Deklarasi komponen untuk Cast, Genres, dan Recommendations
    private lateinit var castAdapter: CastAdapter
    private lateinit var genreAdapter: GenreChipAdapter
    private lateinit var recommendationAdapter: RecommendationAdapter
    private lateinit var repository: FilmRepository
    private lateinit var recyclerViewCast: RecyclerView
    private lateinit var recyclerViewGenres: RecyclerView
    private lateinit var recyclerViewRecommendations: RecyclerView
    private lateinit var layoutNoRecommendations: LinearLayout
    private lateinit var layoutRecommendationsLoading: LinearLayout
    
    // Watchlist components
    private lateinit var watchlistViewModel: WatchlistViewModel
    private lateinit var btnWatchlist: MaterialButton
    private lateinit var btnShare: MaterialButton
    private var currentFilm: Film? = null
    private var currentFilmDetail: FilmDetail? = null
    
    // Watched Movies components
    private lateinit var watchedMoviesRepository: WatchedMoviesRepository
    private lateinit var fabMarkWatched: ExtendedFloatingActionButton
    private lateinit var fabAddWatchlist: FloatingActionButton
    private var isWatched = false
    private var isFabExpanded = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        // Mengambil objek Film dari Intent
        val film = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(EXTRA_FILM, Film::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra<Film>(EXTRA_FILM)
        }

        // 1. Inisialisasi Toolbar dan CollapsingToolbar
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        val collapsingToolbar: CollapsingToolbarLayout = findViewById(R.id.collapsing_toolbar)

        // Tetapkan Toolbar sebagai support action bar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // Tampilkan tombol kembali

        // 2. Inisialisasi Repository
        repository = FilmRepository(RetrofitClient.instance)

        // 3. Setup RecyclerView untuk Aktor
        recyclerViewCast = findViewById(R.id.recycler_view_cast)
        castAdapter = CastAdapter(emptyList())
        // Mengatur layout horizontal untuk daftar aktor
        recyclerViewCast.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recyclerViewCast.adapter = castAdapter

        // 4. Setup RecyclerView untuk Genre
        recyclerViewGenres = findViewById(R.id.recycler_view_genres)
        genreAdapter = GenreChipAdapter(emptyList())
        recyclerViewGenres.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recyclerViewGenres.adapter = genreAdapter

        // 5. Setup RecyclerView untuk Rekomendasi
        recyclerViewRecommendations = findViewById(R.id.recycler_view_recommendations)
        recommendationAdapter = RecommendationAdapter(emptyList())
        recyclerViewRecommendations.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recyclerViewRecommendations.adapter = recommendationAdapter
        
        // 6. Setup empty state
        layoutNoRecommendations = findViewById(R.id.layout_no_recommendations)
        layoutRecommendationsLoading = findViewById(R.id.layout_recommendations_loading)
        
        // 7. Setup Watchlist components
        setupWatchlist()
        
        // 8. Setup Watched Movies Repository and FAB
        watchedMoviesRepository = WatchedMoviesRepository(
            auth = FirebaseAuth.getInstance(),
            userPreferencesRepository = UserPreferencesRepository()
        )
        setupWatchedMoviesFAB()

        // Tampilkan data jika film tidak null
        film?.let { detail ->
            currentFilm = detail
            
            // Load detail film untuk mendapatkan genre dan informasi lengkap
            loadFilmDetail(detail.id)

            // 4. Panggil fungsi untuk memuat data aktor
            loadCastData(detail.id)

            // 5. Panggil fungsi untuk memuat rekomendasi film
            loadRecommendations(detail.id)
            
            // 6. Check watched status
            checkWatchedStatus()

        } ?: finish() // Tutup Activity jika tidak ada data film (error handling sederhana)
    }

    // Override untuk menghandle tombol kembali di Toolbar
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    /**
     * Memuat detail film lengkap dengan genre dari API menggunakan Coroutine.
     */
    private fun loadFilmDetail(movieId: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val filmDetail = repository.getFilmDetail(movieId)
                
                CoroutineScope(Dispatchers.Main).launch {
                    // Store the film detail for watchlist operations
                    currentFilmDetail = filmDetail
                    
                    // Update watchlist status if button is initialized
                    if (::btnWatchlist.isInitialized) {
                        watchlistViewModel.isInWatchlist(filmDetail.id).observe(this@DetailActivity) { isInWatchlist ->
                            updateWatchlistButton(isInWatchlist)
                        }
                    }
                    
                    // Mengambil referensi View
                    val imgPoster: ImageView = findViewById(R.id.img_poster_detail)
                    val imgPosterCover: ImageView = findViewById(R.id.iv_movie_poster_cover)
                    val tvTitle: TextView = findViewById(R.id.tv_title_detail)
                    val tvRating: TextView = findViewById(R.id.tv_rating_detail)
                    val tvSinopsis: TextView = findViewById(R.id.tv_sinopsis_detail)
                    val tvReleaseDate: TextView = findViewById(R.id.tv_release_date)
                    val tvRuntime: TextView = findViewById(R.id.tv_runtime)
                    val collapsingToolbar: CollapsingToolbarLayout = findViewById(R.id.collapsing_toolbar)

                    // Mengatur judul CollapsingToolbar - DINONAKTIFKAN
                    // collapsingToolbar.title = filmDetail.safeTitle

                    // Update UI dengan data dari API
                    tvTitle.text = filmDetail.safeTitle
                    tvRating.text = String.format("%.1f", filmDetail.rating)
                    tvSinopsis.text = filmDetail.sinopsis ?: "Sinopsis tidak tersedia"

                    // Format tanggal rilis
                    filmDetail.releaseDate?.let { dateString ->
                        try {
                            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                            val outputFormat = SimpleDateFormat("dd MMMM yyyy", Locale.forLanguageTag("id-ID"))
                            val date = inputFormat.parse(dateString)
                            tvReleaseDate.text = date?.let { outputFormat.format(it) } ?: dateString
                        } catch (e: Exception) {
                            tvReleaseDate.text = dateString
                        }
                    } ?: run {
                        tvReleaseDate.text = "Tidak diketahui"
                    }

                    // Format runtime
                    filmDetail.runtime?.let { runtime ->
                        val hours = runtime / 60
                        val minutes = runtime % 60
                        tvRuntime.text = when {
                            hours > 0 -> "${hours}j ${minutes}m"
                            else -> "${minutes} menit"
                        }
                    } ?: run {
                        tvRuntime.text = "Tidak diketahui"
                    }

                    // Muat gambar poster untuk backdrop
                    val imageUrl = "https://image.tmdb.org/t/p/w500${filmDetail.posterPath}"
                    imgPoster.load(imageUrl) {
                        crossfade(true)
                    }
                    
                    // Muat gambar poster untuk cover di samping info
                    imgPosterCover.load(imageUrl) {
                        crossfade(true)
                    }

                    // Update genre adapter
                    genreAdapter.updateGenres(filmDetail.genres)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // Fallback to basic film data if detail loading fails
                CoroutineScope(Dispatchers.Main).launch {
                    val film = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        intent.getParcelableExtra(EXTRA_FILM, Film::class.java)
                    } else {
                        @Suppress("DEPRECATION")
                        intent.getParcelableExtra<Film>(EXTRA_FILM)
                    }
                    
                    film?.let { fallbackFilm ->
                        val imgPoster: ImageView = findViewById(R.id.img_poster_detail)
                        val imgPosterCover: ImageView = findViewById(R.id.iv_movie_poster_cover)
                        val tvTitle: TextView = findViewById(R.id.tv_title_detail)
                        val tvRating: TextView = findViewById(R.id.tv_rating_detail)
                        val tvSinopsis: TextView = findViewById(R.id.tv_sinopsis_detail)
                        val collapsingToolbar: CollapsingToolbarLayout = findViewById(R.id.collapsing_toolbar)

                        // collapsingToolbar.title = fallbackFilm.safeTitle - DINONAKTIFKAN
                        tvTitle.text = fallbackFilm.safeTitle
                        tvRating.text = String.format("%.1f", fallbackFilm.rating)
                        tvSinopsis.text = fallbackFilm.sinopsis ?: "Sinopsis tidak tersedia"

                        val imageUrl = "https://image.tmdb.org/t/p/w500${fallbackFilm.posterPath}"
                        imgPoster.load(imageUrl) {
                            crossfade(true)
                        }
                        
                        imgPosterCover.load(imageUrl) {
                            crossfade(true)
                        }
                    }
                }
            }
        }
    }

    /**
     * Memuat data aktor (Cast) dari API menggunakan Coroutine.
     */
    private fun loadCastData(movieId: Int) {
        // CoroutineScope(Dispatchers.IO) menjalankan operasi I/O (network call) di background thread
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Panggil repository untuk mendapatkan daftar Cast
                val castList = repository.getFilmCast(movieId)

                // Pindah ke Main thread untuk update UI (RecyclerView)
                CoroutineScope(Dispatchers.Main).launch {
                    castAdapter.updateCast(castList)
                }
            } catch (e: Exception) {
                // Tangani error, misalnya film tidak memiliki data Cast
                e.printStackTrace()
            }
        }
    }

    /**
     * Memuat rekomendasi film serupa dari API menggunakan Coroutine.
     */
    private fun loadRecommendations(movieId: Int) {
        // Tampilkan loading state
        CoroutineScope(Dispatchers.Main).launch {
            layoutRecommendationsLoading.visibility = View.VISIBLE
            recyclerViewRecommendations.visibility = View.GONE
            layoutNoRecommendations.visibility = View.GONE
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Coba ambil film serupa dulu, kalau gagal ambil rekomendasi
                val similarFilms = repository.getSimilarFilms(movieId)
                
                CoroutineScope(Dispatchers.Main).launch {
                    layoutRecommendationsLoading.visibility = View.GONE
                    
                    if (similarFilms.isNotEmpty()) {
                        // Tampilkan maksimal 10 rekomendasi
                        val limitedRecommendations = similarFilms.take(10)
                        recommendationAdapter.updateFilms(limitedRecommendations)
                        recyclerViewRecommendations.visibility = View.VISIBLE
                        layoutNoRecommendations.visibility = View.GONE
                    } else {
                        // Tampilkan empty state
                        recyclerViewRecommendations.visibility = View.GONE
                        layoutNoRecommendations.visibility = View.VISIBLE
                    }
                }
            } catch (e: Exception) {
                CoroutineScope(Dispatchers.Main).launch {
                    layoutRecommendationsLoading.visibility = View.GONE
                    // Tampilkan empty state jika error
                    recyclerViewRecommendations.visibility = View.GONE
                    layoutNoRecommendations.visibility = View.VISIBLE
                }
                e.printStackTrace()
            }
        }
    }
    
    // Setup Watchlist functionality
    private fun setupWatchlist() {
        // Initialize ViewModel
        watchlistViewModel = ViewModelProvider(this)[WatchlistViewModel::class.java]
        
        // Initialize Buttons
        btnWatchlist = findViewById(R.id.btn_watchlist)
        btnShare = findViewById(R.id.btn_share)
        
        // Setup observers
        setupWatchlistObservers()
        
        // Setup click listeners
        btnWatchlist.setOnClickListener {
            toggleWatchlist()
        }
        
        btnShare.setOnClickListener {
            shareFilm()
        }
        
        // Check initial watchlist status
        currentFilm?.let { film ->
            watchlistViewModel.isInWatchlist(film.id).observe(this) { isInWatchlist ->
                updateWatchlistButton(isInWatchlist)
            }
        }
    }
    
    private fun setupWatchlistObservers() {
        // Observe success messages
        watchlistViewModel.successMessage.observe(this) { message ->
            message?.let {
                showSnackbar(it)
                watchlistViewModel.clearMessages()
            }
        }
        
        // Observe error messages
        watchlistViewModel.errorMessage.observe(this) { message ->
            message?.let {
                showSnackbar(it, isError = true)
                watchlistViewModel.clearMessages()
            }
        }
        
        // Observe loading state
        watchlistViewModel.isLoading.observe(this) { isLoading ->
            btnWatchlist.isEnabled = !isLoading
        }
    }
    
    private fun toggleWatchlist() {
        when {
            currentFilmDetail != null -> {
                watchlistViewModel.toggleWatchlist(currentFilmDetail!!)
            }
            currentFilm != null -> {
                watchlistViewModel.toggleWatchlist(currentFilm!!)
            }
            else -> {
                showSnackbar("Error: Data film tidak tersedia", isError = true)
            }
        }
    }
    
    private fun updateWatchlistButton(isInWatchlist: Boolean) {
        if (isInWatchlist) {
            btnWatchlist.text = "Hapus dari Watchlist"
            btnWatchlist.setIconResource(R.drawable.ic_bookmark)
        } else {
            btnWatchlist.text = "Tambah ke Watchlist"
            btnWatchlist.setIconResource(R.drawable.ic_bookmark_border)
        }
    }
    
    private fun shareFilm() {
        val filmTitle = currentFilmDetail?.title ?: currentFilm?.title ?: "Film"
        val shareText = "Ayo tonton film \"$filmTitle\"! Film yang sangat menarik dan wajib ditonton. 🎬\n\nShared from Moviesta App"
        
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
        }
        
        try {
            startActivity(Intent.createChooser(shareIntent, "Bagikan Film"))
        } catch (e: Exception) {
            showSnackbar("Tidak dapat berbagi film", isError = true)
        }
    }
    
    private fun showSnackbar(message: String, isError: Boolean = false) {
        val snackbar = Snackbar.make(btnWatchlist, message, Snackbar.LENGTH_SHORT)
        if (isError) {
            snackbar.setBackgroundTint(getColor(android.R.color.holo_red_dark))
        }
        snackbar.show()
    }
    
    /**
     * Setup Floating Action Button untuk menandai film sebagai sudah ditonton
     */
    private fun setupWatchedMoviesFAB() {
        fabMarkWatched = findViewById(R.id.fab_mark_watched)
        fabAddWatchlist = findViewById(R.id.fab_add_watchlist)
        
        // Set click listener untuk main FAB (Mark as Watched)
        fabMarkWatched.setOnClickListener {
            handleWatchedToggle()
        }
        
        // Set click listener untuk secondary FAB (Add to Watchlist)
        fabAddWatchlist.setOnClickListener {
            toggleWatchlist()
        }
        
        // Set long click listener untuk expanding secondary FAB
        fabMarkWatched.setOnLongClickListener {
            toggleFABExpansion()
            true
        }
    }
    
    /**
     * Handle toggle watched status
     */
    private fun handleWatchedToggle() {
        currentFilm?.let { film ->
            lifecycleScope.launch {
                try {
                    android.util.Log.d("DetailActivity", "Toggling watched status for: ${film.title}")
                    
                    if (isWatched) {
                        // Remove from watched
                        android.util.Log.d("DetailActivity", "Removing from watched list")
                        val result = watchedMoviesRepository.removeWatchedMovie(film.id.toString())
                        if (result.isSuccess && result.getOrDefault(false)) {
                            isWatched = false
                            updateWatchedFABState()
                            showWatchedSnackbar("Removed from history list", false)
                            android.util.Log.d("DetailActivity", "Successfully removed from watched list")
                        } else {
                            showWatchedSnackbar("Failed to remove from history list", true)
                            android.util.Log.e("DetailActivity", "Failed to remove from watched list")
                        }
                    } else {
                        // Add to watched
                        android.util.Log.d("DetailActivity", "Adding to watched list")
                        val result = watchedMoviesRepository.addWatchedMovie(film)
                        if (result.isSuccess) {
                            isWatched = true
                            updateWatchedFABState()
                            showWatchedSnackbar("Added to history list! 🎉", false)
                            animateWatchedSuccess()
                            triggerHapticFeedback()
                            android.util.Log.d("DetailActivity", "Successfully added to watched list")
                        } else {
                            val error = result.exceptionOrNull()
                            showWatchedSnackbar("Failed to add to history list", true)
                            android.util.Log.e("DetailActivity", "Failed to add to watched list", error)
                        }
                    }
                } catch (e: Exception) {
                    showWatchedSnackbar("Error: ${e.message}", true)
                    android.util.Log.e("DetailActivity", "Exception in handleWatchedToggle", e)
                }
            }
        }
    }
    
    /**
     * Check if current movie is already watched
     */
    private fun checkWatchedStatus() {
        currentFilm?.let { film ->
            lifecycleScope.launch {
                try {
                    android.util.Log.d("DetailActivity", "Checking watched status for movie ID: ${film.id}")
                    
                    // Check from UserPreferences only (single source of truth)
                    val result = watchedMoviesRepository.isMovieWatched(film.id.toString())
                    
                    if (result.isSuccess) {
                        isWatched = result.getOrDefault(false)
                        android.util.Log.d("DetailActivity", "Movie ${film.id} watched status: $isWatched")
                        updateWatchedFABState()
                    } else {
                        android.util.Log.e("DetailActivity", "Failed to check watched status", result.exceptionOrNull())
                        isWatched = false
                        updateWatchedFABState()
                    }
                } catch (e: Exception) {
                    android.util.Log.e("DetailActivity", "Error checking watched status", e)
                    isWatched = false
                    updateWatchedFABState()
                }
            }
        }
    }
    
    /**
     * Update FAB appearance based on watched status
     */
    private fun updateWatchedFABState() {
        if (isWatched) {
            fabMarkWatched.text = "Watched ✓"
            fabMarkWatched.setIconResource(R.drawable.ic_eye_check)
            fabMarkWatched.backgroundTintList = getColorStateList(R.color.fab_watched_checked)
        } else {
            fabMarkWatched.text = "Mark as Watched"
            fabMarkWatched.setIconResource(R.drawable.ic_eye_add)
            fabMarkWatched.backgroundTintList = getColorStateList(R.color.accent_blue)
        }
    }
    
    /**
     * Toggle FAB expansion to show secondary actions
     */
    private fun toggleFABExpansion() {
        isFabExpanded = !isFabExpanded
        
        if (isFabExpanded) {
            // Show secondary FAB with advanced animation
            fabAddWatchlist.visibility = View.VISIBLE
            val expandAnimation = AnimationUtils.loadAnimation(this, R.anim.fab_expand_animation)
            fabAddWatchlist.startAnimation(expandAnimation)
            
            fabAddWatchlist.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(300)
                .setInterpolator(AccelerateDecelerateInterpolator())
                .start()
        } else {
            // Hide secondary FAB with animation
            fabAddWatchlist.animate()
                .alpha(0f)
                .scaleX(0.8f)
                .scaleY(0.8f)
                .setDuration(200)
                .setInterpolator(AccelerateDecelerateInterpolator())
                .withEndAction {
                    fabAddWatchlist.visibility = View.GONE
                }
                .start()
        }
    }
    
    /**
     * Animate success feedback when movie is marked as watched
     */
    private fun animateWatchedSuccess() {
        // Advanced success animation
        val successAnimation = AnimationUtils.loadAnimation(this, R.anim.fab_success_animation)
        fabMarkWatched.startAnimation(successAnimation)
        
        // Scale animation for success feedback
        val scaleX = ObjectAnimator.ofFloat(fabMarkWatched, "scaleX", 1f, 1.3f, 1f)
        val scaleY = ObjectAnimator.ofFloat(fabMarkWatched, "scaleY", 1f, 1.3f, 1f)
        
        scaleX.duration = 800
        scaleY.duration = 800
        scaleX.interpolator = AccelerateDecelerateInterpolator()
        scaleY.interpolator = AccelerateDecelerateInterpolator()
        
        scaleX.start()
        scaleY.start()
        
        // Pulse effect
        fabMarkWatched.animate()
            .translationZ(24f)
            .setDuration(300)
            .withEndAction {
                fabMarkWatched.animate()
                    .translationZ(12f)
                    .setDuration(300)
                    .start()
            }
            .start()
    }
    
    /**
     * Show snackbar for watched movie actions
     */
    private fun showWatchedSnackbar(message: String, isError: Boolean = false) {
        val snackbar = Snackbar.make(fabMarkWatched, message, Snackbar.LENGTH_SHORT)
        if (isError) {
            snackbar.setBackgroundTint(getColor(R.color.error_color))
        } else {
            snackbar.setBackgroundTint(getColor(R.color.accent_blue))
        }
        snackbar.setTextColor(getColor(android.R.color.white))
        snackbar.show()
    }
    
    /**
     * Trigger haptic feedback for futuristic feel
     */
    private fun triggerHapticFeedback() {
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Modern vibration pattern for success
            val vibrationEffect = VibrationEffect.createWaveform(
                longArrayOf(0, 50, 50, 100), // delays and durations
                intArrayOf(0, 100, 0, 255), // amplitudes
                -1 // don't repeat
            )
            vibrator.vibrate(vibrationEffect)
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(longArrayOf(0, 50, 50, 100), -1)
        }
    }
}

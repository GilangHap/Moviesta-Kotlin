package com.unsoed.moviesta

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.google.android.material.chip.ChipGroup
import com.unsoed.moviesta.model.Film
import com.unsoed.moviesta.network.RetrofitClient
import com.unsoed.moviesta.network.TmdbApiService
import kotlinx.coroutines.launch

class SearchActivity : AppCompatActivity() {

    private lateinit var searchEditText: EditText
    private lateinit var backButton: ImageView
    private lateinit var voiceButton: ImageView
    private lateinit var filtersChipGroup: ChipGroup
    private lateinit var resultsRecyclerView: RecyclerView
    private lateinit var recentSearchesRecyclerView: RecyclerView
    private lateinit var resultsTitle: TextView
    private lateinit var resultsCount: TextView
    
    private lateinit var searchAdapter: SearchResultAdapter
    
    private var searchHandler = Handler(Looper.getMainLooper())
    private var searchRunnable: Runnable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_search)
        
        initViews()
        setupRecyclerView()
        setupSearchFunctionality()
        setupInitialState()
    }

    private fun initViews() {
        searchEditText = findViewById(R.id.et_search)
        backButton = findViewById(R.id.iv_back)
        voiceButton = findViewById(R.id.iv_voice_search)
        filtersChipGroup = findViewById(R.id.chip_group_filters)
        resultsRecyclerView = findViewById(R.id.recycler_search_results)
        recentSearchesRecyclerView = findViewById(R.id.recycler_recent_searches)
        resultsTitle = findViewById(R.id.tv_results_title)
        resultsCount = findViewById(R.id.tv_results_count)
        
        backButton.setOnClickListener { finish() }
        voiceButton.setOnClickListener { 
            Toast.makeText(this, "Voice search not implemented", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupRecyclerView() {
        searchAdapter = SearchResultAdapter { movie ->
            val intent = Intent(this, DetailActivity::class.java)
            intent.putExtra(DetailActivity.EXTRA_FILM, movie) // Gunakan konstanta yang benar
            startActivity(intent)
        }
        
        resultsRecyclerView.adapter = searchAdapter
        resultsRecyclerView.layoutManager = GridLayoutManager(this, 2)
    }

    private fun setupSearchFunctionality() {
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            
            override fun afterTextChanged(s: Editable?) {
                val query = s.toString().trim()
                
                // Cancel previous search
                searchRunnable?.let { searchHandler.removeCallbacks(it) }
                
                if (query.isEmpty()) {
                    showInitialState()
                } else {
                    // Debounced search after 300ms of no typing
                    searchRunnable = Runnable {
                        if (query.length >= 2) {
                            performSearch(query)
                        }
                    }
                    searchHandler.postDelayed(searchRunnable!!, 300)
                }
            }
        })
    }

    private fun setupInitialState() {
        showInitialState()
    }

    private fun showInitialState() {
        findViewById<View>(R.id.layout_recent_searches).visibility = View.VISIBLE
        findViewById<View>(R.id.layout_search_results).visibility = View.GONE
        findViewById<View>(R.id.layout_loading).visibility = View.GONE
    }

    private fun showLoading() {
        findViewById<View>(R.id.layout_recent_searches).visibility = View.GONE
        findViewById<View>(R.id.layout_search_results).visibility = View.GONE
        findViewById<View>(R.id.layout_loading).visibility = View.VISIBLE
    }

    private fun showResults(movies: List<Film>, query: String) {
        findViewById<View>(R.id.layout_recent_searches).visibility = View.GONE
        findViewById<View>(R.id.layout_loading).visibility = View.GONE
        
        if (movies.isNotEmpty()) {
            searchAdapter.updateMovies(movies)
            resultsTitle.text = "Search Results"
            resultsCount.text = "${movies.size} movies found for \"$query\""
            findViewById<View>(R.id.layout_search_results).visibility = View.VISIBLE
        } else {
            resultsTitle.text = "No Results"
            resultsCount.text = "No movies found for \"$query\""
            findViewById<View>(R.id.layout_search_results).visibility = View.VISIBLE
        }
    }

    private fun performSearch(query: String) {
        if (query.isBlank()) return
        
        showLoading()
        
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.searchFilms(
                    apiKey = TmdbApiService.API_KEY,
                    query = query
                )
                
                showResults(response.films ?: emptyList(), query)
                
            } catch (e: Exception) {
                showResults(emptyList(), query)
                Toast.makeText(this@SearchActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

// Simple adapter for search results
class SearchResultAdapter(
    private val onMovieClick: (Film) -> Unit
) : RecyclerView.Adapter<SearchResultAdapter.MovieViewHolder>() {

    private var movies: List<Film> = emptyList()

    fun updateMovies(newMovies: List<Film>) {
        movies = newMovies
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovieViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_movie_grid, parent, false)
        return MovieViewHolder(view)
    }

    override fun onBindViewHolder(holder: MovieViewHolder, position: Int) {
        holder.bind(movies[position])
    }

    override fun getItemCount(): Int = movies.size

    inner class MovieViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val moviePoster: ImageView = itemView.findViewById(R.id.iv_poster)
        private val movieTitle: TextView = itemView.findViewById(R.id.tv_title)
        private val movieRating: TextView = itemView.findViewById(R.id.tv_rating)
        private val movieYear: TextView = itemView.findViewById(R.id.tv_year)

        fun bind(movie: Film) {
            movieTitle.text = movie.title ?: "Unknown Title"
            movieRating.text = "${String.format("%.1f", movie.voteAverage ?: 0.0)}"
            
            // Extract year from release date
            val year = try {
                movie.releaseDate?.substring(0, 4) ?: "N/A"
            } catch (e: Exception) {
                "N/A"
            }
            movieYear.text = year
            
            // Load poster image
            val posterUrl = if (!movie.posterPath.isNullOrEmpty()) {
                "https://image.tmdb.org/t/p/w500${movie.posterPath}"
            } else {
                null
            }
            
            moviePoster.load(posterUrl) {
                placeholder(R.drawable.ic_movie_empty)
                error(R.drawable.ic_movie_empty)
                crossfade(true)
            }
            
            itemView.setOnClickListener {
                onMovieClick(movie)
            }
        }
    }
}
package com.unsoed.moviesta

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.CircleCropTransformation
import com.unsoed.moviesta.base.BaseAuthActivity
import com.unsoed.moviesta.model.Actor
import com.unsoed.moviesta.model.ActorDetail
import com.unsoed.moviesta.model.Film
import com.unsoed.moviesta.network.RetrofitClient
import com.unsoed.moviesta.repository.FilmRepository
import com.unsoed.moviesta.adapter.MovieGridAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FilmsByActorActivity : BaseAuthActivity() {

    companion object {
        const val EXTRA_ACTOR = "extra_actor"
    }

    private lateinit var toolbar: Toolbar
    private lateinit var imgActorProfile: ImageView
    private lateinit var tvActorName: TextView
    private lateinit var tvKnownFor: TextView
    private lateinit var tvBirthday: TextView
    private lateinit var tvPlaceOfBirth: TextView
    private lateinit var tvBiography: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvEmptyState: TextView
    private lateinit var movieAdapter: MovieGridAdapter
    private lateinit var repository: FilmRepository

    private lateinit var actor: Actor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_films_by_actor)

        // Get actor data from intent
        actor = intent.getParcelableExtra(EXTRA_ACTOR) ?: run {
            finish()
            return
        }

        Log.d("FilmsByActorActivity", "Activity started with actor: ${actor.name}")

        setupViews()
        setupRecyclerView()
        loadActorDetail()
        loadActorFilms()
    }

    private fun setupViews() {
        toolbar = findViewById(R.id.toolbar)
        imgActorProfile = findViewById(R.id.img_actor_profile)
        tvActorName = findViewById(R.id.tv_actor_name)
        tvKnownFor = findViewById(R.id.tv_known_for)
        tvBirthday = findViewById(R.id.tv_birthday)
        tvPlaceOfBirth = findViewById(R.id.tv_place_of_birth)
        tvBiography = findViewById(R.id.tv_biography)
        recyclerView = findViewById(R.id.recyclerViewFilms)
        progressBar = findViewById(R.id.progressBar)
        tvEmptyState = findViewById(R.id.tvEmptyState)

        // Setup toolbar with back button
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = "Films by ${actor.name}"
            setHomeAsUpIndicator(R.drawable.ic_arrow_back)
        }
        
        toolbar.setNavigationOnClickListener { finish() }

        // Setup basic actor info
        tvActorName.text = actor.name
        tvKnownFor.text = actor.knownForDepartment ?: "Acting"

        // Load actor profile image
        val profileUrl = if (actor.profilePath != null) {
            "https://image.tmdb.org/t/p/w200${actor.profilePath}"
        } else {
            null
        }
        
        imgActorProfile.load(profileUrl) {
            placeholder(R.drawable.placeholder_actor)
            error(R.drawable.placeholder_actor)
            transformations(CircleCropTransformation())
            crossfade(true)
        }

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
            layoutManager = GridLayoutManager(this@FilmsByActorActivity, 2)
            adapter = movieAdapter
        }
    }

    private fun loadActorDetail() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val actorDetail = repository.getActorDetail(actor.id)
                
                withContext(Dispatchers.Main) {
                    updateActorInfo(actorDetail)
                }
            } catch (e: Exception) {
                Log.e("FilmsByActorActivity", "Error loading actor detail: ${e.message}")
            }
        }
    }

    private fun updateActorInfo(actorDetail: ActorDetail) {
        // Update birthday
        if (!actorDetail.birthday.isNullOrEmpty()) {
            tvBirthday.text = "Born: ${formatDate(actorDetail.birthday)}"
            tvBirthday.visibility = View.VISIBLE
        }

        // Update place of birth
        if (!actorDetail.placeOfBirth.isNullOrEmpty()) {
            tvPlaceOfBirth.text = actorDetail.placeOfBirth
            tvPlaceOfBirth.visibility = View.VISIBLE
        }

        // Update biography
        if (!actorDetail.biography.isNullOrEmpty()) {
            tvBiography.text = actorDetail.biography
            findViewById<View>(R.id.card_biography).visibility = View.VISIBLE
        }
    }

    private fun formatDate(dateString: String): String {
        return try {
            val parts = dateString.split("-")
            if (parts.size == 3) {
                val year = parts[0]
                val month = when (parts[1]) {
                    "01" -> "January"
                    "02" -> "February"
                    "03" -> "March"
                    "04" -> "April"
                    "05" -> "May"
                    "06" -> "June"
                    "07" -> "July"
                    "08" -> "August"
                    "09" -> "September"
                    "10" -> "October"
                    "11" -> "November"
                    "12" -> "December"
                    else -> parts[1]
                }
                val day = parts[2].toIntOrNull()?.toString() ?: parts[2]
                "$month $day, $year"
            } else {
                dateString
            }
        } catch (e: Exception) {
            dateString
        }
    }

    private fun loadActorFilms() {
        showLoading(true)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val films = repository.getActorMovies(actor.id)
                
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
                    Log.e("FilmsByActorActivity", "Error loading actor films: ${e.message}")
                }
            }
        }
    }

    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
        recyclerView.visibility = if (show) View.GONE else View.VISIBLE
    }

    private fun showEmptyState(show: Boolean) {
        tvEmptyState.visibility = if (show) View.VISIBLE else View.GONE
        recyclerView.visibility = if (show) View.GONE else View.VISIBLE
    }
}
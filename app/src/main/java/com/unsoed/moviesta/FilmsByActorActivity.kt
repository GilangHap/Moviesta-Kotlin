package com.unsoed.moviesta

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.CircleCropTransformation
import com.google.android.material.appbar.MaterialToolbar
import com.unsoed.moviesta.base.BaseAuthActivity
import com.unsoed.moviesta.model.Actor
import com.unsoed.moviesta.model.Film
import com.unsoed.moviesta.network.RetrofitClient
import com.unsoed.moviesta.repository.FilmRepository
import com.unsoed.moviesta.view.FilmAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FilmsByActorActivity : BaseAuthActivity() {

    companion object {
        const val EXTRA_ACTOR = "extra_actor"
    }

    private lateinit var toolbar: MaterialToolbar
    private lateinit var imgActorProfile: ImageView
    private lateinit var tvActorName: TextView
    private lateinit var tvKnownFor: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvEmptyState: TextView
    private lateinit var filmAdapter: FilmAdapter
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
        loadActorFilms()
    }

    private fun setupViews() {
        toolbar = findViewById(R.id.toolbar)
        imgActorProfile = findViewById(R.id.img_actor_profile)
        tvActorName = findViewById(R.id.tv_actor_name)
        tvKnownFor = findViewById(R.id.tv_known_for)
        recyclerView = findViewById(R.id.recyclerViewFilms)
        progressBar = findViewById(R.id.progressBar)
        tvEmptyState = findViewById(R.id.tvEmptyState)

        // Setup toolbar
        toolbar.setNavigationOnClickListener { finish() }
        toolbar.title = "Films by ${actor.name}"

        // Setup actor info
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
        filmAdapter = FilmAdapter(emptyList())

        recyclerView.apply {
            layoutManager = GridLayoutManager(this@FilmsByActorActivity, 2)
            adapter = filmAdapter
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
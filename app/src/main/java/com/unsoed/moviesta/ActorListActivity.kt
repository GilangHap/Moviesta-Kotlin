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
import com.unsoed.moviesta.adapter.ActorGridAdapter
import com.unsoed.moviesta.base.BaseAuthActivity
import com.unsoed.moviesta.model.Actor
import com.unsoed.moviesta.network.RetrofitClient
import com.unsoed.moviesta.repository.FilmRepository
import com.unsoed.moviesta.viewmodel.FilmViewModel
import com.unsoed.moviesta.viewmodel.FilmViewModelFactory

class ActorListActivity : BaseAuthActivity() {
    
    private lateinit var filmViewModel: FilmViewModel
    private lateinit var actorAdapter: ActorGridAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvTitle: TextView
    private lateinit var ivBack: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_actor_list)

        initViews()
        setupViewModel()
        setupRecyclerView()
        setupClickListeners()
        loadActors()
    }

    private fun initViews() {
        recyclerView = findViewById(R.id.recycler_view_actors)
        progressBar = findViewById(R.id.progress_bar)
        tvTitle = findViewById(R.id.tv_title)
        ivBack = findViewById(R.id.iv_back)
        
        tvTitle.text = "Popular Actors"
    }

    private fun setupViewModel() {
        val repository = FilmRepository(RetrofitClient.instance)
        val viewModelFactory = FilmViewModelFactory(repository)
        filmViewModel = ViewModelProvider(this, viewModelFactory)[FilmViewModel::class.java]
        
        // Observe actors
        filmViewModel.actors.observe(this) { actors ->
            progressBar.visibility = View.GONE
            actorAdapter.updateActors(actors)
        }
        
        filmViewModel.error.observe(this) { error ->
            progressBar.visibility = View.GONE
            // Handle error
        }
    }

    private fun setupRecyclerView() {
        actorAdapter = ActorGridAdapter { actor ->
            openActorDetail(actor)
        }
        
        recyclerView.apply {
            layoutManager = GridLayoutManager(this@ActorListActivity, 2)
            adapter = actorAdapter
        }
    }

    private fun setupClickListeners() {
        ivBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun loadActors() {
        progressBar.visibility = View.VISIBLE
        filmViewModel.loadPopularActors()
    }

    private fun openActorDetail(actor: Actor) {
        val intent = Intent(this, ActorActivity::class.java)
        intent.putExtra("ACTOR_ID", actor.id)
        intent.putExtra("ACTOR_NAME", actor.name)
        startActivity(intent)
    }
}
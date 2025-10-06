package com.unsoed.moviesta.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.unsoed.moviesta.DetailActivity
import com.unsoed.moviesta.adapter.FilmGridAdapter
import com.unsoed.moviesta.databinding.FragmentWatchedBinding
import com.unsoed.moviesta.model.Film
import com.unsoed.moviesta.repository.UserPreferencesRepository
import kotlinx.coroutines.launch

class WatchedFragment : Fragment() {
    
    private var _binding: FragmentWatchedBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var userPreferencesRepository: UserPreferencesRepository
    private lateinit var adapter: FilmGridAdapter
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWatchedBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        userPreferencesRepository = UserPreferencesRepository()
        
        setupRecyclerView()
        loadWatchedMovies()
    }
    
    private fun setupRecyclerView() {
        adapter = FilmGridAdapter { film ->
            val intent = Intent(context, DetailActivity::class.java)
            intent.putExtra("film", film)
            startActivity(intent)
        }
        
        binding.recyclerView.apply {
            layoutManager = GridLayoutManager(context, 2)
            adapter = this@WatchedFragment.adapter
        }
    }
    
    private fun loadWatchedMovies() {
        lifecycleScope.launch {
            try {
                val result = userPreferencesRepository.getUserPreferences()
                
                if (result.isSuccess) {
                    val preferences = result.getOrNull()
                    val watchedDetails = preferences?.watchedMoviesDetails ?: emptyList()
                    
                    val films = watchedDetails.map { watchedInfo ->
                        Film(
                            id = watchedInfo.movieId,
                            title = watchedInfo.title.ifEmpty { "Movie ${watchedInfo.movieId}" },
                            posterPath = watchedInfo.posterUrl.ifEmpty { null },
                            sinopsis = "",
                            releaseDate = "",
                            voteAverage = 0.0
                        )
                    }
                    
                    adapter.submitList(films)
                    
                    // Show/hide empty state
                    if (films.isEmpty()) {
                        binding.recyclerView.visibility = View.GONE
                        binding.emptyStateLayout.visibility = View.VISIBLE
                    } else {
                        binding.recyclerView.visibility = View.VISIBLE
                        binding.emptyStateLayout.visibility = View.GONE
                    }
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
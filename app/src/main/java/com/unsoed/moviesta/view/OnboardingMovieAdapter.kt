package com.unsoed.moviesta.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.google.android.material.card.MaterialCardView
import com.unsoed.moviesta.R
import com.unsoed.moviesta.model.Film

class OnboardingMovieAdapter(
    private val onMovieClick: (movie: Film, isWatched: Boolean) -> Unit
) : ListAdapter<Film, OnboardingMovieAdapter.MovieViewHolder>(MovieDiffCallback()) {

    private val watchedMovies = mutableSetOf<Int>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovieViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_onboarding_movie, parent, false)
        return MovieViewHolder(view)
    }

    override fun onBindViewHolder(holder: MovieViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class MovieViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cardMovie: MaterialCardView = itemView.findViewById(R.id.card_movie)
        private val imgPoster: ImageView = itemView.findViewById(R.id.img_poster)
        private val imgWatchedOverlay: ImageView = itemView.findViewById(R.id.img_watched_overlay)

        fun bind(movie: Film) {
            // Load poster image
            val posterUrl = "https://image.tmdb.org/t/p/w500${movie.posterPath}"
            imgPoster.load(posterUrl) {
                placeholder(R.drawable.ic_placeholder_movie)
                error(R.drawable.ic_placeholder_movie)
            }
            
            // Update card appearance based on watched status
            val isWatched = watchedMovies.contains(movie.id)
            updateMovieAppearance(isWatched)
            
            cardMovie.setOnClickListener {
                val newWatchedStatus = !isWatched
                
                if (newWatchedStatus) {
                    watchedMovies.add(movie.id)
                } else {
                    watchedMovies.remove(movie.id)
                }
                
                updateMovieAppearance(newWatchedStatus)
                onMovieClick(movie, newWatchedStatus)
            }
        }
        
        private fun updateMovieAppearance(isWatched: Boolean) {
            if (isWatched) {
                cardMovie.isChecked = true
                cardMovie.strokeWidth = 4
                imgWatchedOverlay.visibility = View.VISIBLE
                imgPoster.alpha = 0.7f
            } else {
                cardMovie.isChecked = false
                cardMovie.strokeWidth = 1
                imgWatchedOverlay.visibility = View.GONE
                imgPoster.alpha = 1.0f
            }
        }
    }

    class MovieDiffCallback : DiffUtil.ItemCallback<Film>() {
        override fun areItemsTheSame(oldItem: Film, newItem: Film): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Film, newItem: Film): Boolean {
            return oldItem == newItem
        }
    }
}
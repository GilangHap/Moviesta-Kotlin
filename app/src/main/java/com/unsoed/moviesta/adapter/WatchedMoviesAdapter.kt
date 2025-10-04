package com.unsoed.moviesta.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.unsoed.moviesta.R
import com.unsoed.moviesta.model.WatchedMovieInfo
import java.text.SimpleDateFormat
import java.util.*

class WatchedMoviesAdapter(
    private var watchedMovies: List<WatchedMovieInfo>,
    private val onItemClick: (WatchedMovieInfo) -> Unit
) : RecyclerView.Adapter<WatchedMoviesAdapter.WatchedMovieViewHolder>() {

    fun updateMovies(newWatchedMovies: List<WatchedMovieInfo>) {
        watchedMovies = newWatchedMovies
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WatchedMovieViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_watched_movie, parent, false)
        return WatchedMovieViewHolder(view)
    }

    override fun onBindViewHolder(holder: WatchedMovieViewHolder, position: Int) {
        holder.bind(watchedMovies[position])
    }

    override fun getItemCount(): Int = watchedMovies.size

    inner class WatchedMovieViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.iv_movie_poster)
        private val titleText: TextView = itemView.findViewById(R.id.tv_movie_title)
        private val ratingText: TextView = itemView.findViewById(R.id.tv_movie_rating)
        private val watchedDateText: TextView = itemView.findViewById(R.id.tv_watched_date)

        fun bind(watchedMovie: WatchedMovieInfo) {
            titleText.text = watchedMovie.title
            ratingText.text = "★ ${watchedMovie.rating}"
            
            // Format watched date
            val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            val watchedDate = Date(watchedMovie.watchedDate)
            watchedDateText.text = "Watched: ${dateFormat.format(watchedDate)}"
            
            // Load poster image
            val fullPosterUrl = if (watchedMovie.posterUrl.startsWith("http")) {
                watchedMovie.posterUrl
            } else {
                "https://image.tmdb.org/t/p/w500${watchedMovie.posterUrl}"
            }
            
            imageView.load(fullPosterUrl) {
                placeholder(R.drawable.ic_movie_placeholder)
                error(R.drawable.ic_movie_placeholder)
                crossfade(true)
            }
            
            itemView.setOnClickListener {
                onItemClick(watchedMovie)
            }
        }
    }
}
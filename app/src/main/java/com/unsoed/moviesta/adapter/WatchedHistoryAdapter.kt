package com.unsoed.moviesta.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.RoundedCornersTransformation
import com.unsoed.moviesta.R
import com.unsoed.moviesta.model.WatchedMovieInfo
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class WatchedHistoryAdapter(
    private val onItemClick: (WatchedMovieInfo) -> Unit
) : RecyclerView.Adapter<WatchedHistoryAdapter.WatchedHistoryViewHolder>() {

    private var watchedMovies = emptyList<WatchedMovieInfo>()

    fun updateWatchedMovies(movies: List<WatchedMovieInfo>) {
        watchedMovies = movies
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WatchedHistoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_watched_history, parent, false)
        return WatchedHistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: WatchedHistoryViewHolder, position: Int) {
        holder.bind(watchedMovies[position])
    }

    override fun getItemCount() = watchedMovies.size

    inner class WatchedHistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivPoster: ImageView = itemView.findViewById(R.id.iv_poster)
        private val tvTitle: TextView = itemView.findViewById(R.id.tv_title)
        private val tvRating: TextView = itemView.findViewById(R.id.tv_rating)
        private val tvYear: TextView = itemView.findViewById(R.id.tv_year)
        private val tvWatchDate: TextView = itemView.findViewById(R.id.tv_watch_date)
        private val ivRewatchIndicator: ImageView = itemView.findViewById(R.id.iv_rewatch_indicator)

        fun bind(watchedMovie: WatchedMovieInfo) {
            // Set basic movie info
            tvTitle.text = watchedMovie.title
            tvRating.text = String.format("%.1f", watchedMovie.rating)
            
            // Extract and display year
            tvYear.text = watchedMovie.releaseYear
            
            // Load poster image
            val posterUrl = if (watchedMovie.posterUrl.isNotEmpty()) {
                if (watchedMovie.posterUrl.startsWith("http")) {
                    watchedMovie.posterUrl
                } else {
                    "https://image.tmdb.org/t/p/w500${watchedMovie.posterUrl}"
                }
            } else {
                null
            }

            ivPoster.load(posterUrl) {
                crossfade(true)
                placeholder(R.drawable.ic_movie_empty)
                error(R.drawable.ic_movie_empty)
                transformations(RoundedCornersTransformation(16f))
            }

            // Format watch date
            tvWatchDate.text = formatWatchDate(watchedMovie.watchedDate)

            // Check if movie was watched multiple times (rewatch indicator)
            val watchCount = watchedMovies.count { it.movieId == watchedMovie.movieId }
            ivRewatchIndicator.visibility = if (watchCount > 1) View.VISIBLE else View.GONE

            // Set click listener
            itemView.setOnClickListener {
                onItemClick(watchedMovie)
            }
        }

        private fun formatWatchDate(timestamp: Long): String {
            val watchedDate = Date(timestamp)
            val now = Date()
            val diffInMillis = now.time - watchedDate.time
            val diffInDays = TimeUnit.MILLISECONDS.toDays(diffInMillis)

            return when {
                diffInDays == 0L -> {
                    val diffInHours = TimeUnit.MILLISECONDS.toHours(diffInMillis)
                    when {
                        diffInHours == 0L -> {
                            val diffInMinutes = TimeUnit.MILLISECONDS.toMinutes(diffInMillis)
                            if (diffInMinutes <= 1) "Just watched" else "Watched ${diffInMinutes}m ago"
                        }
                        diffInHours == 1L -> "Watched 1 hour ago"
                        else -> "Watched ${diffInHours}h ago"
                    }
                }
                diffInDays == 1L -> "Watched yesterday"
                diffInDays < 7 -> "Watched ${diffInDays} days ago"
                diffInDays < 30 -> {
                    val weeks = diffInDays / 7
                    if (weeks == 1L) "Watched 1 week ago" else "Watched ${weeks} weeks ago"
                }
                diffInDays < 365 -> {
                    val months = diffInDays / 30
                    if (months == 1L) "Watched 1 month ago" else "Watched ${months} months ago"
                }
                else -> {
                    val years = diffInDays / 365
                    if (years == 1L) "Watched 1 year ago" else "Watched ${years} years ago"
                }
            }
        }
    }
}
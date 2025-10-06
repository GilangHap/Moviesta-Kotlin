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
import com.unsoed.moviesta.model.Film

class MovieGridAdapter(
    private val onMovieClick: (Film) -> Unit
) : RecyclerView.Adapter<MovieGridAdapter.MovieViewHolder>() {

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
            
            // Extract year from release date with proper validation
            val year = try {
                if (!movie.releaseDate.isNullOrEmpty() && movie.releaseDate.length >= 4) {
                    movie.releaseDate.substring(0, 4)
                } else {
                    "N/A"
                }
            } catch (e: Exception) {
                "N/A"
            }
            movieYear.text = year
            
            // Load poster image with proper validation
            val posterUrl = if (!movie.posterPath.isNullOrEmpty()) {
                "https://image.tmdb.org/t/p/w500${movie.posterPath}"
            } else {
                null
            }
            
            moviePoster.load(posterUrl) {
                placeholder(R.drawable.ic_movie_placeholder)
                error(R.drawable.ic_movie_placeholder)
                crossfade(true)
            }
            
            itemView.setOnClickListener {
                onMovieClick(movie)
            }
        }
    }
}
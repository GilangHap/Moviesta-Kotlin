package com.unsoed.moviesta.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.unsoed.moviesta.R
import com.unsoed.moviesta.model.Film

class HorizontalMovieAdapter(
    private var movies: List<Film> = emptyList(),
    private val onItemClick: (Film) -> Unit = {}
) : RecyclerView.Adapter<HorizontalMovieAdapter.MovieViewHolder>() {

    class MovieViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val posterImageView: ImageView = itemView.findViewById(R.id.iv_poster)
        val titleTextView: TextView = itemView.findViewById(R.id.tv_title)
        val yearTextView: TextView = itemView.findViewById(R.id.tv_year)
        val ratingTextView: TextView = itemView.findViewById(R.id.tv_rating)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovieViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_movie_horizontal, parent, false)
        return MovieViewHolder(view)
    }

    override fun onBindViewHolder(holder: MovieViewHolder, position: Int) {
        val movie = movies[position]
        
        holder.titleTextView.text = movie.title
        holder.yearTextView.text = movie.releaseDate?.substring(0, 4) ?: "N/A"
        holder.ratingTextView.text = String.format("%.1f", movie.voteAverage ?: 0.0)

        // Load poster image
        val posterUrl = "https://image.tmdb.org/t/p/w500${movie.posterPath}"
        Glide.with(holder.itemView.context)
            .load(posterUrl)
            .apply(RequestOptions().transform(RoundedCorners(16)))
            .placeholder(android.R.drawable.ic_menu_gallery)
            .error(android.R.drawable.ic_menu_gallery)
            .into(holder.posterImageView)

        holder.itemView.setOnClickListener {
            onItemClick(movie)
        }
    }

    override fun getItemCount(): Int = movies.size

    fun updateMovies(newMovies: List<Film>) {
        movies = newMovies
        notifyDataSetChanged()
    }
}
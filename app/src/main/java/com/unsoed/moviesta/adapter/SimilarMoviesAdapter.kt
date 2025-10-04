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

class SimilarMoviesAdapter(
    private val movies: List<Film>,
    private val onMovieClick: (Film) -> Unit = {}
) : RecyclerView.Adapter<SimilarMoviesAdapter.SimilarMovieViewHolder>() {

    inner class SimilarMovieViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivPoster: ImageView = itemView.findViewById(R.id.iv_poster)
        val tvTitle: TextView = itemView.findViewById(R.id.tv_title)
        val tvRating: TextView = itemView.findViewById(R.id.tv_rating)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onMovieClick(movies[position])
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SimilarMovieViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_movie_horizontal, parent, false)
        return SimilarMovieViewHolder(view)
    }

    override fun onBindViewHolder(holder: SimilarMovieViewHolder, position: Int) {
        val movie = movies[position]
        
        holder.tvTitle.text = movie.title
        holder.tvRating.text = String.format("%.1f", movie.rating)
        
        // Use posterPath directly if it's a full URL, otherwise construct TMDB URL
        val posterUrl = if (movie.posterPath?.startsWith("http") == true) {
            movie.posterPath
        } else if (movie.posterPath != null) {
            "https://image.tmdb.org/t/p/w342${movie.posterPath}"
        } else {
            null
        }
        
        Glide.with(holder.itemView.context)
            .load(posterUrl)
            .apply(RequestOptions()
                .centerCrop()
                .transform(RoundedCorners(16)))
            .placeholder(android.R.drawable.ic_menu_gallery)
            .error(android.R.drawable.ic_menu_gallery)
            .into(holder.ivPoster)
    }

    override fun getItemCount(): Int = movies.size
}
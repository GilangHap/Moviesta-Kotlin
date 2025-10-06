package com.unsoed.moviesta.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.unsoed.moviesta.R
import com.unsoed.moviesta.model.Film

class FilmGridAdapter(
    private val onItemClick: (Film) -> Unit
) : ListAdapter<Film, FilmGridAdapter.FilmViewHolder>(FilmDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FilmViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_film_grid, parent, false)
        return FilmViewHolder(view)
    }

    override fun onBindViewHolder(holder: FilmViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class FilmViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivPoster: ImageView = itemView.findViewById(R.id.iv_poster)
        private val tvTitle: TextView = itemView.findViewById(R.id.tv_title)
        private val tvRating: TextView = itemView.findViewById(R.id.tv_rating)

        fun bind(film: Film) {
            tvTitle.text = film.title ?: "Unknown Title"
            tvRating.text = String.format("%.1f", film.voteAverage ?: 0.0)

            val posterUrl = if (film.posterPath.isNullOrEmpty()) {
                null
            } else {
                "https://image.tmdb.org/t/p/w500${film.posterPath}"
            }

            ivPoster.load(posterUrl) {
                placeholder(R.drawable.ic_movie_placeholder)
                error(R.drawable.ic_movie_placeholder)
                crossfade(true)
            }

            itemView.setOnClickListener {
                onItemClick(film)
            }
        }
    }

    private class FilmDiffCallback : DiffUtil.ItemCallback<Film>() {
        override fun areItemsTheSame(oldItem: Film, newItem: Film): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Film, newItem: Film): Boolean {
            return oldItem == newItem
        }
    }
}
package com.unsoed.moviesta.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.unsoed.moviesta.R
import com.unsoed.moviesta.model.Genre

class GenreAdapter(private var genres: List<Genre>) :
    RecyclerView.Adapter<GenreAdapter.GenreViewHolder>() {

    class GenreViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvGenre: TextView = itemView.findViewById(R.id.tv_genre_name)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GenreViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_genre, parent, false)
        return GenreViewHolder(view)
    }

    override fun onBindViewHolder(holder: GenreViewHolder, position: Int) {
        val genre = genres[position]
        holder.tvGenre.text = genre.name
    }

    override fun getItemCount(): Int = genres.size

    fun updateGenres(newGenres: List<Genre>) {
        genres = newGenres
        notifyDataSetChanged()
    }
}
package com.unsoed.moviesta.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.unsoed.moviesta.R
import com.unsoed.moviesta.model.Genre

class GenreAdapter(
    private val onItemClick: (Genre) -> Unit
) : RecyclerView.Adapter<GenreAdapter.GenreViewHolder>() {

    private var genres: List<Genre> = emptyList()

    inner class GenreViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val genreName: TextView = itemView.findViewById(R.id.textGenreName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GenreViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_genre, parent, false)
        return GenreViewHolder(view)
    }

    override fun onBindViewHolder(holder: GenreViewHolder, position: Int) {
        val genre = genres[position]
        
        holder.genreName.text = genre.name
        
        holder.itemView.setOnClickListener {
            onItemClick(genre)
        }
    }

    override fun getItemCount(): Int = genres.size

    fun updateGenres(newGenres: List<Genre>) {
        genres = newGenres
        notifyDataSetChanged()
    }
}
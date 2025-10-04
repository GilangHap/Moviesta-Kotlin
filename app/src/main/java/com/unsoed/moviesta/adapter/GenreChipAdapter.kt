package com.unsoed.moviesta.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.unsoed.moviesta.R
import com.unsoed.moviesta.model.Genre

class GenreChipAdapter(
    private val genres: List<Genre>,
    private val onGenreClick: (Genre) -> Unit = {}
) : RecyclerView.Adapter<GenreChipAdapter.GenreViewHolder>() {

    inner class GenreViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val chipGenre: Chip = itemView.findViewById(R.id.chip_genre)

        init {
            chipGenre.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onGenreClick(genres[position])
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GenreViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_genre_chip, parent, false)
        return GenreViewHolder(view)
    }

    override fun onBindViewHolder(holder: GenreViewHolder, position: Int) {
        val genre = genres[position]
        holder.chipGenre.text = genre.name
        holder.chipGenre.isClickable = true
        holder.chipGenre.isFocusable = true
    }

    override fun getItemCount(): Int = genres.size
}
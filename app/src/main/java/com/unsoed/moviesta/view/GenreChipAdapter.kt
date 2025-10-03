package com.unsoed.moviesta.view

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.unsoed.moviesta.FilmByGenreActivity
import com.unsoed.moviesta.R
import com.unsoed.moviesta.model.Genre

class GenreChipAdapter(private var genres: List<Genre>) : RecyclerView.Adapter<GenreChipAdapter.GenreViewHolder>() {

    class GenreViewHolder(val chip: Chip) : RecyclerView.ViewHolder(chip)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GenreViewHolder {
        val chip = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_genre_chip, parent, false) as Chip
        return GenreViewHolder(chip)
    }

    override fun onBindViewHolder(holder: GenreViewHolder, position: Int) {
        val genre = genres[position]
        holder.chip.text = genre.name
        
        // Add click listener to navigate to FilmByGenreActivity
        holder.chip.setOnClickListener {
            val context = holder.chip.context
            val intent = Intent(context, FilmByGenreActivity::class.java).apply {
                putExtra(FilmByGenreActivity.EXTRA_GENRE_ID, genre.id)
                putExtra(FilmByGenreActivity.EXTRA_GENRE_NAME, genre.name)
            }
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = genres.size

    fun updateGenres(newGenres: List<Genre>) {
        genres = newGenres
        notifyDataSetChanged()
    }
}

package com.unsoed.moviesta.view

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.unsoed.moviesta.FilmByGenreActivity
import com.unsoed.moviesta.R
import com.unsoed.moviesta.model.Genre

class GenreAdapter(private var genres: List<Genre>) :
    RecyclerView.Adapter<GenreAdapter.GenreViewHolder>() {

    class GenreViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvGenre: TextView = itemView.findViewById(R.id.textGenreName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GenreViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_genre, parent, false)
        return GenreViewHolder(view)
    }

    override fun onBindViewHolder(holder: GenreViewHolder, position: Int) {
        val genre = genres[position]
        holder.tvGenre.text = genre.name
        
        // Set up click listener with visual feedback
        holder.itemView.setOnClickListener { view ->
            Log.d("GenreAdapter", "Genre clicked: ${genre.name} (ID: ${genre.id})")
            
            // Add click animation
            view.animate()
                .scaleX(0.95f)
                .scaleY(0.95f)
                .setDuration(100)
                .withEndAction {
                    view.animate()
                        .scaleX(1.0f)
                        .scaleY(1.0f)
                        .setDuration(100)
                        .start()
                }
                .start()
            
            // Navigate to FilmByGenreActivity
            val context = holder.itemView.context
            val intent = Intent(context, FilmByGenreActivity::class.java).apply {
                putExtra(FilmByGenreActivity.EXTRA_GENRE_ID, genre.id)
                putExtra(FilmByGenreActivity.EXTRA_GENRE_NAME, genre.name)
            }
            Log.d("GenreAdapter", "Starting FilmByGenreActivity with genre: ${genre.name}")
            context.startActivity(intent)
        }
        
        // Also add ripple effect
        holder.itemView.isClickable = true
        holder.itemView.isFocusable = true
        
        // Add ripple effect using theme attribute
        val context = holder.itemView.context
        val attrs = intArrayOf(android.R.attr.selectableItemBackground)
        val typedArray = context.theme.obtainStyledAttributes(attrs)
        val backgroundDrawable = typedArray.getDrawable(0)
        typedArray.recycle()
        holder.itemView.foreground = backgroundDrawable
    }

    override fun getItemCount(): Int = genres.size

    fun updateGenres(newGenres: List<Genre>) {
        genres = newGenres
        notifyDataSetChanged()
    }
}
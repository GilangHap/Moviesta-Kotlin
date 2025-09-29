package com.unsoed.moviesta.view

import android.app.ActivityOptions
import android.content.Intent
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

class FilmAdapter(private var films: List<Film>) :
    RecyclerView.Adapter<FilmAdapter.FilmViewHolder>() {

    class FilmViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgPoster: ImageView = itemView.findViewById(R.id.img_poster)
        val tvTitle: TextView = itemView.findViewById(R.id.tv_title)
        val tvRating: TextView = itemView.findViewById(R.id.tv_rating)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FilmViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_film, parent, false)
        return FilmViewHolder(view)
    }

    override fun onBindViewHolder(holder: FilmViewHolder, position: Int) {
        val film = films[position]

        holder.tvTitle.text = film.title
        holder.tvRating.text = String.format("%.1f", film.rating)

        // Load poster image with Coil
        val posterUrl = "https://image.tmdb.org/t/p/w500${film.posterPath ?: ""}"
        
        // Debug: print URL
        println("Loading poster for ${film.title}: $posterUrl")
        
        holder.imgPoster.load(posterUrl) {
            placeholder(R.drawable.placeholder_image)
            error(R.drawable.placeholder_image)
            transformations(RoundedCornersTransformation(16f))
            crossfade(true)
        }
        
        holder.itemView.setOnClickListener { view ->
            // Add scale animation
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
            
            // Navigate to detail
            val intent = Intent(holder.itemView.context, com.unsoed.moviesta.DetailActivity::class.java)
            intent.putExtra(com.unsoed.moviesta.DetailActivity.EXTRA_FILM, film)
            
            // Create shared element transition if possible
            try {
                val options = ActivityOptions.makeSceneTransitionAnimation(
                    view.context as android.app.Activity,
                    holder.imgPoster,
                    "poster_transition"
                )
                holder.itemView.context.startActivity(intent, options.toBundle())
            } catch (e: Exception) {
                // Fallback to normal transition
                holder.itemView.context.startActivity(intent)
            }
        }
    }

    override fun getItemCount(): Int = films.size

    fun updateFilms(newFilms: List<Film>) {
        films = newFilms
        notifyDataSetChanged()
    }
}
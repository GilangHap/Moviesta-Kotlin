package com.unsoed.moviesta.view

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.RoundedCornersTransformation
import com.unsoed.moviesta.DetailActivity
import com.unsoed.moviesta.R
import com.unsoed.moviesta.model.Film

class RecommendationAdapter(private var films: List<Film>) :
    RecyclerView.Adapter<RecommendationAdapter.RecommendationViewHolder>() {

    class RecommendationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgPoster: ImageView = itemView.findViewById(R.id.img_recommendation_poster)
        val tvTitle: TextView = itemView.findViewById(R.id.tv_recommendation_title)
        val tvRating: TextView = itemView.findViewById(R.id.tv_recommendation_rating)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecommendationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recommendation, parent, false)
        return RecommendationViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecommendationViewHolder, position: Int) {
        val film = films[position]

        holder.tvTitle.text = film.safeTitle
        holder.tvRating.text = String.format("%.1f", film.rating)

        // Load poster image
        val posterUrl = "https://image.tmdb.org/t/p/w300${film.posterPath ?: ""}"
        
        holder.imgPoster.load(posterUrl) {
            placeholder(R.drawable.placeholder_image)
            error(R.drawable.placeholder_image)
            transformations(RoundedCornersTransformation(12f))
            crossfade(true)
        }
        
        // Click listener to open detail
        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, DetailActivity::class.java)
            intent.putExtra(DetailActivity.EXTRA_FILM, film)
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = films.size

    fun updateFilms(newFilms: List<Film>) {
        films = newFilms
        notifyDataSetChanged()
    }
}
package com.unsoed.moviesta.view

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import coil.load
import coil.transform.RoundedCornersTransformation
import com.google.android.material.button.MaterialButton
import com.unsoed.moviesta.DetailActivity
import com.unsoed.moviesta.R
import com.unsoed.moviesta.model.WatchlistItem
import java.text.SimpleDateFormat
import java.util.*

class WatchlistAdapter(
    private val onRemoveClick: (WatchlistItem) -> Unit
) : ListAdapter<WatchlistItem, WatchlistAdapter.WatchlistViewHolder>(WatchlistDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WatchlistViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_watchlist, parent, false)
        return WatchlistViewHolder(view)
    }

    override fun onBindViewHolder(holder: WatchlistViewHolder, position: Int) {
        val watchlistItem = getItem(position)
        holder.bind(watchlistItem)
    }

    inner class WatchlistViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imgPoster: ImageView = itemView.findViewById(R.id.img_poster)
        private val tvTitle: TextView = itemView.findViewById(R.id.tv_title)
        private val tvRating: TextView = itemView.findViewById(R.id.tv_rating)
        private val tvOverview: TextView = itemView.findViewById(R.id.tv_overview)
        private val tvAddedDate: TextView = itemView.findViewById(R.id.tv_added_date)
        private val btnRemove: MaterialButton = itemView.findViewById(R.id.btn_remove)

        fun bind(watchlistItem: WatchlistItem) {
            // Set title
            tvTitle.text = watchlistItem.title
            
            // Set rating
            tvRating.text = String.format("%.1f", watchlistItem.rating)
            
            // Set overview
            if (watchlistItem.overview.isNullOrBlank()) {
                tvOverview.text = "Tidak ada sinopsis tersedia"
                tvOverview.setTextColor(itemView.context.getColor(R.color.text_secondary))
            } else {
                tvOverview.text = watchlistItem.overview
                tvOverview.setTextColor(itemView.context.getColor(R.color.text_primary))
            }
            
            // Set added date
            val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
            val addedDate = Date(watchlistItem.addedDate)
            tvAddedDate.text = "Ditambahkan: ${dateFormat.format(addedDate)}"
            
            // Load poster image
            val posterUrl = "https://image.tmdb.org/t/p/w500${watchlistItem.posterPath ?: ""}"
            imgPoster.load(posterUrl) {
                placeholder(R.drawable.placeholder_image)
                error(R.drawable.placeholder_image)
                transformations(RoundedCornersTransformation(12f))
                crossfade(true)
            }
            
            // Set click listener for item
            itemView.setOnClickListener {
                // Add click animation
                itemView.animate()
                    .scaleX(0.95f)
                    .scaleY(0.95f)
                    .setDuration(100)
                    .withEndAction {
                        itemView.animate()
                            .scaleX(1.0f)
                            .scaleY(1.0f)
                            .setDuration(100)
                            .start()
                    }
                    .start()
                
                // Navigate to detail
                val intent = Intent(itemView.context, DetailActivity::class.java)
                intent.putExtra(DetailActivity.EXTRA_FILM, watchlistItem.toFilm())
                itemView.context.startActivity(intent)
            }
            
            // Set remove button click listener
            btnRemove.setOnClickListener {
                onRemoveClick(watchlistItem)
            }
        }
    }

    class WatchlistDiffCallback : DiffUtil.ItemCallback<WatchlistItem>() {
        override fun areItemsTheSame(oldItem: WatchlistItem, newItem: WatchlistItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: WatchlistItem, newItem: WatchlistItem): Boolean {
            return oldItem == newItem
        }
    }
}
package com.unsoed.moviesta.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.unsoed.moviesta.R
import com.unsoed.moviesta.model.GenrePreference

class OnboardingGenreAdapter(
    private val onGenreClick: (genre: com.unsoed.moviesta.model.Genre, isSelected: Boolean) -> Unit
) : ListAdapter<GenrePreference, OnboardingGenreAdapter.GenreViewHolder>(GenreDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GenreViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_onboarding_genre, parent, false)
        return GenreViewHolder(view)
    }

    override fun onBindViewHolder(holder: GenreViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class GenreViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cardGenre: MaterialCardView = itemView.findViewById(R.id.card_genre)
        private val tvGenreName: TextView = itemView.findViewById(R.id.tv_genre_name)

        fun bind(genrePreference: GenrePreference) {
            tvGenreName.text = genrePreference.genreName
            
            // Update card appearance based on selection
            updateCardAppearance(genrePreference.isSelected)
            
            cardGenre.setOnClickListener {
                val newSelection = !genrePreference.isSelected
                updateCardAppearance(newSelection)
                
                // Update the item in the list
                val updatedPreference = genrePreference.copy(isSelected = newSelection)
                val currentList = currentList.toMutableList()
                currentList[adapterPosition] = updatedPreference
                submitList(currentList)
                
                // Notify parent
                val genre = com.unsoed.moviesta.model.Genre(
                    id = genrePreference.genreId,
                    name = genrePreference.genreName
                )
                onGenreClick(genre, newSelection)
            }
        }
        
        private fun updateCardAppearance(isSelected: Boolean) {
            if (isSelected) {
                cardGenre.isChecked = true
                cardGenre.strokeWidth = 4
            } else {
                cardGenre.isChecked = false
                cardGenre.strokeWidth = 1
            }
        }
    }

    class GenreDiffCallback : DiffUtil.ItemCallback<GenrePreference>() {
        override fun areItemsTheSame(oldItem: GenrePreference, newItem: GenrePreference): Boolean {
            return oldItem.genreId == newItem.genreId
        }

        override fun areContentsTheSame(oldItem: GenrePreference, newItem: GenrePreference): Boolean {
            return oldItem == newItem
        }
    }
}
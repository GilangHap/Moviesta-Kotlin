package com.unsoed.moviesta.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.unsoed.moviesta.R

data class Category(
    val id: String,
    val name: String,
    val type: CategoryType,
    val isSelected: Boolean = false
)

enum class CategoryType {
    GENRE,
    POPULAR,
    TOP_RATED,
    UPCOMING,
    NOW_PLAYING,
    ALL
}

class CategoryAdapter(
    private var categories: List<Category>,
    private val onCategoryClick: (Category) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val chipCategory: Chip = itemView.findViewById(R.id.tv_category_name)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category, parent, false)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = categories[position]
        holder.chipCategory.text = category.name
        holder.chipCategory.isChecked = category.isSelected
        
        // Set icon berdasarkan type
        when (category.type) {
            CategoryType.POPULAR -> holder.chipCategory.setChipIconResource(R.drawable.ic_trending_up)
            CategoryType.TOP_RATED -> holder.chipCategory.setChipIconResource(R.drawable.ic_star)
            CategoryType.UPCOMING -> holder.chipCategory.setChipIconResource(R.drawable.ic_schedule)
            CategoryType.NOW_PLAYING -> holder.chipCategory.setChipIconResource(R.drawable.ic_play_circle)
            CategoryType.ALL -> holder.chipCategory.setChipIconResource(R.drawable.ic_apps)
            CategoryType.GENRE -> holder.chipCategory.setChipIconResource(R.drawable.ic_category)
        }
        
        holder.chipCategory.setOnClickListener {
            onCategoryClick(category)
        }
    }

    override fun getItemCount(): Int = categories.size

    fun updateCategories(newCategories: List<Category>) {
        categories = newCategories
        notifyDataSetChanged()
    }

    fun updateSelection(selectedId: String) {
        val updatedCategories = categories.map { category ->
            category.copy(isSelected = category.id == selectedId)
        }
        categories = updatedCategories
        notifyDataSetChanged()
    }
}
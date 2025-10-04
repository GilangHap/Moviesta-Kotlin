package com.unsoed.moviesta.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.unsoed.moviesta.R
import com.unsoed.moviesta.model.Actor

class ActorGridAdapter(
    private val onItemClick: (Actor) -> Unit
) : RecyclerView.Adapter<ActorGridAdapter.ActorViewHolder>() {

    private var actors: List<Actor> = emptyList()

    inner class ActorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val actorImage: ImageView = itemView.findViewById(R.id.iv_actor_photo)
        val actorName: TextView = itemView.findViewById(R.id.tv_actor_name)
        val popularityLabel: TextView = itemView.findViewById(R.id.tv_popularity)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActorViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_actor_grid, parent, false)
        return ActorViewHolder(view)
    }

    override fun onBindViewHolder(holder: ActorViewHolder, position: Int) {
        val actor = actors[position]
        
        holder.actorName.text = actor.name
        holder.popularityLabel.text = String.format("%.1f", actor.popularity ?: 0.0)
        
        // Load actor photo
        val photoUrl = "https://image.tmdb.org/t/p/w500${actor.profilePath}"
        Glide.with(holder.itemView.context)
            .load(photoUrl)
            .apply(RequestOptions().transform(RoundedCorners(16)))
            .placeholder(android.R.drawable.ic_menu_gallery)
            .error(android.R.drawable.ic_menu_gallery)
            .into(holder.actorImage)
        
        holder.itemView.setOnClickListener {
            onItemClick(actor)
        }
    }

    override fun getItemCount(): Int = actors.size

    fun updateActors(newActors: List<Actor>) {
        actors = newActors
        notifyDataSetChanged()
    }
}
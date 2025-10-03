package com.unsoed.moviesta.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.CircleCropTransformation
import com.unsoed.moviesta.R
import com.unsoed.moviesta.model.Actor

class ActorAdapter(
    private var actors: List<Actor>,
    private val onActorClick: (Actor) -> Unit
) : RecyclerView.Adapter<ActorAdapter.ActorViewHolder>() {

    class ActorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgProfile: ImageView = itemView.findViewById(R.id.img_actor_profile)
        val tvName: TextView = itemView.findViewById(R.id.tv_actor_name)
        val tvKnownFor: TextView = itemView.findViewById(R.id.tv_known_for)
        val tvPopularity: TextView = itemView.findViewById(R.id.tv_popularity)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActorViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_actor, parent, false)
        return ActorViewHolder(view)
    }

    override fun onBindViewHolder(holder: ActorViewHolder, position: Int) {
        val actor = actors[position]
        
        holder.tvName.text = actor.name
        holder.tvKnownFor.text = actor.knownForDepartment ?: "Acting"
        holder.tvPopularity.text = String.format("%.1f", actor.popularity)

        // Load profile image with Coil
        val profileUrl = if (actor.profilePath != null) {
            "https://image.tmdb.org/t/p/w200${actor.profilePath}"
        } else {
            null
        }
        
        holder.imgProfile.load(profileUrl) {
            placeholder(R.drawable.placeholder_actor)
            error(R.drawable.placeholder_actor)
            transformations(CircleCropTransformation())
            crossfade(true)
        }
        
        holder.itemView.setOnClickListener { view ->
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
            
            onActorClick(actor)
        }
    }

    override fun getItemCount(): Int = actors.size

    fun updateActors(newActors: List<Actor>) {
        actors = newActors
        notifyDataSetChanged()
    }
}
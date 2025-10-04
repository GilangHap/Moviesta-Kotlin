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

class CastAdapter(
    private val castList: List<Actor>,
    private val onCastClick: (Actor) -> Unit = {}
) : RecyclerView.Adapter<CastAdapter.CastViewHolder>() {

    inner class CastViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivCastPhoto: ImageView = itemView.findViewById(R.id.img_actor_profile)
        val tvCastName: TextView = itemView.findViewById(R.id.tv_actor_name)
        val tvCharacterName: TextView = itemView.findViewById(R.id.tv_character_name)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onCastClick(castList[position])
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CastViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cast, parent, false)
        return CastViewHolder(view)
    }

    override fun onBindViewHolder(holder: CastViewHolder, position: Int) {
        val cast = castList[position]
        
        holder.tvCastName.text = cast.name
        holder.tvCharacterName.text = cast.knownForDepartment ?: "Actor"
        
        // Use profilePath directly if it's a full URL, otherwise construct TMDB URL
        val profileUrl = if (cast.profilePath?.startsWith("http") == true) {
            cast.profilePath
        } else if (cast.profilePath != null) {
            "https://image.tmdb.org/t/p/w185${cast.profilePath}"
        } else {
            null
        }
        
        Glide.with(holder.itemView.context)
            .load(profileUrl)
            .apply(RequestOptions()
                .centerCrop()
                .transform(RoundedCorners(40)))  // Circular for actor photos
            .placeholder(R.drawable.ic_person)
            .error(R.drawable.ic_person)
            .into(holder.ivCastPhoto)
    }

    override fun getItemCount(): Int = castList.size
}
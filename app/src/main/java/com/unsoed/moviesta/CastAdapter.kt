package com.unsoed.moviesta

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.unsoed.moviesta.model.Cast

class CastAdapter(private var castList: List<Cast>) :
    RecyclerView.Adapter<CastAdapter.CastViewHolder>() {

    class CastViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val profileImage: ImageView = itemView.findViewById(R.id.img_actor_profile)
        val name: TextView = itemView.findViewById(R.id.tv_actor_name)
        val character: TextView = itemView.findViewById(R.id.tv_character_name)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CastViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cast, parent, false)
        return CastViewHolder(view)
    }

    override fun onBindViewHolder(holder: CastViewHolder, position: Int) {
        val actor = castList[position]

        holder.name.text = actor.name
        holder.character.text = actor.character

        // Load Gambar Profil Aktor
        val imageUrl = "https://image.tmdb.org/t/p/w200${actor.profilePath}"
        holder.profileImage.load(imageUrl) {
            crossfade(true)
            placeholder(android.R.drawable.sym_def_app_icon)
            error(android.R.drawable.sym_def_app_icon)
        }
    }

    override fun getItemCount(): Int = castList.size

    fun updateCast(newCast: List<Cast>) {
        castList = newCast
        notifyDataSetChanged()
    }
}
package com.unsoed.moviesta

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.unsoed.moviesta.model.Film

class FilmAdapter(private var films: List<Film>) :
    RecyclerView.Adapter<FilmAdapter.FilmViewHolder>() {

    class FilmViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.tv_title)
        val rating: TextView = itemView.findViewById(R.id.tv_rating)
//        val sinopsis: TextView = itemView.findViewById(R.id.tv_sinopsis_preview)
        val poster: ImageView = itemView.findViewById(R.id.img_poster)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FilmViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_film, parent, false)
        return FilmViewHolder(view)
    }

    override fun onBindViewHolder(holder: FilmViewHolder, position: Int) {
        val film = films[position]

        holder.title.text = film.title
        holder.rating.text = "⭐ ${film.rating}/10"
//        holder.sinopsis.text = film.sinopsis ?: "Sinopsis tidak tersedia."

        // Path dasar gambar TMDb
        val imageUrl = "https://image.tmdb.org/t/p/w500${film.posterPath}"

        // Load Gambar menggunakan Coil
        holder.poster.load(imageUrl) {
            crossfade(true)
            placeholder(android.R.drawable.ic_menu_gallery)
            error(android.R.drawable.ic_menu_close_clear_cancel)
        }

        holder.itemView.setOnClickListener {
            // 1. Buat Intent untuk berpindah ke DetailActivity
            val intent = Intent(holder.itemView.context, DetailActivity::class.java)

            // 2. Masukkan objek Film sebagai Parcelable ke Intent
            intent.putExtra(DetailActivity.EXTRA_FILM, film)

            // 3. Mulai Activity
            holder.itemView.context.startActivity(intent)
        }

        // TODO: Tambahkan listener di sini untuk navigasi ke Detail Activity
    }

    override fun getItemCount(): Int = films.size

    fun updateFilms(newFilms: List<Film>) {
        films = newFilms
        notifyDataSetChanged()
    }
}
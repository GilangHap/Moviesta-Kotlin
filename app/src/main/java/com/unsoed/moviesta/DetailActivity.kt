package com.unsoed.moviesta

import android.os.Build
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar // Diperlukan untuk Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.google.android.material.appbar.CollapsingToolbarLayout // Diperlukan untuk CollapsingToolbar
import com.unsoed.moviesta.model.Film
import com.unsoed.moviesta.network.RetrofitClient
import com.unsoed.moviesta.repository.FilmRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DetailActivity : AppCompatActivity() {

    // Konstanta kunci untuk Intent
    companion object {
        const val EXTRA_FILM = "extra_film"
    }

    // Deklarasi komponen untuk Cast
    private lateinit var castAdapter: CastAdapter
    private lateinit var repository: FilmRepository
    private lateinit var recyclerViewCast: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        // Mengambil objek Film dari Intent
        val film = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(EXTRA_FILM, Film::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra<Film>(EXTRA_FILM)
        }

        // 1. Inisialisasi Toolbar dan CollapsingToolbar
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        val collapsingToolbar: CollapsingToolbarLayout = findViewById(R.id.collapsing_toolbar)

        // Tetapkan Toolbar sebagai support action bar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // Tampilkan tombol kembali

        // 2. Inisialisasi Repository
        repository = FilmRepository(RetrofitClient.instance)

        // 3. Setup RecyclerView untuk Aktor
        recyclerViewCast = findViewById(R.id.recycler_view_cast)
        castAdapter = CastAdapter(emptyList())
        // Mengatur layout horizontal untuk daftar aktor
        recyclerViewCast.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recyclerViewCast.adapter = castAdapter

        // Tampilkan data jika film tidak null
        film?.let { detail ->
            // Mengambil referensi View
            val imgPoster: ImageView = findViewById(R.id.img_poster_detail)
            val tvTitle: TextView = findViewById(R.id.tv_title_detail)
            val tvRating: TextView = findViewById(R.id.tv_rating_detail)
            val tvSinopsis: TextView = findViewById(R.id.tv_sinopsis_detail)

            // Mengatur judul CollapsingToolbar (muncul saat digulir ke atas)
            collapsingToolbar.title = detail.title

            // Mengisi data ke View
            tvTitle.text = detail.title
            tvRating.text = "⭐ ${detail.rating} / 10"
            tvSinopsis.text = detail.sinopsis

            // Muat gambar poster
            val imageUrl = "https://image.tmdb.org/t/p/w500${detail.posterPath}"
            imgPoster.load(imageUrl) {
                crossfade(true)
            }

            // 4. Panggil fungsi untuk memuat data aktor
            loadCastData(detail.id)

        } ?: finish() // Tutup Activity jika tidak ada data film (error handling sederhana)
    }

    // Override untuk menghandle tombol kembali di Toolbar
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    /**
     * Memuat data aktor (Cast) dari API menggunakan Coroutine.
     */
    private fun loadCastData(movieId: Int) {
        // CoroutineScope(Dispatchers.IO) menjalankan operasi I/O (network call) di background thread
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Panggil repository untuk mendapatkan daftar Cast
                val castList = repository.getFilmCast(movieId)

                // Pindah ke Main thread untuk update UI (RecyclerView)
                CoroutineScope(Dispatchers.Main).launch {
                    castAdapter.updateCast(castList)
                }
            } catch (e: Exception) {
                // Tangani error, misalnya film tidak memiliki data Cast
                e.printStackTrace()
            }
        }
    }
}

package com.unsoed.moviesta

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar // Import Toolbar
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager // Menggunakan GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.unsoed.moviesta.network.RetrofitClient
import com.unsoed.moviesta.repository.FilmRepository
import com.unsoed.moviesta.viewmodel.FilmViewModel
import com.unsoed.moviesta.viewmodel.FilmViewModelFactory

class MainActivity : AppCompatActivity() {

    private lateinit var filmViewModel: FilmViewModel
    private lateinit var filmAdapter: FilmAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var searchView: SearchView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 1. Inisialisasi Toolbar
        val toolbar: Toolbar = findViewById(R.id.toolbar_main)
        setSupportActionBar(toolbar)
        // Judul sudah diatur di XML, tetapi bisa diatur ulang di sini jika perlu
        // supportActionBar?.title = "Moviesta"

        recyclerView = findViewById(R.id.recycler_view_films)
        searchView = findViewById(R.id.search_view)

        // 2. Inisialisasi Repository dan ViewModel
        val repository = FilmRepository(RetrofitClient.instance)
        val viewModelFactory = FilmViewModelFactory(repository)
        filmViewModel = ViewModelProvider(this, viewModelFactory)[FilmViewModel::class.java]

        // 3. Setup RecyclerView: MENGGUNAKAN GRID LAYOUT 2 KOLOM
        filmAdapter = FilmAdapter(emptyList()) // Mulai dengan list kosong
        recyclerView.layoutManager = GridLayoutManager(this, 2) // 2 kolom
        recyclerView.adapter = filmAdapter

        // 4. Amati (Observe) data dari ViewModel
        filmViewModel.films.observe(this) { films ->
            filmAdapter.updateFilms(films)
        }

        // 5. Implementasi Search
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let {
                    filmViewModel.searchFilms(it)
                }
                searchView.clearFocus()
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        })

        // Mengembalikan daftar populer jika tombol clear search ditekan
        searchView.setOnCloseListener {
            filmViewModel.loadPopularFilms()
            false
        }
    }
}

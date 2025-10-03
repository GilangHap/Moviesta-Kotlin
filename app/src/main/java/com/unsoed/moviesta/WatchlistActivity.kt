package com.unsoed.moviesta

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.unsoed.moviesta.model.WatchlistItem
import com.unsoed.moviesta.view.WatchlistAdapter
import com.unsoed.moviesta.viewmodel.WatchlistViewModel

class WatchlistActivity : AppCompatActivity() {

    private lateinit var watchlistViewModel: WatchlistViewModel
    private lateinit var watchlistAdapter: WatchlistAdapter
    
    // Views
    private lateinit var toolbar: MaterialToolbar
    private lateinit var recyclerView: RecyclerView
    private lateinit var layoutEmptyState: View
    private lateinit var layoutLoading: View
    private lateinit var layoutQuickActions: View
    private lateinit var tvWatchlistCount: TextView
    private lateinit var tvTotalRuntime: TextView
    private lateinit var btnBrowseFilms: MaterialButton
    private lateinit var btnSortDate: MaterialButton
    private lateinit var btnSortTitle: MaterialButton
    private lateinit var btnSortRating: MaterialButton
    
    // Data
    private var currentWatchlistItems = listOf<WatchlistItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_watchlist)

        setupViews()
        setupRecyclerView()
        setupViewModel()
        setupObservers()
        setupClickListeners()
    }

    private fun setupViews() {
        toolbar = findViewById(R.id.toolbar)
        recyclerView = findViewById(R.id.recycler_view_watchlist)
        layoutEmptyState = findViewById(R.id.layout_empty_state)
        layoutLoading = findViewById(R.id.layout_loading)
        layoutQuickActions = findViewById(R.id.layout_quick_actions)
        tvWatchlistCount = findViewById(R.id.tv_watchlist_count)
        tvTotalRuntime = findViewById(R.id.tv_total_runtime)
        btnBrowseFilms = findViewById(R.id.btn_browse_films)
        btnSortDate = findViewById(R.id.btn_sort_date)
        btnSortTitle = findViewById(R.id.btn_sort_title)
        btnSortRating = findViewById(R.id.btn_sort_rating)
        
        // Setup toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
    }

    private fun setupRecyclerView() {
        watchlistAdapter = WatchlistAdapter { watchlistItem ->
            showRemoveConfirmationDialog(watchlistItem)
        }
        
        recyclerView.apply {
            adapter = watchlistAdapter
            layoutManager = LinearLayoutManager(this@WatchlistActivity)
            setHasFixedSize(true)
        }
    }

    private fun setupViewModel() {
        watchlistViewModel = ViewModelProvider(this)[WatchlistViewModel::class.java]
    }

    private fun setupObservers() {
        // Observe watchlist items
        watchlistViewModel.watchlistItems.observe(this) { items ->
            currentWatchlistItems = items
            updateUI(items)
        }
        
        // Observe watchlist count
        watchlistViewModel.watchlistCount.observe(this) { count ->
            tvWatchlistCount.text = count.toString()
        }
        
        // Observe loading state
        watchlistViewModel.isLoading.observe(this) { isLoading ->
            if (isLoading) {
                showLoading()
            } else {
                hideLoading()
            }
        }
        
        // Observe error messages
        watchlistViewModel.errorMessage.observe(this) { errorMessage ->
            errorMessage?.let {
                showErrorMessage(it)
                watchlistViewModel.clearMessages()
            }
        }
        
        // Observe success messages
        watchlistViewModel.successMessage.observe(this) { successMessage ->
            successMessage?.let {
                showSuccessMessage(it)
                watchlistViewModel.clearMessages()
            }
        }
    }

    private fun setupClickListeners() {
        // Browse films button
        btnBrowseFilms.setOnClickListener {
            finish() // Go back to MainActivity
        }
        
        // Toolbar navigation
        toolbar.setNavigationOnClickListener {
            finish()
        }
        
        // Quick sort buttons
        btnSortDate.setOnClickListener {
            sortWatchlist(SortType.DATE_DESC)
        }
        
        btnSortTitle.setOnClickListener {
            sortWatchlist(SortType.TITLE_ASC)
        }
        
        btnSortRating.setOnClickListener {
            sortWatchlist(SortType.RATING_DESC)
        }
    }

    private fun updateUI(items: List<WatchlistItem>) {
        if (items.isEmpty()) {
            showEmptyState()
        } else {
            showWatchlistContent()
            watchlistAdapter.submitList(items)
            updateStats(items)
            
            // Show quick actions if there are items
            layoutQuickActions.visibility = View.VISIBLE
        }
    }

    private fun updateStats(items: List<WatchlistItem>) {
        // Update count (already handled by observer)
        
        // Calculate total runtime (estimated 2 hours per film since we don't have runtime data)
        val estimatedTotalHours = items.size * 2
        tvTotalRuntime.text = "${estimatedTotalHours}h"
    }

    private fun showWatchlistContent() {
        recyclerView.visibility = View.VISIBLE
        layoutEmptyState.visibility = View.GONE
        layoutLoading.visibility = View.GONE
    }

    private fun showEmptyState() {
        recyclerView.visibility = View.GONE
        layoutQuickActions.visibility = View.GONE
        layoutEmptyState.visibility = View.VISIBLE
        layoutLoading.visibility = View.GONE
    }

    private fun showLoading() {
        recyclerView.visibility = View.GONE
        layoutEmptyState.visibility = View.GONE
        layoutLoading.visibility = View.VISIBLE
    }

    private fun hideLoading() {
        layoutLoading.visibility = View.GONE
        // updateUI will handle showing the correct content
    }

    private fun showRemoveConfirmationDialog(watchlistItem: WatchlistItem) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Hapus dari Watchlist")
            .setMessage("Apakah Anda yakin ingin menghapus \"${watchlistItem.title}\" dari watchlist?")
            .setPositiveButton("Hapus") { _, _ ->
                watchlistViewModel.removeFromWatchlist(watchlistItem)
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun showClearAllConfirmationDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Hapus Semua Watchlist")
            .setMessage("Apakah Anda yakin ingin menghapus semua film dari watchlist? Tindakan ini tidak dapat dibatalkan.")
            .setPositiveButton("Hapus Semua") { _, _ ->
                watchlistViewModel.clearWatchlist()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun sortWatchlist(sortType: SortType) {
        val sortedItems = when (sortType) {
            SortType.DATE_DESC -> currentWatchlistItems.sortedByDescending { it.addedDate }
            SortType.DATE_ASC -> currentWatchlistItems.sortedBy { it.addedDate }
            SortType.TITLE_ASC -> currentWatchlistItems.sortedBy { it.title }
            SortType.TITLE_DESC -> currentWatchlistItems.sortedByDescending { it.title }
            SortType.RATING_DESC -> currentWatchlistItems.sortedByDescending { it.rating }
            SortType.RATING_ASC -> currentWatchlistItems.sortedBy { it.rating }
        }
        
        watchlistAdapter.submitList(sortedItems)
        
        val sortMessage = when (sortType) {
            SortType.DATE_DESC -> "Diurutkan berdasarkan terbaru ditambahkan"
            SortType.DATE_ASC -> "Diurutkan berdasarkan terlama ditambahkan"
            SortType.TITLE_ASC -> "Diurutkan berdasarkan judul A-Z"
            SortType.TITLE_DESC -> "Diurutkan berdasarkan judul Z-A"
            SortType.RATING_DESC -> "Diurutkan berdasarkan rating tertinggi"
            SortType.RATING_ASC -> "Diurutkan berdasarkan rating terendah"
        }
        
        showSuccessMessage(sortMessage)
    }

    private fun showErrorMessage(message: String) {
        Snackbar.make(recyclerView, message, Snackbar.LENGTH_LONG)
            .setAction("OK") { }
            .show()
    }

    private fun showSuccessMessage(message: String) {
        Snackbar.make(recyclerView, message, Snackbar.LENGTH_SHORT).show()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_watchlist, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            R.id.action_clear_all -> {
                if (currentWatchlistItems.isNotEmpty()) {
                    showClearAllConfirmationDialog()
                } else {
                    showErrorMessage("Watchlist sudah kosong")
                }
                true
            }
            R.id.action_sort_date_desc -> {
                sortWatchlist(SortType.DATE_DESC)
                true
            }
            R.id.action_sort_date_asc -> {
                sortWatchlist(SortType.DATE_ASC)
                true
            }
            R.id.action_sort_title_asc -> {
                sortWatchlist(SortType.TITLE_ASC)
                true
            }
            R.id.action_sort_title_desc -> {
                sortWatchlist(SortType.TITLE_DESC)
                true
            }
            R.id.action_sort_rating_desc -> {
                sortWatchlist(SortType.RATING_DESC)
                true
            }
            R.id.action_sort_rating_asc -> {
                sortWatchlist(SortType.RATING_ASC)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    enum class SortType {
        DATE_DESC, DATE_ASC, TITLE_ASC, TITLE_DESC, RATING_DESC, RATING_ASC
    }
}
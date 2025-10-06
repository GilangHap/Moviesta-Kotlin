package com.unsoed.moviesta

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import coil.load
import coil.transform.CircleCropTransformation
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.unsoed.moviesta.repository.UserPreferencesRepository
import com.unsoed.moviesta.view.CustomBottomNavigation
import kotlinx.coroutines.launch

class ProfileActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var userPreferencesRepository: UserPreferencesRepository
    private lateinit var bottomNavigation: CustomBottomNavigation
    
    // Views
    private lateinit var layoutLoading: LinearLayout
    private lateinit var layoutContent: androidx.core.widget.NestedScrollView
    private lateinit var ivProfilePhoto: ImageView
    private lateinit var tvUserName: TextView
    private lateinit var tvUserBio: TextView
    
    // Stats
    private lateinit var tvWatchedCount: TextView
    private lateinit var tvFavoriteGenresCount: TextView
    private lateinit var tvWatchlistCount: TextView
    
    // Cards
    private lateinit var cardWatchedMovies: MaterialCardView
    private lateinit var cardFavoriteGenres: MaterialCardView
    private lateinit var cardWatchlist: MaterialCardView
    
    // Buttons
    private lateinit var btnEditProfile: MaterialButton
    private lateinit var btnSettings: MaterialButton
    private lateinit var btnShare: MaterialButton
    private lateinit var btnLogout: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        initViews()
        setupRepository()
        setupBottomNavigation()
        setupClickListeners()
        loadUserProfile()
    }

    private fun initViews() {
        // Loading and content layouts
        layoutLoading = findViewById(R.id.layout_loading)
        layoutContent = findViewById(R.id.layout_content)
        
        // Profile info
        ivProfilePhoto = findViewById(R.id.iv_profile_photo)
        tvUserName = findViewById(R.id.tv_user_name)
        tvUserBio = findViewById(R.id.tv_user_bio)
        
        // Stats
        tvWatchedCount = findViewById(R.id.tv_watched_count)
        tvFavoriteGenresCount = findViewById(R.id.tv_favorite_genres_count)
        tvWatchlistCount = findViewById(R.id.tv_watchlist_count)
        
        // Stats cards
        cardWatchedMovies = findViewById(R.id.card_watched_movies)
        cardFavoriteGenres = findViewById(R.id.card_favorite_genres)
        cardWatchlist = findViewById(R.id.card_watchlist)
        
        // Action buttons
        btnEditProfile = findViewById(R.id.btn_edit_profile)
        btnSettings = findViewById(R.id.btn_settings)
        btnShare = findViewById(R.id.btn_share)
        btnLogout = findViewById(R.id.btn_logout)
        
        bottomNavigation = findViewById(R.id.bottom_navigation)
    }

    private fun setupRepository() {
        auth = FirebaseAuth.getInstance()
        userPreferencesRepository = UserPreferencesRepository()
    }

    private fun setupBottomNavigation() {
        bottomNavigation = findViewById(R.id.bottom_navigation)
        bottomNavigation.setOnTabSelectedListener { tab ->
            when (tab) {
                CustomBottomNavigation.NavigationTab.GENRE -> {
                    navigateToGenre()
                }
                CustomBottomNavigation.NavigationTab.ACTOR -> {
                    navigateToActor()
                }
                CustomBottomNavigation.NavigationTab.HOME -> {
                    navigateToHome()
                }
                CustomBottomNavigation.NavigationTab.HISTORY -> {
                    navigateToHistory()
                }
                CustomBottomNavigation.NavigationTab.PROFILE -> {
                    // Already on profile, do nothing
                }
            }
        }
        
        // Set initial selection to PROFILE
        bottomNavigation.selectTab(CustomBottomNavigation.NavigationTab.PROFILE)
    }

    private fun setupClickListeners() {
        btnEditProfile.setOnClickListener {
            showEditProfileDialog()
        }
        
        cardWatchedMovies.setOnClickListener {
            navigateToHistory()
        }
        
        cardFavoriteGenres.setOnClickListener {
            navigateToGenre()
        }
        
        cardWatchlist.setOnClickListener {
            // Could navigate to a dedicated watchlist activity in the future
            Toast.makeText(this, "Watchlist feature coming soon", Toast.LENGTH_SHORT).show()
        }
        
        btnSettings.setOnClickListener {
            showSettingsDialog()
        }
        
        btnShare.setOnClickListener {
            shareProfile()
        }
        
        btnLogout.setOnClickListener {
            showLogoutDialog()
        }
        
        ivProfilePhoto.setOnClickListener {
            showProfilePhotoDialog()
        }
    }

    private fun loadUserProfile() {
        showLoading()
        
        val currentUser = auth.currentUser
        if (currentUser == null) {
            redirectToLogin()
            return
        }
        
        // Load basic user info
        loadBasicUserInfo(currentUser)
        
        // Load user statistics
        loadUserStatistics()
    }

    private fun loadBasicUserInfo(user: FirebaseUser) {
        tvUserName.text = user.displayName ?: "Movie Enthusiast"
        tvUserBio.text = "I don't like to talk about myself.\nI prefer to talk about movies."
        
        // Load profile photo
        val photoUrl = user.photoUrl
        ivProfilePhoto.load(photoUrl) {
            placeholder(R.drawable.ic_person_placeholder)
            error(R.drawable.ic_person_placeholder)
            transformations(CircleCropTransformation())
            crossfade(true)
        }
    }

    private fun loadUserStatistics() {
        lifecycleScope.launch {
            try {
                val result = userPreferencesRepository.getUserPreferences()
                
                if (result.isSuccess) {
                    val preferences = result.getOrNull()
                    
                    // Update watched movies count
                    val watchedCount = preferences?.watchedMoviesDetails?.size ?: 0
                    tvWatchedCount.text = watchedCount.toString()
                    
                    // Update watchlist count (use watched movies for now since watchlist field doesn't exist)
                    val watchlistCount = 0 // No watchlist field available
                    tvWatchlistCount.text = watchlistCount.toString()
                    
                    // Update favorite genres count
                    val favoriteGenresCount = preferences?.favoriteGenres?.size ?: 0
                    tvFavoriteGenresCount.text = favoriteGenresCount.toString()
                }
                
                hideLoading()
                
            } catch (e: Exception) {
                hideLoading()
                Toast.makeText(this@ProfileActivity, "Failed to load profile data", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showLoading() {
        layoutLoading.visibility = View.VISIBLE
        layoutContent.visibility = View.GONE
    }

    private fun hideLoading() {
        layoutLoading.visibility = View.GONE
        layoutContent.visibility = View.VISIBLE
    }

    private fun showEditProfileDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Edit Profile")
            .setMessage("Profile editing will be available in the next update!")
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun showSettingsDialog() {
        val options = arrayOf("Notifications", "Privacy", "Data & Storage", "About")
        
        MaterialAlertDialogBuilder(this)
            .setTitle("Settings")
            .setItems(options) { _, which ->
                val selected = options[which]
                Toast.makeText(this, "$selected settings coming soon!", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun shareProfile() {
        val shareText = "Check out my movie profile on Moviesta! I've watched ${tvWatchedCount.text} movies. Join me on Moviesta!"
        
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
            putExtra(Intent.EXTRA_SUBJECT, "My Moviesta Profile")
        }
        
        startActivity(Intent.createChooser(shareIntent, "Share Profile"))
    }

    private fun showProfilePhotoDialog() {
        val options = arrayOf("View Photo", "Change Photo", "Remove Photo")
        
        MaterialAlertDialogBuilder(this)
            .setTitle("Profile Photo")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> viewFullPhoto()
                    1 -> changePhoto()
                    2 -> removePhoto()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun viewFullPhoto() {
        // TODO: Implement full photo view
        Toast.makeText(this, "Photo viewer coming soon!", Toast.LENGTH_SHORT).show()
    }

    private fun changePhoto() {
        // TODO: Implement photo picker
        Toast.makeText(this, "Photo picker coming soon!", Toast.LENGTH_SHORT).show()
    }

    private fun removePhoto() {
        // TODO: Implement photo removal
        Toast.makeText(this, "Photo removal coming soon!", Toast.LENGTH_SHORT).show()
    }

    private fun showLogoutDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout from your account?")
            .setPositiveButton("Logout") { _, _ ->
                performLogout()
            }
            .setNegativeButton("Cancel", null)
            .setIcon(R.drawable.ic_logout)
            .show()
    }

    private fun performLogout() {
        try {
            auth.signOut()
            redirectToLogin()
            Toast.makeText(this, "Successfully logged out", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Logout failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun redirectToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun navigateToGenre() {
        val intent = Intent(this, GenreActivity::class.java)
        startActivity(intent)
        finish()
    }
    
    private fun navigateToActor() {
        val intent = Intent(this, ActorActivity::class.java)
        startActivity(intent)
        finish()
    }
    
    private fun navigateToHome() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
    
    private fun navigateToHistory() {
        val intent = Intent(this, HistoryActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onResume() {
        super.onResume()
        // Refresh profile data when returning to this activity
        loadUserProfile()
    }
}
package com.unsoed.moviesta

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
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
    private lateinit var ivProfilePhoto: ImageView
    private lateinit var tvUserName: TextView
    private lateinit var tvUserEmail: TextView
    private lateinit var tvJoinDate: TextView
    private lateinit var tvWatchedCount: TextView
    private lateinit var tvFavoriteGenres: TextView
    private lateinit var tvAccountType: TextView
    
    // Stats Cards
    private lateinit var cardWatchedMovies: MaterialCardView
    private lateinit var cardFavoriteGenres: MaterialCardView
    private lateinit var cardWatchTime: MaterialCardView
    
    // Action Buttons
    private lateinit var btnEditProfile: MaterialButton
    private lateinit var btnWatchHistory: MaterialButton
    private lateinit var btnSettings: MaterialButton
    private lateinit var btnShare: MaterialButton
    private lateinit var btnLogout: MaterialButton
    
    // Loading and content views
    private lateinit var layoutLoading: View
    private lateinit var layoutContent: View

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
        // Profile info
        ivProfilePhoto = findViewById(R.id.iv_profile_photo)
        tvUserName = findViewById(R.id.tv_user_name)
        tvUserEmail = findViewById(R.id.tv_user_email)
        tvJoinDate = findViewById(R.id.tv_join_date)
        tvWatchedCount = findViewById(R.id.tv_watched_count)
        tvFavoriteGenres = findViewById(R.id.tv_favorite_genres)
        tvAccountType = findViewById(R.id.tv_account_type)
        
        // Stats cards
        cardWatchedMovies = findViewById(R.id.card_watched_movies)
        cardFavoriteGenres = findViewById(R.id.card_favorite_genres)
        cardWatchTime = findViewById(R.id.card_watch_time)
        
        // Action buttons
        btnEditProfile = findViewById(R.id.btn_edit_profile)
        btnWatchHistory = findViewById(R.id.btn_watch_history)
        btnSettings = findViewById(R.id.btn_settings)
        btnShare = findViewById(R.id.btn_share)
        btnLogout = findViewById(R.id.btn_logout)
        
        // Layout views
        layoutLoading = findViewById(R.id.layout_loading)
        layoutContent = findViewById(R.id.layout_content)
        bottomNavigation = findViewById(R.id.bottom_navigation)
    }

    private fun setupRepository() {
        auth = FirebaseAuth.getInstance()
        userPreferencesRepository = UserPreferencesRepository()
    }

    private fun setupBottomNavigation() {
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
        
        btnWatchHistory.setOnClickListener {
            navigateToHistory()
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
        
        cardWatchedMovies.setOnClickListener {
            navigateToHistory()
        }
        
        cardFavoriteGenres.setOnClickListener {
            navigateToGenre()
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
        tvUserEmail.text = user.email ?: "No email"
        
        // Format join date
        val joinDate = user.metadata?.creationTimestamp?.let { timestamp ->
            val date = java.util.Date(timestamp)
            java.text.SimpleDateFormat("MMMM yyyy", java.util.Locale.getDefault()).format(date)
        } ?: "Unknown"
        tvJoinDate.text = "Member since $joinDate"
        
        // Load profile photo
        val photoUrl = user.photoUrl
        ivProfilePhoto.load(photoUrl) {
            placeholder(R.drawable.ic_person_placeholder)
            error(R.drawable.ic_person_placeholder)
            transformations(CircleCropTransformation())
            crossfade(true)
        }
        
        // Set account type
        tvAccountType.text = if (user.isEmailVerified) "Verified Account" else "Basic Account"
    }

    private fun loadUserStatistics() {
        lifecycleScope.launch {
            try {
                val preferencesResult = userPreferencesRepository.getUserPreferences()
                
                if (preferencesResult.isSuccess) {
                    val preferences = preferencesResult.getOrNull()
                    
                    // Update watched movies count
                    val watchedCount = preferences?.watchedMoviesDetails?.size ?: 0
                    tvWatchedCount.text = "$watchedCount"
                    findViewById<TextView>(R.id.tv_watched_movies_stat).text = "$watchedCount Movies Watched"
                    
                    // Update favorite genres
                    val genreCount = preferences?.favoriteGenres?.size ?: 0
                    findViewById<TextView>(R.id.tv_favorite_genres_stat).text = "$genreCount Favorite Genres"
                    
                    // Calculate approximate watch time (assuming 2 hours per movie)
                    val watchTimeHours = watchedCount * 2
                    findViewById<TextView>(R.id.tv_watch_time_stat).text = "${watchTimeHours}h Watch Time"
                    
                    // Show favorite genres names
                    val genreNames = preferences?.favoriteGenres?.take(3)?.joinToString(", ") ?: "Not set"
                    tvFavoriteGenres.text = if (genreNames.isNotEmpty() && genreNames != "Not set") {
                        genreNames
                    } else {
                        "Explore genres to set favorites"
                    }
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
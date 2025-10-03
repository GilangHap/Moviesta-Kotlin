package com.unsoed.moviesta.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.unsoed.moviesta.R

class CustomBottomNavigation @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val navGenre: LinearLayout
    private val navActor: LinearLayout
    private val navHome: LinearLayout
    private val navHistory: LinearLayout
    private val navProfile: LinearLayout

    private val iconGenre: ImageView
    private val iconActor: ImageView
    private val iconHome: ImageView
    private val iconHistory: ImageView
    private val iconProfile: ImageView

    private val labelGenre: TextView
    private val labelActor: TextView
    private val labelHome: TextView
    private val labelHistory: TextView
    private val labelProfile: TextView

    private var currentSelectedTab = NavigationTab.HOME
    private var onTabSelectedListener: ((NavigationTab) -> Unit)? = null

    enum class NavigationTab {
        GENRE, ACTOR, HOME, HISTORY, PROFILE
    }

    init {
        LayoutInflater.from(context).inflate(R.layout.bottom_navigation_custom, this, true)

        // Initialize views
        navGenre = findViewById(R.id.nav_genre)
        navActor = findViewById(R.id.nav_actor)
        navHome = findViewById(R.id.nav_home)
        navHistory = findViewById(R.id.nav_history)
        navProfile = findViewById(R.id.nav_profile)

        iconGenre = findViewById(R.id.icon_genre)
        iconActor = findViewById(R.id.icon_actor)
        iconHome = findViewById(R.id.icon_home)
        iconHistory = findViewById(R.id.icon_history)
        iconProfile = findViewById(R.id.icon_profile)

        labelGenre = findViewById(R.id.label_genre)
        labelActor = findViewById(R.id.label_actor)
        labelHome = findViewById(R.id.label_home)
        labelHistory = findViewById(R.id.label_history)
        labelProfile = findViewById(R.id.label_profile)

        setupClickListeners()
        updateTabStates()
    }

    private fun setupClickListeners() {
        navGenre.setOnClickListener {
            selectTab(NavigationTab.GENRE)
        }

        navActor.setOnClickListener {
            selectTab(NavigationTab.ACTOR)
        }

        navHome.setOnClickListener {
            selectTab(NavigationTab.HOME)
        }

        navHistory.setOnClickListener {
            selectTab(NavigationTab.HISTORY)
        }

        navProfile.setOnClickListener {
            selectTab(NavigationTab.PROFILE)
        }
    }

    fun selectTab(tab: NavigationTab) {
        if (currentSelectedTab != tab) {
            currentSelectedTab = tab
            updateTabStates()
            onTabSelectedListener?.invoke(tab)
        }
    }

    private fun updateTabStates() {
        // Reset all tabs to unselected state
        resetTabState(iconGenre, labelGenre)
        resetTabState(iconActor, labelActor)
        resetTabState(iconHistory, labelHistory)
        resetTabState(iconProfile, labelProfile)

        // Set selected tab state
        when (currentSelectedTab) {
            NavigationTab.GENRE -> setSelectedState(iconGenre, labelGenre)
            NavigationTab.ACTOR -> setSelectedState(iconActor, labelActor)
            NavigationTab.HOME -> {
                // Home is always styled differently (primary color)
                iconHome.imageTintList = ContextCompat.getColorStateList(context, android.R.color.white)
                labelHome.setTextColor(ContextCompat.getColor(context, R.color.nav_text_selected))
                labelHome.textSize = 12f
                labelHome.typeface = android.graphics.Typeface.DEFAULT_BOLD
            }
            NavigationTab.HISTORY -> setSelectedState(iconHistory, labelHistory)
            NavigationTab.PROFILE -> setSelectedState(iconProfile, labelProfile)
        }
    }

    private fun resetTabState(icon: ImageView, label: TextView) {
        if (icon == iconHome) return // Home icon always stays white

        icon.imageTintList = ContextCompat.getColorStateList(context, R.color.nav_icon_unselected)
        label.setTextColor(ContextCompat.getColor(context, R.color.nav_text_unselected))
        label.textSize = 11f
        label.typeface = android.graphics.Typeface.DEFAULT
    }

    private fun setSelectedState(icon: ImageView, label: TextView) {
        icon.imageTintList = ContextCompat.getColorStateList(context, R.color.nav_icon_selected)
        label.setTextColor(ContextCompat.getColor(context, R.color.nav_text_selected))
        label.textSize = 12f
        label.typeface = android.graphics.Typeface.DEFAULT_BOLD
    }

    fun setOnTabSelectedListener(listener: (NavigationTab) -> Unit) {
        onTabSelectedListener = listener
    }

    fun getCurrentTab(): NavigationTab = currentSelectedTab
}
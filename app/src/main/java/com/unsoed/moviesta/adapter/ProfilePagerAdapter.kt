package com.unsoed.moviesta.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.unsoed.moviesta.fragment.FavoritesFragment
import com.unsoed.moviesta.fragment.WatchedFragment
import com.unsoed.moviesta.fragment.WatchlistFragment

class ProfilePagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {
    
    override fun getItemCount(): Int = 3
    
    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> WatchlistFragment()
            1 -> FavoritesFragment()
            2 -> WatchedFragment()
            else -> WatchlistFragment()
        }
    }
}
package com.unsoed.moviesta.utils

import android.content.Context
import android.util.Log
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability

object GoogleServicesHelper {
    private const val TAG = "GoogleServicesHelper"
    
    fun checkGooglePlayServices(context: Context): Boolean {
        val googleApiAvailability = GoogleApiAvailability.getInstance()
        val resultCode = googleApiAvailability.isGooglePlayServicesAvailable(context)
        
        return when (resultCode) {
            ConnectionResult.SUCCESS -> {
                Log.d(TAG, "Google Play Services available")
                true
            }
            ConnectionResult.SERVICE_MISSING -> {
                Log.e(TAG, "Google Play Services missing")
                false
            }
            ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED -> {
                Log.e(TAG, "Google Play Services needs update")
                false
            }
            ConnectionResult.SERVICE_DISABLED -> {
                Log.e(TAG, "Google Play Services disabled")
                false
            }
            else -> {
                Log.e(TAG, "Google Play Services error: $resultCode")
                false
            }
        }
    }
    
    fun getGooglePlayServicesStatus(context: Context): String {
        val googleApiAvailability = GoogleApiAvailability.getInstance()
        val resultCode = googleApiAvailability.isGooglePlayServicesAvailable(context)
        
        return when (resultCode) {
            ConnectionResult.SUCCESS -> "Google Play Services available"
            ConnectionResult.SERVICE_MISSING -> "Google Play Services missing"
            ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED -> "Google Play Services needs update"
            ConnectionResult.SERVICE_DISABLED -> "Google Play Services disabled"
            else -> "Google Play Services error: $resultCode"
        }
    }
}
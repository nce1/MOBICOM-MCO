package com.mobdeve.s18.group5.bayanihanspots.utils
import android.location.Location

class LocationUtil {
    companion object{
        fun calculateDistance(userLat: Double, userLng: Double, venueLat: Double, venueLng: Double): String {
            val results = FloatArray(1)
            Location.distanceBetween(userLat, userLng, venueLat, venueLng, results)
            val distanceInMeters = results[0]
            return if (distanceInMeters > 1000) {
                String.format("%.1f km", distanceInMeters / 1000)
            } else {
                "${distanceInMeters.toInt()} m"
            }
        }
    }
}
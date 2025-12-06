package com.mobdeve.s18.group5.bayanihanspots.data.repository

import android.content.Context
import android.location.Location
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.mobdeve.s18.group5.bayanihanspots.data.local.BayanihanDatabase
import com.mobdeve.s18.group5.bayanihanspots.data.local.entity.SpotEntity
import com.mobdeve.s18.group5.bayanihanspots.data.spots.Spot
import com.mobdeve.s18.group5.bayanihanspots.data.spots.toSpot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Locale

/**
 * Repository that implements offline-first strategy:
 * 1. UI always observes Room database (single source of truth)
 * 2. Firestore syncs to Room in the background
 * 3. New data created offline queued as pending uploads
 */
class OfflineFirstSpotRepository(
    private val context: Context,
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val database = BayanihanDatabase.getInstance(context)
    private val spotDao = database.spotDao()
    private val pendingUploadDao = database.pendingUploadDao()

    companion object {
        private const val TAG = "OfflineFirstSpotRepo"
        private const val SPOTS_COLLECTION = "spots"

        @Volatile
        private var INSTANCE: OfflineFirstSpotRepository? = null

        fun getInstance(context: Context): OfflineFirstSpotRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: OfflineFirstSpotRepository(context.applicationContext).also {
                    INSTANCE = it
                }
            }
        }
    }

    /**
     * Observe approved spots from Room (offline-first)
     */
    fun observeApprovedSpots(): Flow<List<Spot>> {
        // Start background sync when observing
        syncFromFirestore()
        return spotDao.observeApprovedSpots().map { entities ->
            entities.map { it.toSpot() }
        }
    }

    /**
     * Observe all spots from Room
     */
    fun observeAllSpots(): Flow<List<Spot>> {
        syncFromFirestore()
        return spotDao.observeAllSpots().map { entities ->
            entities.map { it.toSpot() }
        }
    }

    /**
     * Observe spots by user
     */
    fun observeSpotsByUser(userId: String): Flow<List<Spot>> {
        syncUserSpotsFromFirestore(userId)
        return spotDao.observeSpotsByUser(userId).map { entities ->
            entities.map { it.toSpot() }
        }
    }

    /**
     * Observe spots by type/category
     */
    fun observeSpotsByType(type: String): Flow<List<Spot>> {
        return spotDao.observeSpotsByType(type).map { entities ->
            entities.map { it.toSpot() }
        }
    }

    /**
     * Get spot by ID (from Room)
     */
    suspend fun getSpotById(spotId: String): Spot? {
        return spotDao.getSpotById(spotId)?.toSpot()
    }

    /**
     * Observe a specific spot
     */
    fun observeSpotById(spotId: String): Flow<Spot?> {
        return spotDao.observeSpotById(spotId).map { it?.toSpot() }
    }

    /**
     * Sync approved spots from Firestore to Room (background)
     */
    fun syncFromFirestore() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val snapshot = firestore.collection(SPOTS_COLLECTION)
                    .whereEqualTo("approvalStatus", "APPROVED")
                    .get()
                    .await()

                val spots = snapshot.documents.mapNotNull { doc ->
                    doc.toSpot()?.let { SpotEntity.fromSpot(it) }
                }

                // Replace all spots to ensure deleted spots are removed
                spotDao.replaceAllSpots(spots)
                Log.d(TAG, "Synced ${spots.size} spots from Firestore")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to sync from Firestore: ${e.message}")
                // App continues to work with cached data
            }
        }
    }

    /**
     * Sync user-specific spots from Firestore
     */
    private fun syncUserSpotsFromFirestore(userId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val snapshot = firestore.collection(SPOTS_COLLECTION)
                    .whereEqualTo("userID", userId)
                    .get()
                    .await()

                val spots = snapshot.documents.mapNotNull { doc ->
                    doc.toSpot()?.let { SpotEntity.fromSpot(it) }
                }

                if (spots.isNotEmpty()) {
                    spotDao.insertSpots(spots)
                    Log.d(TAG, "Synced ${spots.size} user spots from Firestore")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to sync user spots: ${e.message}")
            }
        }
    }

    /**
     * Force refresh from Firestore
     */
    suspend fun forceRefresh(): Result<Unit> {
        return try {
            val snapshot = firestore.collection(SPOTS_COLLECTION)
                .whereEqualTo("approvalStatus", "APPROVED")
                .get()
                .await()

            val spots = snapshot.documents.mapNotNull { doc ->
                doc.toSpot()?.let { SpotEntity.fromSpot(it) }
            }

            spotDao.replaceAllSpots(spots)
            Log.d(TAG, "Force refreshed ${spots.size} spots")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Force refresh failed: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Update distances for spots based on user location
     */
    fun updateDistances(spots: List<Spot>, userLocation: Location?): List<Spot> {
        if (userLocation == null) return spots
        return spots.map { spot ->
            val spotLat = spot.coordinates?.latitude ?: 0.0
            val spotLng = spot.coordinates?.longitude ?: 0.0

            val distString = calculateDistance(
                userLat = userLocation.latitude,
                userLng = userLocation.longitude,
                venueLat = spotLat,
                venueLng = spotLng
            )

            spot.copy(distanceString = " • $distString")
        }
    }

    private fun calculateDistance(userLat: Double, userLng: Double, venueLat: Double, venueLng: Double): String {
        val results = FloatArray(1)
        Location.distanceBetween(userLat, userLng, venueLat, venueLng, results)
        val distanceInMeters = results[0]
        return if (distanceInMeters > 1000) {
            String.format(Locale.US, "%.1f km", distanceInMeters / 1000)
        } else {
            "${distanceInMeters.toInt()} m"
        }
    }

    /**
     * Setup real-time listener for Firestore changes
     */
    fun startRealtimeSync() {
        firestore.collection(SPOTS_COLLECTION)
            .whereEqualTo("approvalStatus", "APPROVED")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Realtime sync error: ${error.message}")
                    return@addSnapshotListener
                }

                snapshot?.let { snap ->
                    CoroutineScope(Dispatchers.IO).launch {
                        val spots = snap.documents.mapNotNull { doc ->
                            doc.toSpot()?.let { SpotEntity.fromSpot(it) }
                        }
                        // Replace all spots to ensure deleted spots are removed
                        spotDao.replaceAllSpots(spots)
                        Log.d(TAG, "Realtime sync: replaced with ${spots.size} spots")
                    }
                }
            }
    }
}


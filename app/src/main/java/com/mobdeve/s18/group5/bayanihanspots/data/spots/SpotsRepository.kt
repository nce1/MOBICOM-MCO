package com.mobdeve.s18.group5.bayanihanspots.data.spots

import android.location.Location
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.mobdeve.s18.group5.bayanihanspots.utils.LocationUtil
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class SpotsRepository(private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()){
    fun observeSpots(): Flow<Result<List<Spot>>> = callbackFlow{
        val registration = firestore.collection(SPOTS_COLLECTION)
            .addSnapshotListener { snapshot, error ->
                if (error != null){
                    trySend(Result.failure(error)).isSuccess
                    return@addSnapshotListener
                }
                val spots = snapshot?.documents.orEmpty().mapNotNull { doc ->
                    doc.toSpot()
                }
                trySend(Result.success(spots)).isSuccess
            }
        awaitClose {registration.remove()}
    }

    suspend fun refreshSpots(): Result<Unit> = try{
        firestore.collection(SPOTS_COLLECTION).get().await()
        Result.success(Unit)
    } catch (e: Exception){
        Result.failure(e)
    }

    private fun DocumentSnapshot.toSpot(): Spot?{
        val name = getString("name") ?: return null
        val type = getString("type") ?: return null
        val status = getString("status") ?: return null
        val crowdLevel = getString("crowdLevel") ?: return null
        val description = getString("description") ?: return null
        val coordinates = getGeoPoint("coordinates") ?: return null

        return Spot(
            id =id,
            name = name,
            type = type,
            status = status,
            crowdLevel = crowdLevel,
            description = description,
            coordinates = coordinates
        )
    }

    fun updateDistances(spots: List<Spot>, userLocation: Location?): List<Spot> {
        if (userLocation == null) return spots

        return spots.map { spot ->
            val spotLat = spot.coordinates?.latitude ?: 0.0
            val spotLng = spot.coordinates?.longitude ?: 0.0

            val distString = LocationUtil.calculateDistance(
                userLat = userLocation.latitude,
                userLng = userLocation.longitude,
                venueLat = spotLat,
                venueLng = spotLng
            )

            spot.copy(distanceString = " • $distString")
        }
    }
    companion object{
        private const val SPOTS_COLLECTION = "spots"
    }
}
package com.mobdeve.s18.group5.bayanihanspots.data.events

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class EventsRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    fun observeEvents(): Flow<Result<List<Event>>> = callbackFlow {
        val registration = firestore.collection(EVENTS_COLLECTION)
            .orderBy("schedule")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Result.failure(error)).isSuccess
                    return@addSnapshotListener
                }
                val events = snapshot?.documents.orEmpty().mapNotNull { doc ->
                    doc.toEvent()
                }
                trySend(Result.success(events)).isSuccess
            }
        awaitClose { registration.remove() }
    }

    suspend fun refreshEvents(): Result<Unit> = try {
        firestore.collection(EVENTS_COLLECTION).get().await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    private fun DocumentSnapshot.toEvent(): Event? {
        val title = getString("title") ?: return null
        val schedule = getString("schedule") ?: getString("date") ?: return null
        val description = getString("description") ?: ""

        val locationValue = get("location")
        val locationLabel = when (locationValue) {
            is String -> locationValue
            is GeoPoint -> formatGeoPoint(locationValue)
            null -> getString("locationLabel") ?: "Unknown location"
            else -> locationValue.toString()
        }

        val coordinates = when (locationValue) {
            is GeoPoint -> locationValue
            else -> getGeoPoint("coordinates")
        }

        val creatorId = getString("creatorId") ?: return null
        val approvalStatus = getString("approvalStatus") ?: return null
        val modificationType = getString("modificationType") ?: return null
        val currentVolunteers = getString("currentVolunteers") ?: return null

        return Event(
            id = id,
            title = title,
            schedule = schedule,
            locationLabel = locationLabel,
            description = description,
            coordinates = coordinates,
            creatorId = creatorId,
            approvalStatus = approvalStatus,
            modificationType = modificationType
        )
    }

    private fun formatGeoPoint(point: GeoPoint): String =
        "Lat %.4f, Lng %.4f".format(point.latitude, point.longitude)

    companion object {
        private const val EVENTS_COLLECTION = "events"
    }
}

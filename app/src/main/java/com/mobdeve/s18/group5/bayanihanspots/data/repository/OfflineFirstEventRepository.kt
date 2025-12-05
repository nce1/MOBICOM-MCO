package com.mobdeve.s18.group5.bayanihanspots.data.repository

import android.content.Context
import android.util.Log
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.mobdeve.s18.group5.bayanihanspots.data.events.Event
import com.mobdeve.s18.group5.bayanihanspots.data.local.BayanihanDatabase
import com.mobdeve.s18.group5.bayanihanspots.data.local.entity.EventEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlin.collections.map

/**
 * Repository that implements offline-first strategy for Events:
 * 1. UI always observes Room database (single source of truth)
 * 2. Firestore syncs to Room in the background
 */
class OfflineFirstEventRepository(
    private val context: Context,
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val database = BayanihanDatabase.getInstance(context)
    private val eventDao = database.eventDao()

    companion object {
        private const val TAG = "OfflineFirstEventRepo"
        private const val EVENTS_COLLECTION = "events"

        @Volatile
        private var INSTANCE: OfflineFirstEventRepository? = null

        fun getInstance(context: Context): OfflineFirstEventRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: OfflineFirstEventRepository(context.applicationContext).also {
                    INSTANCE = it
                }
            }
        }
    }

    /**
     * Observe approved events from Room (offline-first)
     */
    fun observeApprovedEvents(): Flow<List<Event>> {
        // Start background sync when observing
        syncFromFirestore()
        return eventDao.observeApprovedEvents().map { entities ->
            entities.map { it.toEvent() }
        }
    }
    /**
     * Observe all events from Room (offline-first)
     */
    fun observeEvents(): Flow<List<Event>> {
        // Start background sync when observing
        syncFromFirestore()
        return eventDao.observeAllEvents().map { entities ->
            entities.map { it.toEvent() }
        }
    }

    /**
     * Get event by ID (from Room)
     */
    suspend fun getEventById(eventId: String): Event? {
        return eventDao.getEventById(eventId)?.toEvent()
    }

    /**
     * Observe a specific event
     */
    fun observeEventById(eventId: String): Flow<Event?> {
        return eventDao.observeEventById(eventId).map { it?.toEvent() }
    }

    /**
     * Sync events from Firestore to Room (background)
     */
    fun syncFromFirestore() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val snapshot = firestore.collection(EVENTS_COLLECTION)
                    .orderBy("schedule")
                    .get()
                    .await()

                val events = snapshot.documents.mapNotNull { doc ->
                    val event = doc.toEvent()
                    if (event != null && event.approvalStatus == "APPROVED"){
                        EventEntity.fromEvent(event)
                    } else {
                        null
                    }
                }

                if (events.isNotEmpty()) {
                    eventDao.insertEvents(events)
                    Log.d(TAG, "Synced ${events.size} events from Firestore")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to sync from Firestore: ${e.message}")
                // App continues to work with cached data
            }
        }
    }

    /**
     * Force refresh from Firestore
     */
    suspend fun forceRefresh(): Result<Unit> {
        return try {
            val snapshot = firestore.collection(EVENTS_COLLECTION)
                .orderBy("schedule")
                .get()
                .await()

            val events = snapshot.documents.mapNotNull { doc ->
                val event = doc.toEvent()
                if (event != null && event.approvalStatus == "APPROVED"){
                    EventEntity.fromEvent(event)
                } else {
                    null
                }
            }

            eventDao.replaceAllEvents(events)
            Log.d(TAG, "Force refreshed ${events.size} events")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Force refresh failed: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Setup real-time listener for Firestore changes
     */
    fun startRealtimeSync() {
        firestore.collection(EVENTS_COLLECTION)
            .orderBy("schedule")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Realtime sync error: ${error.message}")
                    return@addSnapshotListener
                }

                snapshot?.let { snap ->
                    CoroutineScope(Dispatchers.IO).launch {
                        val events = snap.documents.mapNotNull { doc ->
                            val event = doc.toEvent()
                            if (event != null && event.approvalStatus == "APPROVED"){
                                EventEntity.fromEvent(event)
                            } else {
                                null
                            }
                        }
                        if (events.isNotEmpty()) {
                            eventDao.insertEvents(events)
                            Log.d(TAG, "Realtime sync: updated ${events.size} events")
                        }
                    }
                }
            }
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

        // Safely parse maxVolunteers - could be Long, Int, String, or null
        val maxVolunteers = try {
            when (val value = get("maxVolunteers")) {
                is Number -> value.toInt()
                is String -> value.toIntOrNull()
                else -> null
            }
        } catch (e: Exception) {
            null
        }

        // Safely parse scheduleUtcMillis
        val scheduleUtcMillis = try {
            when (val value = get("scheduleUtcMillis")) {
                is Number -> value.toLong()
                is String -> value.toLongOrNull()
                else -> null
            }
        } catch (e: Exception) {
            null
        }
        val creatorId = getString("creatorId") ?: return null
        val approvalStatus = getString("approvalStatus") ?: return null
        val modificationType = getString("modificationType") ?: return null
        return Event(
            id = id,
            title = title,
            schedule = schedule,
            locationLabel = locationLabel,
            description = description,
            coordinates = coordinates,
            host = getString("host"),
            maxVolunteers = maxVolunteers,
            scheduleUtcMillis = scheduleUtcMillis,
            creatorId = creatorId,
            approvalStatus = approvalStatus,
            modificationType = modificationType
        )
    }

    private fun formatGeoPoint(point: GeoPoint): String =
        "Lat %.4f, Lng %.4f".format(point.latitude, point.longitude)


    // SIGN UP PORTION

    // Join an Event
    suspend fun joinEvent(event: Event, userId: String, userEmail: String): Result<String> {
        return try {
            val eventRef = firestore.collection("events").document(event.id)

            val signupId = "${event.id}_${userId}"
            val signupRef = firestore.collection("event_signups").document(signupId)

            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(eventRef)
                val currentCount = snapshot.getLong("currentVolunteers")?.toInt() ?: 0

                val newSignup = hashMapOf(
                    "signupId" to signupId,
                    "eventId" to event.id,
                    "eventTitle" to event.title,
                    "userId" to userId,
                    "userEmail" to userEmail,
                    "status" to "CONFIRMED",
                    "timestamp" to com.google.firebase.Timestamp.now()
                )

                transaction.set(signupRef, newSignup)
                transaction.update(eventRef, "currentVolunteers", currentCount + 1)
            }.await()

            Result.success("Successfully joined!")
        } catch (e: Exception) {
            val msg = if (e.message?.contains("Event is full") == true) "Event is full"
            else if (e.message?.contains("already joined") == true) "You already joined"
            else "Failed to join: ${e.message}"
            Result.failure(Exception(msg))
        }
    }

    // Leave an Event
    suspend fun leaveEvent(signupId: String, eventId: String): Result<String> {
        return try {
            val eventRef = firestore.collection("events").document(eventId)
            val signupRef = firestore.collection("event_signups").document(signupId)

            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(eventRef)
                val currentCount = snapshot.getLong("currentVolunteers")?.toInt() ?: 0

                transaction.update(signupRef, "status", "CANCELLED")

                if (currentCount > 0) {
                    transaction.update(eventRef, "currentVolunteers", currentCount - 1)
                }
            }.await()

            Result.success("Signup cancelled")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Find active events joined by a user
    fun observeUserJoinedEventIds(userId: String): Flow<Set<String>> = callbackFlow{
        val query = firestore.collection("signups")
            .whereEqualTo("userId", userId)
            .whereEqualTo("status", "CONFIRMED")

        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null){
                trySend(emptySet())
                return@addSnapshotListener
            }
            if (snapshot != null){
                val ids = snapshot.documents.mapNotNull { it.getString("eventId") }.toSet()
                trySend(ids)
            }
        }
        awaitClose { listener.remove() }
    }
}


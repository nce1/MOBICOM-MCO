package com.mobdeve.s18.group5.bayanihanspots.data.repository

import android.content.Context
import android.util.Log
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.mobdeve.s18.group5.bayanihanspots.data.events.Event
import com.mobdeve.s18.group5.bayanihanspots.data.local.BayanihanDatabase
import com.mobdeve.s18.group5.bayanihanspots.data.local.entity.EventEntity
<<<<<<< Updated upstream
<<<<<<< Updated upstream
import com.mobdeve.s18.group5.bayanihanspots.data.signups.Signups
=======
import com.mobdeve.s18.group5.bayanihanspots.data.local.entity.EventSignupEntity
import com.mobdeve.s18.group5.bayanihanspots.data.local.entity.FavoriteEventEntity
import com.mobdeve.s18.group5.bayanihanspots.workers.EventReminderScheduler
>>>>>>> Stashed changes
=======
import com.mobdeve.s18.group5.bayanihanspots.data.local.entity.EventSignupEntity
import com.mobdeve.s18.group5.bayanihanspots.data.local.entity.FavoriteEventEntity
import com.mobdeve.s18.group5.bayanihanspots.workers.EventReminderScheduler
>>>>>>> Stashed changes
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
    private val favoriteEventDao = database.favoriteEventDao()
    private val eventSignupDao = database.eventSignupDao()

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
        val locationLabel = getString("locationLabel") ?: "Unknown location"

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
        val currentVolunteers = try{
            when (val value = get("currentVolunteers")){
                is Number -> value.toInt()
                is String -> value.toIntOrNull()
                else -> null
            }
        } catch(e: Exception){
            null
        }
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
            modificationType = modificationType,
            currentVolunteers = currentVolunteers
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
            val signupRef = firestore.collection("signups").document(signupId)
<<<<<<< Updated upstream
<<<<<<< Updated upstream

            val notificationRef = firestore.collection("users")
                .document(event.creatorId)
                .collection("notifications")
                .document()
=======
>>>>>>> Stashed changes
=======
>>>>>>> Stashed changes

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
                val notifData = hashMapOf(
                    "title" to "New Volunteer!",
                    "message" to "$userEmail has joined '${event.title}'",
                    "timestamp" to com.google.firebase.Timestamp.now(),
                    "isRead" to false
                )

                transaction.set(signupRef, newSignup)
                transaction.update(eventRef, "currentVolunteers", currentCount + 1)
                transaction.set(notificationRef, notifData)
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
            val signupRef = firestore.collection("signups").document(signupId)

            firestore.runTransaction { transaction ->
                val eventSnapshot = transaction.get(eventRef)
                val currentCount = eventSnapshot.getLong("currentVolunteers")?.toInt() ?: 0
                val creatorId = eventSnapshot.getString("creatorId") ?: ""
                val eventTitle = eventSnapshot.getString("title") ?: "Event"

                val signupSnapshot = transaction.get(signupRef)
                val userEmail = signupSnapshot.getString("userEmail") ?: "A volunteer"

                if (creatorId.isNotEmpty()) {
                    val notificationRef = firestore.collection("users")
                        .document(creatorId)
                        .collection("notifications")
                        .document() // Generate random ID

                    val notifData = hashMapOf(
                        "title" to "Volunteer Left",
                        "message" to "$userEmail has cancelled their signup for '$eventTitle'.",
                        "timestamp" to com.google.firebase.Timestamp.now(),
                        "isRead" to false
                    )
                    transaction.set(notificationRef, notifData)
                }

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

    // Find active events joined by a user - combines local DB and Firestore
    fun observeUserJoinedEventIds(userId: String): Flow<Set<String>> = callbackFlow {
        // First, emit from local database immediately
        val localSignups = eventSignupDao.getSignupsByUser(userId)
        val localIds = localSignups.map { it.eventId }.toSet()
        trySend(localIds)

        // Then listen to Firestore for real-time updates
        val query = firestore.collection("signups")
            .whereEqualTo("userId", userId)
            .whereEqualTo("status", "CONFIRMED")

        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e(TAG, "Error observing joined events: ${error.message}")
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val ids = snapshot.documents.mapNotNull { it.getString("eventId") }.toSet()
                trySend(ids)

                // Also sync to local database
                CoroutineScope(Dispatchers.IO).launch {
                    for (doc in snapshot.documents) {
                        val eventId = doc.getString("eventId") ?: continue
                        val eventTitle = doc.getString("eventTitle") ?: ""
                        val status = doc.getString("status") ?: "CONFIRMED"

                        if (status == "CONFIRMED") {
                            val entity = EventSignupEntity(
                                id = doc.id,
                                eventId = eventId,
                                userId = userId,
                                userEmail = doc.getString("userEmail") ?: "",
                                eventTitle = eventTitle,
                                eventSchedule = "",
                                eventScheduleMillis = null,
                                status = status,
                                joinedAt = System.currentTimeMillis(),
                                notificationScheduled = false
                            )
                            eventSignupDao.insert(entity)
                        }
                    }
                }
            }
        }
        awaitClose { listener.remove() }
    }

<<<<<<< Updated upstream
    // Find all events joined by the user
    fun observeUserSignups(userId: String): Flow<List<Signups>> = callbackFlow {
        val listener = firestore.collection("signups")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val signups = snapshot?.toObjects(Signups::class.java) ?: emptyList()
                trySend(signups)
            }
        awaitClose { listener.remove() }
=======
    // ==================== RSVP WITH NOTIFICATIONS ====================

    /**
     * Join an event and schedule reminder notifications
     */
    suspend fun joinEventWithNotifications(
        event: Event,
        userId: String,
        userEmail: String
    ): Result<String> {
        return try {
            val eventRef = firestore.collection("events").document(event.id)
            val signupId = "${event.id}_${userId}"
            val signupRef = firestore.collection("signups").document(signupId)

            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(eventRef)
                val currentCount = snapshot.getLong("currentVolunteers")?.toInt() ?: 0
                val maxVolunteers = snapshot.getLong("maxVolunteers")?.toInt()

                // Check if event is full
                if (maxVolunteers != null && currentCount >= maxVolunteers) {
                    throw Exception("Event is full")
                }

                // Check if already joined
                val existingSignup = transaction.get(signupRef)
                if (existingSignup.exists() && existingSignup.getString("status") == "CONFIRMED") {
                    throw Exception("You have already joined this event")
                }

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

            // Save to local database
            val signupEntity = EventSignupEntity(
                id = signupId,
                eventId = event.id,
                userId = userId,
                userEmail = userEmail,
                eventTitle = event.title,
                eventSchedule = event.schedule,
                eventScheduleMillis = event.scheduleUtcMillis,
                status = "CONFIRMED",
                joinedAt = System.currentTimeMillis(),
                notificationScheduled = false
            )
            eventSignupDao.insert(signupEntity)

            // Schedule notifications if event has a valid schedule time
            if (event.scheduleUtcMillis != null && event.scheduleUtcMillis > System.currentTimeMillis()) {
                EventReminderScheduler.scheduleReminders(
                    context = context,
                    eventId = event.id,
                    eventTitle = event.title,
                    eventScheduleMillis = event.scheduleUtcMillis
                )
                eventSignupDao.markNotificationScheduled(signupId)
                Log.d(TAG, "Scheduled notifications for event: ${event.title}")
            }

            Result.success("Successfully joined! You'll be reminded before the event.")
        } catch (e: Exception) {
            val msg = when {
                e.message?.contains("Event is full") == true -> "Event is full"
                e.message?.contains("already joined") == true -> "You have already joined this event"
                else -> "Failed to join: ${e.message}"
            }
            Result.failure(Exception(msg))
        }
    }

    /**
     * Leave an event and cancel scheduled notifications
     */
    suspend fun leaveEventWithNotifications(eventId: String, userId: String): Result<String> {
        val signupId = "${eventId}_${userId}"

        return try {
            val eventRef = firestore.collection("events").document(eventId)
            val signupRef = firestore.collection("signups").document(signupId)

            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(eventRef)
                val currentCount = snapshot.getLong("currentVolunteers")?.toInt() ?: 0

                transaction.update(signupRef, "status", "CANCELLED")

                if (currentCount > 0) {
                    transaction.update(eventRef, "currentVolunteers", currentCount - 1)
                }
            }.await()

            // Update local database
            eventSignupDao.updateStatus(signupId, "CANCELLED")

            // Cancel scheduled notifications
            EventReminderScheduler.cancelReminders(context, eventId)

            Result.success("You have left the event")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

<<<<<<< Updated upstream
    /**
     * Observe if user has joined a specific event (from local database)
     */
    fun observeHasJoined(eventId: String, userId: String): Flow<Boolean> {
        return eventSignupDao.observeHasJoined(eventId, userId)
    }

    /**
     * Observe joined event IDs from local database (for immediate UI updates)
     */
    fun observeLocalJoinedEventIds(userId: String): Flow<Set<String>> {
        return eventSignupDao.observeJoinedEventIds(userId).map { it.toSet() }
    }

    /**
     * Sync joined events from Firestore to local database
     */
    suspend fun syncJoinedEventsFromFirestore(userId: String) {
        try {
            val snapshot = firestore.collection("signups")
                .whereEqualTo("userId", userId)
                .whereEqualTo("status", "CONFIRMED")
                .get()
                .await()

            for (doc in snapshot.documents) {
                val eventId = doc.getString("eventId") ?: continue
                val eventTitle = doc.getString("eventTitle") ?: ""

                val entity = EventSignupEntity(
                    id = doc.id,
                    eventId = eventId,
                    userId = userId,
                    userEmail = doc.getString("userEmail") ?: "",
                    eventTitle = eventTitle,
                    eventSchedule = "",
                    eventScheduleMillis = null,
                    status = "CONFIRMED",
                    joinedAt = System.currentTimeMillis(),
                    notificationScheduled = false
                )
                eventSignupDao.insert(entity)
            }
            Log.d(TAG, "Synced ${snapshot.size()} joined events from Firestore")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to sync joined events: ${e.message}")
        }
    }

    /**
     * Get user's joined events from local database
     */
    fun observeUserSignups(userId: String): Flow<List<EventSignupEntity>> {
        return eventSignupDao.observeSignupsByUser(userId)
    }

    // ==================== FAVORITES ====================

    /**
     * Add event to favorites
     */
    suspend fun addToFavorites(event: Event, userId: String): Result<Unit> {
        return try {
            val favoriteId = "${event.id}_${userId}"
            val favoriteEntity = FavoriteEventEntity(
                id = favoriteId,
                eventId = event.id,
                userId = userId,
                eventTitle = event.title,
                eventSchedule = event.schedule
            )
            favoriteEventDao.insert(favoriteEntity)

            // Also save to Firestore for cross-device sync
            val favoriteRef = firestore.collection("user_favorites").document(favoriteId)
            val favoriteData = hashMapOf(
                "eventId" to event.id,
                "userId" to userId,
                "eventTitle" to event.title,
                "addedAt" to com.google.firebase.Timestamp.now()
            )
            favoriteRef.set(favoriteData).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to add favorite: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Remove event from favorites
     */
    suspend fun removeFromFavorites(eventId: String, userId: String): Result<Unit> {
        return try {
            favoriteEventDao.deleteByEventAndUser(eventId, userId)

            // Also remove from Firestore
            val favoriteId = "${eventId}_${userId}"
            firestore.collection("user_favorites").document(favoriteId).delete().await()

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to remove favorite: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Toggle favorite status
     */
    suspend fun toggleFavorite(event: Event, userId: String): Result<Boolean> {
        val isFavorite = favoriteEventDao.isFavorite(event.id, userId)
        return if (isFavorite) {
            removeFromFavorites(event.id, userId)
            Result.success(false)
        } else {
            addToFavorites(event, userId)
            Result.success(true)
        }
    }

    /**
     * Observe if event is favorited (from local database)
     */
    fun observeIsFavorite(eventId: String, userId: String): Flow<Boolean> {
        return favoriteEventDao.observeIsFavorite(eventId, userId)
    }

    /**
     * Observe user's favorite event IDs
     */
    fun observeFavoriteEventIds(userId: String): Flow<List<String>> {
        return favoriteEventDao.observeFavoriteEventIds(userId)
    }

    /**
     * Observe user's favorites
     */
    fun observeFavorites(userId: String): Flow<List<FavoriteEventEntity>> {
        return favoriteEventDao.observeFavoritesByUser(userId)
    }

    /**
     * Sync favorites from Firestore to local database
     */
    fun syncFavoritesFromFirestore(userId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val snapshot = firestore.collection("user_favorites")
                    .whereEqualTo("userId", userId)
                    .get()
                    .await()

                for (doc in snapshot.documents) {
                    val eventId = doc.getString("eventId") ?: continue
                    val eventTitle = doc.getString("eventTitle") ?: ""

                    val favoriteEntity = FavoriteEventEntity(
                        id = doc.id,
                        eventId = eventId,
                        userId = userId,
                        eventTitle = eventTitle,
                        eventSchedule = ""
                    )
                    favoriteEventDao.insert(favoriteEntity)
                }
                Log.d(TAG, "Synced ${snapshot.size()} favorites from Firestore")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to sync favorites: ${e.message}")
=======
    // Find active events joined by a user - combines local DB and Firestore
    fun observeUserJoinedEventIds(userId: String): Flow<Set<String>> = callbackFlow {
        // First, emit from local database immediately
        val localSignups = eventSignupDao.getSignupsByUser(userId)
        val localIds = localSignups.map { it.eventId }.toSet()
        trySend(localIds)

        // Then listen to Firestore for real-time updates
        val query = firestore.collection("signups")
            .whereEqualTo("userId", userId)
            .whereEqualTo("status", "CONFIRMED")

        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e(TAG, "Error observing joined events: ${error.message}")
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val ids = snapshot.documents.mapNotNull { it.getString("eventId") }.toSet()
                trySend(ids)

                // Also sync to local database
                CoroutineScope(Dispatchers.IO).launch {
                    for (doc in snapshot.documents) {
                        val eventId = doc.getString("eventId") ?: continue
                        val eventTitle = doc.getString("eventTitle") ?: ""
                        val status = doc.getString("status") ?: "CONFIRMED"

                        if (status == "CONFIRMED") {
                            val entity = EventSignupEntity(
                                id = doc.id,
                                eventId = eventId,
                                userId = userId,
                                userEmail = doc.getString("userEmail") ?: "",
                                eventTitle = eventTitle,
                                eventSchedule = "",
                                eventScheduleMillis = null,
                                status = status,
                                joinedAt = System.currentTimeMillis(),
                                notificationScheduled = false
                            )
                            eventSignupDao.insert(entity)
                        }
                    }
                }
>>>>>>> Stashed changes
            }
        }
>>>>>>> Stashed changes
    }

    // ==================== RSVP WITH NOTIFICATIONS ====================

    /**
     * Join an event and schedule reminder notifications
     */
    suspend fun joinEventWithNotifications(
        event: Event,
        userId: String,
        userEmail: String
    ): Result<String> {
        return try {
            val eventRef = firestore.collection("events").document(event.id)
            val signupId = "${event.id}_${userId}"
            val signupRef = firestore.collection("signups").document(signupId)

            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(eventRef)
                val currentCount = snapshot.getLong("currentVolunteers")?.toInt() ?: 0
                val maxVolunteers = snapshot.getLong("maxVolunteers")?.toInt()

                // Check if event is full
                if (maxVolunteers != null && currentCount >= maxVolunteers) {
                    throw Exception("Event is full")
                }

                // Check if already joined
                val existingSignup = transaction.get(signupRef)
                if (existingSignup.exists() && existingSignup.getString("status") == "CONFIRMED") {
                    throw Exception("You have already joined this event")
                }

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

            // Save to local database
            val signupEntity = EventSignupEntity(
                id = signupId,
                eventId = event.id,
                userId = userId,
                userEmail = userEmail,
                eventTitle = event.title,
                eventSchedule = event.schedule,
                eventScheduleMillis = event.scheduleUtcMillis,
                status = "CONFIRMED",
                joinedAt = System.currentTimeMillis(),
                notificationScheduled = false
            )
            eventSignupDao.insert(signupEntity)

            // Schedule notifications if event has a valid schedule time
            if (event.scheduleUtcMillis != null && event.scheduleUtcMillis > System.currentTimeMillis()) {
                EventReminderScheduler.scheduleReminders(
                    context = context,
                    eventId = event.id,
                    eventTitle = event.title,
                    eventScheduleMillis = event.scheduleUtcMillis
                )
                eventSignupDao.markNotificationScheduled(signupId)
                Log.d(TAG, "Scheduled notifications for event: ${event.title}")
            }

            Result.success("Successfully joined! You'll be reminded before the event.")
        } catch (e: Exception) {
            val msg = when {
                e.message?.contains("Event is full") == true -> "Event is full"
                e.message?.contains("already joined") == true -> "You have already joined this event"
                else -> "Failed to join: ${e.message}"
            }
            Result.failure(Exception(msg))
        }
    }

    /**
     * Leave an event and cancel scheduled notifications
     */
    suspend fun leaveEventWithNotifications(eventId: String, userId: String): Result<String> {
        val signupId = "${eventId}_${userId}"

        return try {
            val eventRef = firestore.collection("events").document(eventId)
            val signupRef = firestore.collection("signups").document(signupId)

            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(eventRef)
                val currentCount = snapshot.getLong("currentVolunteers")?.toInt() ?: 0

                transaction.update(signupRef, "status", "CANCELLED")

                if (currentCount > 0) {
                    transaction.update(eventRef, "currentVolunteers", currentCount - 1)
                }
            }.await()

            // Update local database
            eventSignupDao.updateStatus(signupId, "CANCELLED")

            // Cancel scheduled notifications
            EventReminderScheduler.cancelReminders(context, eventId)

            Result.success("You have left the event")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Observe if user has joined a specific event (from local database)
     */
    fun observeHasJoined(eventId: String, userId: String): Flow<Boolean> {
        return eventSignupDao.observeHasJoined(eventId, userId)
    }

    /**
     * Observe joined event IDs from local database (for immediate UI updates)
     */
    fun observeLocalJoinedEventIds(userId: String): Flow<Set<String>> {
        return eventSignupDao.observeJoinedEventIds(userId).map { it.toSet() }
    }

    /**
     * Sync joined events from Firestore to local database
     */
    suspend fun syncJoinedEventsFromFirestore(userId: String) {
        try {
            val snapshot = firestore.collection("signups")
                .whereEqualTo("userId", userId)
                .whereEqualTo("status", "CONFIRMED")
                .get()
                .await()

            for (doc in snapshot.documents) {
                val eventId = doc.getString("eventId") ?: continue
                val eventTitle = doc.getString("eventTitle") ?: ""

                val entity = EventSignupEntity(
                    id = doc.id,
                    eventId = eventId,
                    userId = userId,
                    userEmail = doc.getString("userEmail") ?: "",
                    eventTitle = eventTitle,
                    eventSchedule = "",
                    eventScheduleMillis = null,
                    status = "CONFIRMED",
                    joinedAt = System.currentTimeMillis(),
                    notificationScheduled = false
                )
                eventSignupDao.insert(entity)
            }
            Log.d(TAG, "Synced ${snapshot.size()} joined events from Firestore")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to sync joined events: ${e.message}")
        }
    }

    /**
     * Get user's joined events from local database
     */
    fun observeUserSignups(userId: String): Flow<List<EventSignupEntity>> {
        return eventSignupDao.observeSignupsByUser(userId)
    }

    // ==================== FAVORITES ====================

    /**
     * Add event to favorites
     */
    suspend fun addToFavorites(event: Event, userId: String): Result<Unit> {
        return try {
            val favoriteId = "${event.id}_${userId}"
            val favoriteEntity = FavoriteEventEntity(
                id = favoriteId,
                eventId = event.id,
                userId = userId,
                eventTitle = event.title,
                eventSchedule = event.schedule
            )
            favoriteEventDao.insert(favoriteEntity)

            // Also save to Firestore for cross-device sync
            val favoriteRef = firestore.collection("user_favorites").document(favoriteId)
            val favoriteData = hashMapOf(
                "eventId" to event.id,
                "userId" to userId,
                "eventTitle" to event.title,
                "addedAt" to com.google.firebase.Timestamp.now()
            )
            favoriteRef.set(favoriteData).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to add favorite: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Remove event from favorites
     */
    suspend fun removeFromFavorites(eventId: String, userId: String): Result<Unit> {
        return try {
            favoriteEventDao.deleteByEventAndUser(eventId, userId)

            // Also remove from Firestore
            val favoriteId = "${eventId}_${userId}"
            firestore.collection("user_favorites").document(favoriteId).delete().await()

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to remove favorite: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Toggle favorite status
     */
    suspend fun toggleFavorite(event: Event, userId: String): Result<Boolean> {
        val isFavorite = favoriteEventDao.isFavorite(event.id, userId)
        return if (isFavorite) {
            removeFromFavorites(event.id, userId)
            Result.success(false)
        } else {
            addToFavorites(event, userId)
            Result.success(true)
        }
    }

    /**
     * Observe if event is favorited (from local database)
     */
    fun observeIsFavorite(eventId: String, userId: String): Flow<Boolean> {
        return favoriteEventDao.observeIsFavorite(eventId, userId)
    }

    /**
     * Observe user's favorite event IDs
     */
    fun observeFavoriteEventIds(userId: String): Flow<List<String>> {
        return favoriteEventDao.observeFavoriteEventIds(userId)
    }

    /**
     * Observe user's favorites
     */
    fun observeFavorites(userId: String): Flow<List<FavoriteEventEntity>> {
        return favoriteEventDao.observeFavoritesByUser(userId)
    }

    /**
     * Sync favorites from Firestore to local database
     */
    fun syncFavoritesFromFirestore(userId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val snapshot = firestore.collection("user_favorites")
                    .whereEqualTo("userId", userId)
                    .get()
                    .await()

                for (doc in snapshot.documents) {
                    val eventId = doc.getString("eventId") ?: continue
                    val eventTitle = doc.getString("eventTitle") ?: ""

                    val favoriteEntity = FavoriteEventEntity(
                        id = doc.id,
                        eventId = eventId,
                        userId = userId,
                        eventTitle = eventTitle,
                        eventSchedule = ""
                    )
                    favoriteEventDao.insert(favoriteEntity)
                }
                Log.d(TAG, "Synced ${snapshot.size()} favorites from Firestore")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to sync favorites: ${e.message}")
            }
        }
    }
}


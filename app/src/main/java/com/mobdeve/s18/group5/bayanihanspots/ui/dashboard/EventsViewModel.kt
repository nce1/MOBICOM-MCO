package com.mobdeve.s18.group5.bayanihanspots.ui.dashboard

import android.app.Application
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.mobdeve.s18.group5.bayanihanspots.data.events.Event
import com.mobdeve.s18.group5.bayanihanspots.data.repository.OfflineFirstEventRepository
import com.mobdeve.s18.group5.bayanihanspots.notifications.EventReminderManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

sealed interface EventsUiState {
    object Loading : EventsUiState
    data class Success(val events: List<Event>, val joinedEventIds: Set<String> = emptySet()) : EventsUiState
    data class Error(val message: String) : EventsUiState
}

/**
 * ViewModel that uses offline-first repository for events.
 * UI always reads from Room (single source of truth).
 * Firestore syncs in the background.
 */
class EventsViewModel(
    private val repository: OfflineFirstEventRepository,
    private val application: Application
) : ViewModel() {
    private val _uiState = MutableStateFlow<EventsUiState>(EventsUiState.Loading)
    private var joinedIds = emptySet<String>()
    val uiState: StateFlow<EventsUiState> = _uiState.asStateFlow()

    init {
        // Create notification channel on init
        EventReminderManager.createNotificationChannel(application)
        observeEvents()
        // Start real-time sync from Firestore to Room
        repository.startRealtimeSync()
    }

    /**
     * Observe events from Room database (offline-first)
     */
    private fun observeEvents() {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        val eventsFlow = repository.observeApprovedEvents()
        val joinedFlow = if (currentUserId != null) {
            repository.observeUserJoinedEventIds(currentUserId)
        } else {
            flowOf(emptySet())
        }

        viewModelScope.launch {
            combine(eventsFlow, joinedFlow) { events, joinedIds ->
                EventsUiState.Success(events, joinedIds)
            }.collect { successState ->
                _uiState.value = successState
            }
        }
    }

    /**
     * Force refresh from Firestore
     */
    fun refresh() {
        viewModelScope.launch {
            _uiState.value = EventsUiState.Loading
            val result = repository.forceRefresh()
            if (result.isFailure) {
                // Events will still be observed from Room cache
                _uiState.value = EventsUiState.Error("Unable to refresh. Showing cached data.")
            }
            // Success case is handled by observeEvents() flow
        }
    }


    fun joinEvent(event: Event){
        val user = FirebaseAuth.getInstance().currentUser

        if (user != null) {
            viewModelScope.launch {
                val result = repository.joinEvent(
                    event = event,
                    userId = user.uid,
                    userEmail = user.email ?: ""
                )
                if (result.isSuccess) {
                    // Schedule reminders for this event
                    scheduleEventReminders(event)
                    Log.d("EventsViewModel", "Successfully joined event and scheduled reminders")
                } else {
                    Log.e("EventsViewModel", "Join failed: ${result.exceptionOrNull()?.message}")
                }
            }
        }
    }

    fun leaveEvent(event: Event) {
        val user = FirebaseAuth.getInstance().currentUser

        if (user != null) {
            viewModelScope.launch {
                val signupId = "${event.id}_${user.uid}"
                val result = repository.leaveEvent(signupId, event.id)
                if (result.isSuccess) {
                    // Cancel scheduled reminders for this event
                    EventReminderManager.cancelEventReminders(application, event.id)
                    Log.d("EventsViewModel", "Successfully left event and cancelled reminders")
                } else {
                    Log.e("EventsViewModel", "Leave failed: ${result.exceptionOrNull()?.message}")
                }
            }
        }
    }

    /**
     * Record a check-in for an event
     */
    fun checkInEvent(event: Event, distanceMeters: Float) {
        val user = FirebaseAuth.getInstance().currentUser

        if (user != null) {
            viewModelScope.launch {
                try {
                    val signupId = "${event.id}_${user.uid}"
                    val checkInData = hashMapOf(
                        "checkedInAt" to com.google.firebase.Timestamp.now(),
                        "distanceMeters" to distanceMeters.toDouble()
                    )

                    com.google.firebase.firestore.FirebaseFirestore.getInstance()
                        .collection("signups")
                        .document(signupId)
                        .update(checkInData as Map<String, Any>)
                        .addOnSuccessListener {
                            Log.d("EventsViewModel", "Check-in recorded for event: ${event.title}")
                        }
                        .addOnFailureListener { e ->
                            Log.e("EventsViewModel", "Failed to record check-in: ${e.message}")
                        }
                } catch (e: Exception) {
                    Log.e("EventsViewModel", "Check-in error: ${e.message}")
                }
            }
        }
    }

    /**
     * Schedule notification reminders for an event
     */
    private fun scheduleEventReminders(event: Event) {
        val eventTimeMillis = event.scheduleUtcMillis
        if (eventTimeMillis != null && eventTimeMillis > System.currentTimeMillis()) {
            EventReminderManager.scheduleEventReminders(
                context = application,
                eventId = event.id,
                eventTitle = event.title,
                eventTimeMillis = eventTimeMillis
            )
            Log.d("EventsViewModel", "Scheduled reminders for event: ${event.title} at $eventTimeMillis")
        } else {
            Log.w("EventsViewModel", "Cannot schedule reminders: event time is null or in the past")
        }
    }
}

/**
 * Factory that creates EventsViewModel with Application context for Room database access.
 */
class EventsViewModelFactory(private val application: Application? = null) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EventsViewModel::class.java)) {
            if (application == null) {
                throw IllegalStateException("Application context required for offline-first mode. Use EventsViewModelFactory(application).")
            }
            val repository = OfflineFirstEventRepository.getInstance(application)
            @Suppress("UNCHECKED_CAST")
            return EventsViewModel(repository, application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

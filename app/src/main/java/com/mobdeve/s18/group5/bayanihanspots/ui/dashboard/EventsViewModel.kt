package com.mobdeve.s18.group5.bayanihanspots.ui.dashboard

import android.app.Application
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.mobdeve.s18.group5.bayanihanspots.data.events.Event
import com.mobdeve.s18.group5.bayanihanspots.data.repository.OfflineFirstEventRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

sealed interface EventsUiState {
    object Loading : EventsUiState
    data class Success(
        val events: List<Event>,
        val joinedEventIds: Set<String> = emptySet(),
        val favoriteEventIds: Set<String> = emptySet()
    ) : EventsUiState
    data class Error(val message: String) : EventsUiState
}

/**
 * ViewModel that uses offline-first repository for events.
 * UI always reads from Room (single source of truth).
 * Firestore syncs in the background.
 */
class EventsViewModel(private val repository: OfflineFirstEventRepository) : ViewModel() {
    private val _uiState = MutableStateFlow<EventsUiState>(EventsUiState.Loading)
    val uiState: StateFlow<EventsUiState> = _uiState.asStateFlow()

    private val _actionResult = MutableStateFlow<ActionResult?>(null)
    val actionResult: StateFlow<ActionResult?> = _actionResult.asStateFlow()

    init {
        observeEvents()
        // Start real-time sync from Firestore to Room
        repository.startRealtimeSync()

        // Sync favorites if user is logged in
        FirebaseAuth.getInstance().currentUser?.uid?.let { userId ->
            repository.syncFavoritesFromFirestore(userId)
        }
    }

    /**
     * Observe events from Room database (offline-first)
     */
    private fun observeEvents() {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        val eventsFlow = repository.observeApprovedEvents()

        // Use local database for joined events (immediate updates)
        val joinedFlow = if (currentUserId != null) {
            repository.observeLocalJoinedEventIds(currentUserId)
        } else {
            flowOf(emptySet())
        }

        val favoritesFlow = if (currentUserId != null) {
            repository.observeFavoriteEventIds(currentUserId)
        } else {
            flowOf(emptyList())
        }

        viewModelScope.launch {
            combine(eventsFlow, joinedFlow, favoritesFlow) { events, joinedIds, favoriteIds ->
                EventsUiState.Success(
                    events = events,
                    joinedEventIds = joinedIds,
                    favoriteEventIds = favoriteIds.toSet()
                )
            }.collect { successState ->
                _uiState.value = successState
            }
        }

        // Also sync from Firestore in the background
        if (currentUserId != null) {
            viewModelScope.launch {
                repository.syncJoinedEventsFromFirestore(currentUserId)
            }
        }
    }

    /**
     * Join an event with notification scheduling
     */
    fun joinEvent(event: Event) {
        val currentUser = FirebaseAuth.getInstance().currentUser ?: return

        viewModelScope.launch {
            val result = repository.joinEventWithNotifications(
                event = event,
                userId = currentUser.uid,
                userEmail = currentUser.email ?: ""
            )

            _actionResult.value = if (result.isSuccess) {
                ActionResult.Success(result.getOrNull() ?: "Joined successfully!")
            } else {
                ActionResult.Error(result.exceptionOrNull()?.message ?: "Failed to join")
            }
        }
    }

    /**
     * Leave an event and cancel notifications
     */
    fun leaveEvent(eventId: String) {
        val currentUser = FirebaseAuth.getInstance().currentUser ?: return

        viewModelScope.launch {
            val result = repository.leaveEventWithNotifications(
                eventId = eventId,
                userId = currentUser.uid
            )

            _actionResult.value = if (result.isSuccess) {
                ActionResult.Success("You have left the event")
            } else {
                ActionResult.Error(result.exceptionOrNull()?.message ?: "Failed to leave event")
            }
        }
    }

    /**
     * Toggle favorite status for an event
     */
    fun toggleFavorite(event: Event) {
        val currentUser = FirebaseAuth.getInstance().currentUser ?: return

        viewModelScope.launch {
            val result = repository.toggleFavorite(event, currentUser.uid)

            _actionResult.value = if (result.isSuccess) {
                val isFavorite = result.getOrNull() ?: false
                ActionResult.Success(if (isFavorite) "Added to favorites" else "Removed from favorites")
            } else {
                ActionResult.Error("Failed to update favorites")
            }
        }
    }

    /**
     * Clear action result after showing to user
     */
    fun clearActionResult() {
        _actionResult.value = null
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
                if (result.isFailure) {
                    println("Join failed: ${result.exceptionOrNull()?.message}")
                }
            }
        }
    }
}

sealed interface ActionResult {
    data class Success(val message: String) : ActionResult
    data class Error(val message: String) : ActionResult
}

/**
 * Factory that creates EventsViewModel with Application context for Room database access.
 */
class EventsViewModelFactory(private val application: Application? = null) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EventsViewModel::class.java)) {
            val repository = if (application != null) {
                OfflineFirstEventRepository.getInstance(application)
            } else {
                throw IllegalStateException("Application context required for offline-first mode. Use EventsViewModelFactory(application).")
            }
            @Suppress("UNCHECKED_CAST")
            return EventsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

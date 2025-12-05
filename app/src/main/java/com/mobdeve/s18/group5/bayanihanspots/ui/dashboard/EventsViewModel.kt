package com.mobdeve.s18.group5.bayanihanspots.ui.dashboard

import android.app.Application
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
    data class Success(val events: List<Event>, val joinedEventIds: Set<String> = emptySet()) : EventsUiState
    data class Error(val message: String) : EventsUiState
}

/**
 * ViewModel that uses offline-first repository for events.
 * UI always reads from Room (single source of truth).
 * Firestore syncs in the background.
 */
class EventsViewModel(private val repository: OfflineFirstEventRepository) : ViewModel() {
    private val _uiState = MutableStateFlow<EventsUiState>(EventsUiState.Loading)
    private var joinedIds = emptySet<String>()
    val uiState: StateFlow<EventsUiState> = _uiState.asStateFlow()

    init {
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
                if (result.isFailure) {
                    println("Join failed: ${result.exceptionOrNull()?.message}")
                }
            }
        }
    }
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

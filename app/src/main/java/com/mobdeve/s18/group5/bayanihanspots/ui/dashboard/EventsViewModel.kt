package com.mobdeve.s18.group5.bayanihanspots.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mobdeve.s18.group5.bayanihanspots.data.events.Event
import com.mobdeve.s18.group5.bayanihanspots.data.events.EventsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface EventsUiState {
    object Loading : EventsUiState
    data class Success(val events: List<Event>) : EventsUiState
    data class Error(val message: String) : EventsUiState
}

class EventsViewModel(private val repository: EventsRepository) : ViewModel() {
    private val _uiState = MutableStateFlow<EventsUiState>(EventsUiState.Loading)
    val uiState: StateFlow<EventsUiState> = _uiState.asStateFlow()

    init {
        observeEvents()
    }

    private fun observeEvents() {
        viewModelScope.launch {
            repository.observeEvents().collect { result ->
                _uiState.value = result.fold(
                    onSuccess = { events -> EventsUiState.Success(events) },
                    onFailure = { EventsUiState.Error(it.message ?: "Unable to load events") }
                )
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = EventsUiState.Loading
            _uiState.value = repository.refreshEvents().fold(
                onSuccess = { EventsUiState.Success(emptyList()) },
                onFailure = { EventsUiState.Error(it.message ?: "Unable to refresh events") }
            )
        }
    }
}

class EventsViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EventsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return EventsViewModel(EventsRepository()) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

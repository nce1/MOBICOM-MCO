package com.mobdeve.s18.group5.bayanihanspots.ui.home

import android.app.Application
import android.location.Location
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mobdeve.s18.group5.bayanihanspots.data.repository.OfflineFirstSpotRepository
import com.mobdeve.s18.group5.bayanihanspots.data.spots.Spot
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface SpotsUiState {
    object Loading : SpotsUiState
    data class Success(val events: List<Spot>) : SpotsUiState
    data class Error(val message: String) : SpotsUiState
}

/**
 * ViewModel that uses offline-first repository.
 * UI always reads from Room (single source of truth).
 * Firestore syncs in the background.
 */
class HomeViewModel(private val repository: OfflineFirstSpotRepository): ViewModel(){
    private val _uiState = MutableStateFlow<SpotsUiState>(SpotsUiState.Loading)
    private var rawSpots: List<Spot> = emptyList()
    var userLocation by mutableStateOf<Location?>(null)
    val uiState: StateFlow<SpotsUiState> = _uiState.asStateFlow()

    init{
        observeSpots()
        // Start real-time sync from Firestore to Room
        repository.startRealtimeSync()
    }

    /**
     * Observe spots from Room database (offline-first)
     */
    private fun observeSpots() {
        viewModelScope.launch {
            repository.observeApprovedSpots().collect { spots ->
                rawSpots = spots
                val displaySpots = if (userLocation != null) {
                    repository.updateDistances(spots, userLocation)
                } else {
                    spots
                }
                _uiState.value = SpotsUiState.Success(displaySpots)
            }
        }
    }

    /**
     * Force refresh from Firestore
     */
    fun refresh() {
        viewModelScope.launch {
            _uiState.value = SpotsUiState.Loading
            val result = repository.forceRefresh()
            if (result.isFailure) {
                // Still show cached data even if refresh fails
                _uiState.value = SpotsUiState.Success(rawSpots)
            }
            // Success case is handled by observeSpots() flow
        }
    }

    fun updateUserLocation(location: Location) {
        userLocation = location
        val updatedSpots = repository.updateDistances(rawSpots, location)
        _uiState.value = SpotsUiState.Success(updatedSpots)
    }

    fun getSpotById(id: String): Spot? {
        return rawSpots.find { it.id == id }
    }
}

/**
 * Factory that creates HomeViewModel with Application context for Room database access.
 */
class HomeViewModelFactory(private val application: Application? = null) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            // If no application provided, fall back to legacy behavior
            val repository = if (application != null) {
                OfflineFirstSpotRepository.getInstance(application)
            } else {
                // Legacy fallback - will need context passed
                throw IllegalStateException("Application context required for offline-first mode. Use HomeViewModelFactory(application).")
            }
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
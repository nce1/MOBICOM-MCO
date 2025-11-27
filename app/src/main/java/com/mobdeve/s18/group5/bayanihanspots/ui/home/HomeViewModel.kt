package com.mobdeve.s18.group5.bayanihanspots.ui.home

import android.location.Location
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mobdeve.s18.group5.bayanihanspots.data.spots.Spot
import com.mobdeve.s18.group5.bayanihanspots.data.spots.SpotsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface SpotsUiState {
    object Loading : SpotsUiState
    data class Success(val events: List<Spot>) : SpotsUiState
    data class Error(val message: String) : SpotsUiState
}

class HomeViewModel(private val repository: SpotsRepository): ViewModel(){
    private val _uiState = MutableStateFlow<SpotsUiState>(SpotsUiState.Loading)
    private var rawSpots: List<Spot> = emptyList()
    var userLocation by mutableStateOf<Location?>(null)
    val uiState: StateFlow<SpotsUiState> = _uiState.asStateFlow()
    init{
        observeSpots()
    }

    private fun observeSpots() {
        viewModelScope.launch {
            repository.observeSpots().collect { result ->
                _uiState.value = result.fold(
                    onSuccess = { spots ->
                        rawSpots = spots
                        SpotsUiState.Success(spots) },
                    onFailure = { SpotsUiState.Error(it.message ?: "Unable to load home page") }
                )
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = SpotsUiState.Loading
            _uiState.value = repository.refreshSpots().fold(
                onSuccess = { SpotsUiState.Success(emptyList()) },
                onFailure = { SpotsUiState.Error(it.message ?: "Unable to refresh home page") }
            )
        }
    }

    fun updateUserLocation(location: Location) {
        userLocation = location
        val updatedSpots = repository.updateDistances(rawSpots, location)
        _uiState.value = SpotsUiState.Success(updatedSpots)
    }
}

class HomeViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(SpotsRepository()) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
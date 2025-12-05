package com.mobdeve.s18.group5.bayanihanspots.manage.signups

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.mobdeve.s18.group5.bayanihanspots.data.repository.OfflineFirstEventRepository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ManageSignupsViewModel(private val repository: OfflineFirstEventRepository): ViewModel() {
    private val _uiState = MutableStateFlow<List<SignupWithEvent>>(emptyList())
    val uiState: StateFlow<List<SignupWithEvent>> = _uiState.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadSignups()
    }

    private fun loadSignups() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            _isLoading.value = false
            return
        }

        viewModelScope.launch {
            repository.observeUserSignups(userId).collect { rawSignups ->
                _isLoading.value = true

                val combinedList = rawSignups.map { signup ->
                    val event = repository.getEventById(signup.eventId)
                    SignupWithEvent(signup, event)
                }

                val sortedList = combinedList.sortedWith(
                    compareByDescending<SignupWithEvent> { it.signup.status == "CONFIRMED" }
                        .thenByDescending { it.signup.timestamp }
                )

                _uiState.value = sortedList
                _isLoading.value = false
            }
        }
    }

    fun cancelSignup(signupId: String, eventId: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val result = repository.leaveEvent(signupId, eventId)
            if (result.isSuccess) {
                onSuccess()
            } else {
                onError(result.exceptionOrNull()?.message ?: "Unknown error")
            }
        }
    }
}

class ManageSignupsViewModelFactory(private val application: Application? = null): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ManageSignupsViewModel::class.java)) {
            val repository = if (application != null){
                OfflineFirstEventRepository.getInstance(application)
            } else{
                throw IllegalStateException("Application context required for ManageSignupsViewModelFactory.")
            }

            @Suppress("UNCHECKED_CAST")
            return ManageSignupsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
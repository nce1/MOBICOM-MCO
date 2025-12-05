package com.mobdeve.s18.group5.bayanihanspots.manage.programs

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.mobdeve.s18.group5.bayanihanspots.data.events.Event
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ManageProgramsViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    var myPrograms by mutableStateOf<List<Event>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    init {
        fetchUserPrograms()
    }

    fun fetchUserPrograms() {
        val userId = auth.currentUser?.uid ?: return

        isLoading = true
        viewModelScope.launch {
            try {
                val snapshot = firestore.collection("events")
                    .whereEqualTo("creatorId", userId)
                    .get()
                    .await()

                myPrograms = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Event::class.java)?.copy(id = doc.id)
                }

                isLoading = false
            } catch (e: Exception) {
                isLoading = false
            }
        }
    }

    fun refreshPrograms() {
        fetchUserPrograms()
    }

    fun getProgramById(programId: String): Event? {
        return myPrograms.find { it.id == programId }
    }

    fun deleteProgram(programId: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        viewModelScope.launch {
            try {
                firestore.collection("events").document(programId).delete().await()
                myPrograms = myPrograms.filter { it.id != programId }
                onSuccess()
            } catch (e: Exception) {
                onFailure(e)
            }
        }
    }
}


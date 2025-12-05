package com.mobdeve.s18.group5.bayanihanspots.manage.events

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.mobdeve.s18.group5.bayanihanspots.data.events.Event

class ManageEventsViewModel : ViewModel() {
    var myEvents by mutableStateOf<List<Event>>(emptyList())
        private set
    var isLoading by mutableStateOf(false)
        private set

    private var listenerRegistration: com.google.firebase.firestore.ListenerRegistration? = null

    fun fetchUserEvents() {
        val currentUser = FirebaseAuth.getInstance().currentUser ?: return

        // If already listening, don't set up another listener
        if (listenerRegistration != null) return

        isLoading = true

        listenerRegistration = FirebaseFirestore.getInstance().collection("events")
            .whereEqualTo("creatorId", currentUser.uid)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    myEvents = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(Event::class.java)?.copy(id = doc.id)
                    }
                }
                isLoading = false
            }
    }

    fun getEventById(id: String): Event? {
        return myEvents.find { it.id == id }
    }

    fun deleteEvent(eventId: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        FirebaseFirestore.getInstance().collection("events")
            .document(eventId)
            .delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onFailure(e) }
    }

    override fun onCleared() {
        super.onCleared()
        listenerRegistration?.remove()
    }
}


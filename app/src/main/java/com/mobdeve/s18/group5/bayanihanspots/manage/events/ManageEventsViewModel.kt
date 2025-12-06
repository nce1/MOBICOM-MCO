package com.mobdeve.s18.group5.bayanihanspots.manage.events

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.mobdeve.s18.group5.bayanihanspots.data.events.Event
import java.util.Date


class ManageEventsViewModel : ViewModel() {
    var myEvents by mutableStateOf<List<Event>>(emptyList())
        private set
    var isLoading by mutableStateOf(false)
        private set
    var isCanceling by mutableStateOf(false)
        private set
    private var listenerRegistration: com.google.firebase.firestore.ListenerRegistration? = null
    private val firestore = FirebaseFirestore.getInstance()
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

    // Canceling a live event with volunteers
    fun cancelEvent(event: Event, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        isCanceling = true
        val batch = firestore.batch()

        firestore.collection("signups")
            .whereEqualTo("eventId", event.id)
            .get()
            .addOnSuccessListener { snapshot ->

                for (doc in snapshot.documents) {
                    val userId = doc.getString("userId")

                    if (userId != null) {
                        val notifRef = firestore.collection("users")
                            .document(userId)
                            .collection("notifications")
                            .document()
                        val notificationData = hashMapOf(
                            "title" to "Event Cancelled",
                            "message" to "The event '${event.title}' has been cancelled by the host.",
                            "timestamp" to Date(),
                            "isRead" to false
                        )

                        batch.set(notifRef, notificationData)
                    }
                    batch.delete(doc.reference)
                }
                val eventRef = firestore.collection("events").document(event.id)
                batch.delete(eventRef)
                batch.commit()
                    .addOnSuccessListener {
                        isCanceling = false
                        onSuccess()
                    }
                    .addOnFailureListener { e ->
                        isCanceling = false
                        onFailure(e.message ?: "Unknown error occurred")
                    }
            }
            .addOnFailureListener { e ->
                isCanceling = false
                onFailure("Failed to check signups: ${e.message}")
            }
    }
    override fun onCleared() {
        super.onCleared()
        listenerRegistration?.remove()
    }
}


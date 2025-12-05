package com.mobdeve.s18.group5.bayanihanspots.manage.programs

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.mobdeve.s18.group5.bayanihanspots.data.events.Event

class ManageProgramsViewModel : ViewModel() {
    var myPrograms by mutableStateOf<List<Event>>(emptyList())
        private set
    var isLoading by mutableStateOf(false)
        private set

    private var hasFetched = false

    fun fetchUserPrograms() {
        if (hasFetched) return
        val currentUser = FirebaseAuth.getInstance().currentUser ?: return
        isLoading = true
        hasFetched = true

        FirebaseFirestore.getInstance().collection("events")
            .whereEqualTo("creatorId", currentUser.uid)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    myPrograms = snapshot.documents.mapNotNull { doc ->
                        try {
                            val locationValue = doc.get("location")
                            val locationLabel = when (locationValue) {
                                is String -> locationValue
                                is GeoPoint -> "Lat ${locationValue.latitude}, Lng ${locationValue.longitude}"
                                null -> doc.getString("locationLabel") ?: "Unknown location"
                                else -> locationValue.toString()
                            }

                            Event(
                                id = doc.id,
                                title = doc.getString("title") ?: "",
                                schedule = doc.getString("schedule") ?: doc.getString("date") ?: "",
                                locationLabel = locationLabel,
                                description = doc.getString("description") ?: "",
                                coordinates = doc.getGeoPoint("coordinates"),
                                host = doc.getString("host"),
                                maxVolunteers = doc.getLong("maxVolunteers")?.toInt(),
                                scheduleUtcMillis = doc.getLong("scheduleUtcMillis"),
                                creatorId = doc.getString("creatorId") ?: "",
                                approvalStatus = doc.getString("approvalStatus") ?: "PENDING",
                                modificationType = doc.getString("modificationType") ?: "NEW",
                                currentVolunteers = doc.getLong("currentVolunteers")?.toInt() ?: 0
                            )
                        } catch (e: Exception) {
                            null
                        }
                    }
                }
                isLoading = false
            }
    }

    fun getProgramById(id: String): Event? {
        return myPrograms.find { it.id == id }
    }

    fun deleteProgram(programId: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        FirebaseFirestore.getInstance().collection("events")
            .document(programId)
            .delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onFailure(e) }
    }

    fun refreshPrograms() {
        hasFetched = false
        fetchUserPrograms()
    }
}

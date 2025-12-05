package com.mobdeve.s18.group5.bayanihanspots.manage.spots

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.mobdeve.s18.group5.bayanihanspots.data.spots.Spot
import com.mobdeve.s18.group5.bayanihanspots.data.spots.toSpot

class ManageSpotsViewModel : ViewModel(){
    var mySpots by mutableStateOf<List<Spot>>(emptyList())
        private set
    var isLoading by mutableStateOf(false)
        private set

    private var hasFetched = false
    fun fetchUserSpots(){
        if (hasFetched) return
        val currentUser = FirebaseAuth.getInstance().currentUser ?: return
        isLoading = true
        hasFetched = true

        FirebaseFirestore.getInstance().collection("spots")
            .whereEqualTo("userID", currentUser.uid)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null){
                    mySpots = snapshot.documents.mapNotNull { it.toSpot() }
                }
                isLoading = false
            }
    }
    fun getSpotById(id: String): Spot?{
        return mySpots.find { it.id == id }
    }

    fun deleteSpot(spotId: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        FirebaseFirestore.getInstance().collection("spots")
            .document(spotId)
            .delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onFailure(e) }
    }
}
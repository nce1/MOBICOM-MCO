package com.mobdeve.s18.group5.bayanihanspots.data.spots

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.GeoPoint

data class Spot(
    val id: String,
    val name: String,
    val type: String,
    val status: String,
    val crowdLevel: String,
    val description: String,
    val coordinates: GeoPoint? = null,
    val userID: String,
    val approvalStatus: String = "PENDING",

    val imageList: List<String> = emptyList(),
    @get:Exclude
    var distanceString: String = ""
)
fun DocumentSnapshot.toSpot(): Spot?{
    val name = getString("name") ?: return null
    val type = getString("type") ?: return null
    val status = getString("status") ?: return null
    val crowdLevel = getString("crowdLevel") ?: return null
    val description = getString("description") ?: return null
    val coordinates = getGeoPoint("coordinates") ?: return null
    val userID = getString("userID") ?: return null
    val approvalStatus = getString("approvalStatus") ?: return null
    @Suppress("UNCHECKED_CAST")
    val imageList = get("imageList") as? List<String> ?: emptyList()

    return Spot(
        id = id,
        name = name,
        type = type,
        status = status,
        crowdLevel = crowdLevel,
        description = description,
        coordinates = coordinates,
        userID = userID,
        approvalStatus = approvalStatus,
        imageList = imageList
    )
}
package com.mobdeve.s18.group5.bayanihanspots.data.spots

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

    val imageList: List<String> = emptyList(),
    @get:Exclude
    var distanceString: String = ""
)
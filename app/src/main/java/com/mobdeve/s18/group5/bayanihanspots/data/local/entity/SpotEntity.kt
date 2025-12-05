package com.mobdeve.s18.group5.bayanihanspots.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mobdeve.s18.group5.bayanihanspots.data.spots.Spot
import com.google.firebase.firestore.GeoPoint

@Entity(tableName = "spots")
data class SpotEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val type: String,
    val status: String,
    val crowdLevel: String,
    val description: String,
    val latitude: Double?,
    val longitude: Double?,
    val userID: String,
    val approvalStatus: String,
    val modificationType: String,
    val imageList: String // JSON string of image URLs
) {
    fun toSpot(): Spot {
        return Spot(
            id = id,
            name = name,
            type = type,
            status = status,
            crowdLevel = crowdLevel,
            description = description,
            coordinates = if (latitude != null && longitude != null) GeoPoint(latitude, longitude) else null,
            userID = userID,
            approvalStatus = approvalStatus,
            modificationType = modificationType,
            imageList = parseImageList(imageList)
        )
    }

    companion object {
        fun fromSpot(spot: Spot): SpotEntity {
            return SpotEntity(
                id = spot.id,
                name = spot.name,
                type = spot.type,
                status = spot.status,
                crowdLevel = spot.crowdLevel,
                description = spot.description,
                latitude = spot.coordinates?.latitude,
                longitude = spot.coordinates?.longitude,
                userID = spot.userID,
                approvalStatus = spot.approvalStatus,
                modificationType = spot.modificationType,
                imageList = spot.imageList.joinToString("|||")
            )
        }

        private fun parseImageList(imageListStr: String): List<String> {
            return if (imageListStr.isBlank()) emptyList()
            else imageListStr.split("|||").filter { it.isNotBlank() }
        }
    }
}


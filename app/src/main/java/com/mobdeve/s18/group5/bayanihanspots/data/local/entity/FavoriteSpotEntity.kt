package com.mobdeve.s18.group5.bayanihanspots.data.local.entity

import androidx.room.Entity

@Entity(tableName = "favorite_spots", primaryKeys = ["spotId", "userId"])
data class FavoriteSpotEntity(
    val spotId: String,
    val userId: String,
    val timestamp: Long = System.currentTimeMillis()
)

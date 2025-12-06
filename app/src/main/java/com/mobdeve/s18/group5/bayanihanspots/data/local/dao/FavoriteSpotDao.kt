package com.mobdeve.s18.group5.bayanihanspots.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mobdeve.s18.group5.bayanihanspots.data.local.entity.FavoriteSpotEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteSpotDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFavorite(favoriteSpot: FavoriteSpotEntity)

    @Query("DELETE FROM favorite_spots WHERE spotId = :spotId AND userId = :userId")
    suspend fun removeFavorite(spotId: String, userId: String)

    @Query("SELECT * FROM favorite_spots WHERE userId = :userId")
    fun getFavoriteSpots(userId: String): Flow<List<FavoriteSpotEntity>>

    @Query("SELECT COUNT(*) FROM favorite_spots WHERE spotId = :spotId AND userId = :userId")
    fun isFavorite(spotId: String, userId: String): Flow<Int>
}

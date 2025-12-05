package com.mobdeve.s18.group5.bayanihanspots.data.local.dao

import androidx.room.*
import com.mobdeve.s18.group5.bayanihanspots.data.local.entity.SpotEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SpotDao {
    @Query("SELECT * FROM spots WHERE approvalStatus = 'APPROVED' ORDER BY name ASC")
    fun observeApprovedSpots(): Flow<List<SpotEntity>>

    @Query("SELECT * FROM spots ORDER BY name ASC")
    fun observeAllSpots(): Flow<List<SpotEntity>>

    @Query("SELECT * FROM spots WHERE id = :spotId")
    suspend fun getSpotById(spotId: String): SpotEntity?

    @Query("SELECT * FROM spots WHERE id = :spotId")
    fun observeSpotById(spotId: String): Flow<SpotEntity?>

    @Query("SELECT * FROM spots WHERE userID = :userId ORDER BY name ASC")
    fun observeSpotsByUser(userId: String): Flow<List<SpotEntity>>

    @Query("SELECT * FROM spots WHERE type = :type AND approvalStatus = 'APPROVED'")
    fun observeSpotsByType(type: String): Flow<List<SpotEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSpot(spot: SpotEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSpots(spots: List<SpotEntity>)

    @Update
    suspend fun updateSpot(spot: SpotEntity)

    @Delete
    suspend fun deleteSpot(spot: SpotEntity)

    @Query("DELETE FROM spots WHERE id = :spotId")
    suspend fun deleteSpotById(spotId: String)

    @Query("DELETE FROM spots")
    suspend fun deleteAllSpots()

    @Transaction
    suspend fun replaceAllSpots(spots: List<SpotEntity>) {
        deleteAllSpots()
        insertSpots(spots)
    }
}


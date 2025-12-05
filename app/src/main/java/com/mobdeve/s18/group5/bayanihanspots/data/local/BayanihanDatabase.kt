package com.mobdeve.s18.group5.bayanihanspots.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.mobdeve.s18.group5.bayanihanspots.data.local.dao.EventDao
import com.mobdeve.s18.group5.bayanihanspots.data.local.dao.PendingUploadDao
import com.mobdeve.s18.group5.bayanihanspots.data.local.dao.SpotDao
import com.mobdeve.s18.group5.bayanihanspots.data.local.entity.EventEntity
import com.mobdeve.s18.group5.bayanihanspots.data.local.entity.PendingUploadEntity
import com.mobdeve.s18.group5.bayanihanspots.data.local.entity.SpotEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [SpotEntity::class, EventEntity::class, PendingUploadEntity::class],
    version = 1,
    exportSchema = false
)
abstract class BayanihanDatabase : RoomDatabase() {
    abstract fun spotDao(): SpotDao
    abstract fun eventDao(): EventDao
    abstract fun pendingUploadDao(): PendingUploadDao

    companion object {
        @Volatile
        private var INSTANCE: BayanihanDatabase? = null

        fun getInstance(context: Context): BayanihanDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BayanihanDatabase::class.java,
                    "bayanihan_database"
                )
                    .addCallback(SeedDatabaseCallback())
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    /**
     * Callback to seed the database with initial spots on first launch.
     * This ensures the map is never empty during testing.
     */
    private class SeedDatabaseCallback : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    seedDatabase(database.spotDao())
                }
            }
        }

        private suspend fun seedDatabase(spotDao: SpotDao) {
            // Pre-populate with ~20 curated spots in Manila/DLSU area
            val seedSpots = listOf(
                SpotEntity(
                    id = "seed_1",
                    name = "DLSU Henry Sy Sr. Hall Study Area",
                    type = "Study",
                    status = "Open",
                    crowdLevel = "Moderate",
                    description = "Quiet study area on the ground floor with WiFi and outlets. Air-conditioned with comfortable seating.",
                    latitude = 14.5647,
                    longitude = 120.9932,
                    userID = "system",
                    approvalStatus = "APPROVED",
                    modificationType = "SEED",
                    imageList = ""
                ),
                SpotEntity(
                    id = "seed_2",
                    name = "Rizal Park Open Plaza",
                    type = "Rest",
                    status = "Open",
                    crowdLevel = "Busy",
                    description = "Large open plaza perfect for relaxation. Plenty of shade from trees, benches available.",
                    latitude = 14.5831,
                    longitude = 120.9794,
                    userID = "system",
                    approvalStatus = "APPROVED",
                    modificationType = "SEED",
                    imageList = ""
                ),
                SpotEntity(
                    id = "seed_3",
                    name = "Intramuros Plaza Roma",
                    type = "Nature",
                    status = "Open",
                    crowdLevel = "Quiet",
                    description = "Historic plaza with gardens and fountains. Great for morning walks and photography.",
                    latitude = 14.5916,
                    longitude = 120.9733,
                    userID = "system",
                    approvalStatus = "APPROVED",
                    modificationType = "SEED",
                    imageList = ""
                ),
                SpotEntity(
                    id = "seed_4",
                    name = "Taft Avenue Food Park",
                    type = "Market",
                    status = "Open",
                    crowdLevel = "Packed",
                    description = "Street food stalls and small restaurants. Best during lunch hours. Affordable meals for students.",
                    latitude = 14.5632,
                    longitude = 120.9945,
                    userID = "system",
                    approvalStatus = "APPROVED",
                    modificationType = "SEED",
                    imageList = ""
                ),
                SpotEntity(
                    id = "seed_5",
                    name = "Barangay 666 Covered Court",
                    type = "Play",
                    status = "Open",
                    crowdLevel = "Moderate",
                    description = "Community basketball court open to residents. Has lighting for evening games.",
                    latitude = 14.5690,
                    longitude = 120.9901,
                    userID = "system",
                    approvalStatus = "APPROVED",
                    modificationType = "SEED",
                    imageList = ""
                ),
                SpotEntity(
                    id = "seed_6",
                    name = "Paco Park Garden",
                    type = "Nature",
                    status = "Open",
                    crowdLevel = "Quiet",
                    description = "Peaceful garden inside historic cemetery walls. Popular for weddings and quiet reading.",
                    latitude = 14.5814,
                    longitude = 120.9893,
                    userID = "system",
                    approvalStatus = "APPROVED",
                    modificationType = "SEED",
                    imageList = ""
                ),
                SpotEntity(
                    id = "seed_7",
                    name = "Malate Church Courtyard",
                    type = "Rest",
                    status = "Open",
                    crowdLevel = "Quiet",
                    description = "Shaded courtyard beside the historic church. Benches available, peaceful atmosphere.",
                    latitude = 14.5698,
                    longitude = 120.9866,
                    userID = "system",
                    approvalStatus = "APPROVED",
                    modificationType = "SEED",
                    imageList = ""
                ),
                SpotEntity(
                    id = "seed_8",
                    name = "Robinsons Manila Atrium",
                    type = "Study",
                    status = "Open",
                    crowdLevel = "Busy",
                    description = "Air-conditioned common area with seating. WiFi available, outlets limited.",
                    latitude = 14.5736,
                    longitude = 120.9852,
                    userID = "system",
                    approvalStatus = "APPROVED",
                    modificationType = "SEED",
                    imageList = ""
                ),
                SpotEntity(
                    id = "seed_9",
                    name = "SM Manila Rooftop Garden",
                    type = "Nature",
                    status = "Open",
                    crowdLevel = "Moderate",
                    description = "Urban garden on the rooftop level. Great city views, some seating available.",
                    latitude = 14.5880,
                    longitude = 120.9800,
                    userID = "system",
                    approvalStatus = "APPROVED",
                    modificationType = "SEED",
                    imageList = ""
                ),
                SpotEntity(
                    id = "seed_10",
                    name = "DLSU EGI Taft Study Corner",
                    type = "Study",
                    status = "Open",
                    crowdLevel = "Quiet",
                    description = "Quiet corner in the student housing lobby. WiFi, outlets, 24/7 access for residents.",
                    latitude = 14.5652,
                    longitude = 120.9928,
                    userID = "system",
                    approvalStatus = "APPROVED",
                    modificationType = "SEED",
                    imageList = ""
                ),
                SpotEntity(
                    id = "seed_11",
                    name = "Manila Baywalk Sunset Strip",
                    type = "Rest",
                    status = "Open",
                    crowdLevel = "Busy",
                    description = "Seaside promenade with benches facing the bay. Best visited during sunset.",
                    latitude = 14.5776,
                    longitude = 120.9725,
                    userID = "system",
                    approvalStatus = "APPROVED",
                    modificationType = "SEED",
                    imageList = ""
                ),
                SpotEntity(
                    id = "seed_12",
                    name = "Ermita Saturday Market",
                    type = "Market",
                    status = "Open",
                    crowdLevel = "Packed",
                    description = "Weekend pop-up market with local crafts, food, and vintage items. Saturdays only.",
                    latitude = 14.5766,
                    longitude = 120.9870,
                    userID = "system",
                    approvalStatus = "APPROVED",
                    modificationType = "SEED",
                    imageList = ""
                ),
                SpotEntity(
                    id = "seed_13",
                    name = "Vito Cruz LRT Station Plaza",
                    type = "Rest",
                    status = "Open",
                    crowdLevel = "Busy",
                    description = "Small plaza near the LRT station. Covered waiting area with some benches.",
                    latitude = 14.5636,
                    longitude = 120.9952,
                    userID = "system",
                    approvalStatus = "APPROVED",
                    modificationType = "SEED",
                    imageList = ""
                ),
                SpotEntity(
                    id = "seed_14",
                    name = "Water Refill Station - Taft",
                    type = "Study",
                    status = "Open",
                    crowdLevel = "Quiet",
                    description = "Small water station with a waiting area. Good for quick breaks.",
                    latitude = 14.5678,
                    longitude = 120.9912,
                    userID = "system",
                    approvalStatus = "APPROVED",
                    modificationType = "SEED",
                    imageList = ""
                ),
                SpotEntity(
                    id = "seed_15",
                    name = "DLSU Bloemen Hall Garden",
                    type = "Nature",
                    status = "Open",
                    crowdLevel = "Moderate",
                    description = "Small garden area between buildings. Shaded with benches, good for short breaks.",
                    latitude = 14.5649,
                    longitude = 120.9939,
                    userID = "system",
                    approvalStatus = "APPROVED",
                    modificationType = "SEED",
                    imageList = ""
                ),
                SpotEntity(
                    id = "seed_16",
                    name = "Manila City Hall Plaza",
                    type = "Rest",
                    status = "Open",
                    crowdLevel = "Moderate",
                    description = "Open plaza in front of city hall. Large space with some shade from trees.",
                    latitude = 14.5940,
                    longitude = 120.9820,
                    userID = "system",
                    approvalStatus = "APPROVED",
                    modificationType = "SEED",
                    imageList = ""
                ),
                SpotEntity(
                    id = "seed_17",
                    name = "Barangay 669 Mini Park",
                    type = "Play",
                    status = "Open",
                    crowdLevel = "Quiet",
                    description = "Small neighborhood park with playground equipment. Good for families.",
                    latitude = 14.5612,
                    longitude = 120.9878,
                    userID = "system",
                    approvalStatus = "APPROVED",
                    modificationType = "SEED",
                    imageList = ""
                ),
                SpotEntity(
                    id = "seed_18",
                    name = "Cafe de Lipa Study Lounge",
                    type = "Study",
                    status = "Open",
                    crowdLevel = "Moderate",
                    description = "Coffee shop with study-friendly atmosphere. WiFi, outlets, and good coffee.",
                    latitude = 14.5658,
                    longitude = 120.9918,
                    userID = "system",
                    approvalStatus = "APPROVED",
                    modificationType = "SEED",
                    imageList = ""
                ),
                SpotEntity(
                    id = "seed_19",
                    name = "Pedro Gil LRT Pocket Plaza",
                    type = "Rest",
                    status = "Open",
                    crowdLevel = "Busy",
                    description = "Small plaza near Pedro Gil station. Street vendors nearby, covered waiting area.",
                    latitude = 14.5765,
                    longitude = 120.9885,
                    userID = "system",
                    approvalStatus = "APPROVED",
                    modificationType = "SEED",
                    imageList = ""
                ),
                SpotEntity(
                    id = "seed_20",
                    name = "Quirino Grandstand Area",
                    type = "Play",
                    status = "Open",
                    crowdLevel = "Moderate",
                    description = "Large open area perfect for jogging and outdoor activities. Best visited early morning.",
                    latitude = 14.5821,
                    longitude = 120.9772,
                    userID = "system",
                    approvalStatus = "APPROVED",
                    modificationType = "SEED",
                    imageList = ""
                )
            )
            spotDao.insertSpots(seedSpots)
        }
    }
}


package com.mobdeve.s18.group5.bayanihanspots.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.mobdeve.s18.group5.bayanihanspots.data.local.dao.EventDao
import com.mobdeve.s18.group5.bayanihanspots.data.local.dao.EventSignupDao
import com.mobdeve.s18.group5.bayanihanspots.data.local.dao.FavoriteEventDao
import com.mobdeve.s18.group5.bayanihanspots.data.local.dao.PendingUploadDao
import com.mobdeve.s18.group5.bayanihanspots.data.local.dao.SpotDao
import com.mobdeve.s18.group5.bayanihanspots.data.local.entity.EventEntity
import com.mobdeve.s18.group5.bayanihanspots.data.local.entity.EventSignupEntity
import com.mobdeve.s18.group5.bayanihanspots.data.local.entity.FavoriteEventEntity
import com.mobdeve.s18.group5.bayanihanspots.data.local.entity.PendingUploadEntity
import com.mobdeve.s18.group5.bayanihanspots.data.local.entity.SpotEntity

@Database(
<<<<<<< Updated upstream
<<<<<<< Updated upstream
<<<<<<< Updated upstream
    entities = [SpotEntity::class, EventEntity::class, PendingUploadEntity::class],
=======
=======
>>>>>>> Stashed changes
=======
>>>>>>> Stashed changes
    entities = [
        SpotEntity::class,
        EventEntity::class,
        PendingUploadEntity::class,
        FavoriteEventEntity::class,
        EventSignupEntity::class
    ],
<<<<<<< Updated upstream
<<<<<<< Updated upstream
>>>>>>> Stashed changes
=======
>>>>>>> Stashed changes
=======
>>>>>>> Stashed changes
    version = 5,
    exportSchema = false
)
abstract class BayanihanDatabase : RoomDatabase() {
    abstract fun spotDao(): SpotDao
    abstract fun eventDao(): EventDao
    abstract fun pendingUploadDao(): PendingUploadDao
    abstract fun favoriteEventDao(): FavoriteEventDao
    abstract fun eventSignupDao(): EventSignupDao

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
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}


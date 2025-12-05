package com.mobdeve.s18.group5.bayanihanspots.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.mobdeve.s18.group5.bayanihanspots.data.local.dao.EventDao
import com.mobdeve.s18.group5.bayanihanspots.data.local.dao.PendingUploadDao
import com.mobdeve.s18.group5.bayanihanspots.data.local.dao.SpotDao
import com.mobdeve.s18.group5.bayanihanspots.data.local.entity.EventEntity
import com.mobdeve.s18.group5.bayanihanspots.data.local.entity.PendingUploadEntity
import com.mobdeve.s18.group5.bayanihanspots.data.local.entity.SpotEntity

@Database(
    entities = [SpotEntity::class, EventEntity::class, PendingUploadEntity::class],
    version = 6,
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
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}


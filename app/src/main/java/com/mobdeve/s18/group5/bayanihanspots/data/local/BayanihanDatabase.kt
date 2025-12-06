package com.mobdeve.s18.group5.bayanihanspots.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.mobdeve.s18.group5.bayanihanspots.data.local.dao.EventDao
import com.mobdeve.s18.group5.bayanihanspots.data.local.dao.FavoriteSpotDao
import com.mobdeve.s18.group5.bayanihanspots.data.local.dao.PendingUploadDao
import com.mobdeve.s18.group5.bayanihanspots.data.local.dao.SpotDao
import com.mobdeve.s18.group5.bayanihanspots.data.local.entity.EventEntity
import com.mobdeve.s18.group5.bayanihanspots.data.local.entity.FavoriteSpotEntity
import com.mobdeve.s18.group5.bayanihanspots.data.local.entity.PendingUploadEntity
import com.mobdeve.s18.group5.bayanihanspots.data.local.entity.SpotEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.InputStreamReader

@Database(
    entities = [
        SpotEntity::class,
        EventEntity::class,
        PendingUploadEntity::class,
        FavoriteSpotEntity::class
    ],
    version = 9,
    exportSchema = false
)
abstract class BayanihanDatabase : RoomDatabase() {
    abstract fun spotDao(): SpotDao
    abstract fun eventDao(): EventDao
    abstract fun pendingUploadDao(): PendingUploadDao
    abstract fun favoriteSpotDao(): FavoriteSpotDao


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
                    .addCallback(SpotDatabaseCallback(context))
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class SpotDatabaseCallback(private val context: Context) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    prePopulateSpots(database.spotDao())
                }
            }
        }

        private suspend fun prePopulateSpots(spotDao: SpotDao) {
            try {
                val inputStream = context.assets.open("spots_seed.json")
                val reader = InputStreamReader(inputStream)
                val spotListType = object : TypeToken<List<SpotEntity>>() {}.type
                val spots: List<SpotEntity> = Gson().fromJson(reader, spotListType)
                spotDao.insertSpots(spots)
                reader.close()
            } catch (e: Exception) {
                // Handle exception
            }
        }
    }
}

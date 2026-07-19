package com.example.mapai.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        CachedReport::class,
        CachedPlace::class,
        CachedSOS::class,
        CacheMetadata::class
    ],
    version = 1,
    exportSchema = false
)
abstract class MapAiDatabase : RoomDatabase() {
    abstract fun cachedReportDao(): CachedReportDao
    abstract fun cachedPlaceDao(): CachedPlaceDao
    abstract fun cachedSOSDao(): CachedSOSDao
    abstract fun cacheMetadataDao(): CacheMetadataDao

    companion object {
        @Volatile
        private var instance: MapAiDatabase? = null

        fun getInstance(context: Context): MapAiDatabase {
            return instance ?: synchronized(this) {
                val newInstance = Room.databaseBuilder(
                    context.applicationContext,
                    MapAiDatabase::class.java,
                    "mapai_cache.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                instance = newInstance
                newInstance
            }
        }
    }
}

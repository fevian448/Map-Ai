package com.example.mapai.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Insert
import androidx.room.Update
import androidx.room.Delete
import kotlinx.coroutines.flow.Flow

// ---- Report Cache ----
@Entity(tableName = "cached_reports")
data class CachedReport(
    @PrimaryKey val id: String,
    val type: String,
    val lat: Double,
    val lon: Double,
    val description: String,
    val reporter: String,
    val createdAt: Long,
    val confirmed: Int,
    val syncedToServer: Boolean = false
)

@Dao
interface CachedReportDao {
    @Query("SELECT * FROM cached_reports ORDER BY createdAt DESC LIMIT 200")
    fun getAllReports(): Flow<List<CachedReport>>

    @Query("SELECT * FROM cached_reports WHERE type = :type ORDER BY createdAt DESC")
    suspend fun getReportsByType(type: String): List<CachedReport>

    @Insert
    suspend fun insertReport(report: CachedReport)

    @Update
    suspend fun updateReport(report: CachedReport)

    @Delete
    suspend fun deleteReport(report: CachedReport)

    @Query("DELETE FROM cached_reports WHERE createdAt < :olderThan")
    suspend fun deleteOldReports(olderThan: Long)

    @Query("SELECT COUNT(*) FROM cached_reports")
    suspend fun getReportCount(): Int

    @Query("SELECT * FROM cached_reports WHERE syncedToServer = 0")
    suspend fun getUnsyncedReports(): List<CachedReport>

    @Query("UPDATE cached_reports SET syncedToServer = 1 WHERE id = :reportId")
    suspend fun markAsSynced(reportId: String)
}

// ---- Place Cache ----
@Entity(tableName = "cached_places")
data class CachedPlace(
    @PrimaryKey val id: String,
    val name: String,
    val category: String,
    val lat: Double,
    val lon: Double,
    val rating: Float,
    val isOpen: Boolean,
    val fuelPrice: String? = null,
    val extra: String? = null,
    val createdAt: Long
)

@Dao
interface CachedPlaceDao {
    @Query("SELECT * FROM cached_places WHERE category = :category ORDER BY rating DESC LIMIT 100")
    fun getPlacesByCategory(category: String): Flow<List<CachedPlace>>

    @Query("SELECT * FROM cached_places ORDER BY createdAt DESC LIMIT 200")
    fun getAllPlaces(): Flow<List<CachedPlace>>

    @Insert
    suspend fun insertPlace(place: CachedPlace)

    @Update
    suspend fun updatePlace(place: CachedPlace)

    @Delete
    suspend fun deletePlace(place: CachedPlace)

    @Query("DELETE FROM cached_places WHERE createdAt < :olderThan")
    suspend fun deleteOldPlaces(olderThan: Long)

    @Query("SELECT COUNT(*) FROM cached_places")
    suspend fun getPlaceCount(): Int
}

// ---- SOS Cache ----
@Entity(tableName = "cached_sos")
data class CachedSOS(
    @PrimaryKey val id: String,
    val user: String,
    val lat: Double,
    val lon: Double,
    val message: String,
    val createdAt: Long
)

@Dao
interface CachedSOSDao {
    @Query("SELECT * FROM cached_sos ORDER BY createdAt DESC LIMIT 50")
    fun getSOS(): Flow<List<CachedSOS>>

    @Insert
    suspend fun insertSOS(sos: CachedSOS)

    @Delete
    suspend fun deleteSOS(sos: CachedSOS)

    @Query("DELETE FROM cached_sos WHERE createdAt < :olderThan")
    suspend fun deleteOldSOS(olderThan: Long)
}

// ---- Cache Metadata ----
@Entity(tableName = "cache_metadata")
data class CacheMetadata(
    @PrimaryKey val key: String,
    val lastSyncTime: Long,
    val syncStatus: String // "success" or "pending"
)

@Dao
interface CacheMetadataDao {
    @Query("SELECT * FROM cache_metadata WHERE key = :key")
    suspend fun getMetadata(key: String): CacheMetadata?

    @Insert
    suspend fun insertMetadata(metadata: CacheMetadata)

    @Update
    suspend fun updateMetadata(metadata: CacheMetadata)
}

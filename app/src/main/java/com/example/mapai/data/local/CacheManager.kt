package com.example.mapai.data.local

import android.content.Context
import com.example.mapai.data.GeoPoint
import com.example.mapai.data.TrafficAlert
import com.example.mapai.data.AlertType
import com.example.mapai.data.Place
import com.example.mapai.data.PlaceCategory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CacheManager(context: Context) {
    private val db = MapAiDatabase.getInstance(context)
    private val reportDao = db.cachedReportDao()
    private val placeDao = db.cachedPlaceDao()
    private val sosDao = db.cachedSOSDao()
    private val metadataDao = db.cacheMetadataDao()

    // ---- Report Cache Operations ----
    fun observeReports(): Flow<List<TrafficAlert>> {
        return reportDao.getAllReports().map { reports ->
            reports.map { cached ->
                TrafficAlert(
                    id = cached.id,
                    type = AlertType.valueOf(cached.type),
                    point = GeoPoint(cached.lat, cached.lon),
                    description = cached.description,
                    reporter = cached.reporter,
                    timestamp = cached.createdAt,
                    confidence = 100,
                    confirmedBy = cached.confirmed
                )
            }
        }
    }

    suspend fun cacheReport(alert: TrafficAlert) {
        reportDao.insertReport(
            CachedReport(
                id = alert.id,
                type = alert.type.name,
                lat = alert.point.latitude,
                lon = alert.point.longitude,
                description = alert.description,
                reporter = alert.reporter,
                createdAt = alert.timestamp,
                confirmed = alert.confirmedBy,
                syncedToServer = false
            )
        )
    }

    suspend fun getUnsyncedReports(): List<CachedReport> {
        return reportDao.getUnsyncedReports()
    }

    suspend fun markReportSynced(reportId: String) {
        reportDao.markAsSynced(reportId)
    }

    suspend fun clearOldReports(ageInMillis: Long = 7 * 24 * 60 * 60 * 1000) {
        val cutoff = System.currentTimeMillis() - ageInMillis
        reportDao.deleteOldReports(cutoff)
    }

    // ---- Place Cache Operations ----
    fun observePlacesByCategory(category: PlaceCategory): Flow<List<Place>> {
        return placeDao.getPlacesByCategory(category.key).map { places ->
            places.map { cached ->
                Place(
                    id = cached.id,
                    name = cached.name,
                    category = category,
                    point = GeoPoint(cached.lat, cached.lon),
                    distanceMeters = 0.0, // Will be calculated on the fly
                    rating = cached.rating,
                    isOpen = cached.isOpen,
                    fuelPrice = cached.fuelPrice,
                    extra = cached.extra
                )
            }
        }
    }

    fun observeAllPlaces(): Flow<List<Pair<PlaceCategory, Place>>> {
        return placeDao.getAllPlaces().map { places ->
            places.mapNotNull { cached ->
                val category = PlaceCategory.ALL.find { it.key == cached.category } ?: return@mapNotNull null
                category to Place(
                    id = cached.id,
                    name = cached.name,
                    category = category,
                    point = GeoPoint(cached.lat, cached.lon),
                    distanceMeters = 0.0,
                    rating = cached.rating,
                    isOpen = cached.isOpen,
                    fuelPrice = cached.fuelPrice,
                    extra = cached.extra
                )
            }
        }
    }

    suspend fun cachePlace(place: Place) {
        placeDao.insertPlace(
            CachedPlace(
                id = place.id,
                name = place.name,
                category = place.category.key,
                lat = place.point.latitude,
                lon = place.point.longitude,
                rating = place.rating,
                isOpen = place.isOpen,
                fuelPrice = place.fuelPrice,
                extra = place.extra,
                createdAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun clearOldPlaces(ageInMillis: Long = 7 * 24 * 60 * 60 * 1000) {
        val cutoff = System.currentTimeMillis() - ageInMillis
        placeDao.deleteOldPlaces(cutoff)
    }

    // ---- SOS Cache Operations ----
    fun observeSOS(): Flow<List<CachedSOS>> = sosDao.getSOS()

    suspend fun cacheSOS(id: String, user: String, lat: Double, lon: Double, message: String) {
        sosDao.insertSOS(
            CachedSOS(
                id = id,
                user = user,
                lat = lat,
                lon = lon,
                message = message,
                createdAt = System.currentTimeMillis()
            )
        )
    }

    // ---- Sync Status ----
    suspend fun getLastSyncTime(key: String): Long {
        return metadataDao.getMetadata(key)?.lastSyncTime ?: 0L
    }

    suspend fun updateSyncTime(key: String, success: Boolean = true) {
        metadataDao.updateMetadata(
            CacheMetadata(
                key = key,
                lastSyncTime = System.currentTimeMillis(),
                syncStatus = if (success) "success" else "pending"
            )
        )
    }

    // ---- Cleanup ----
    suspend fun clearAllCache() {
        // Delete old data periodically to manage storage
        clearOldReports()
        clearOldPlaces()
        sosDao.deleteOldSOS(System.currentTimeMillis() - 24 * 60 * 60 * 1000) // 1 day for SOS
    }
}

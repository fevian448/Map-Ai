package com.example.mapai.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class GeoPoint(
    val latitude: Double,
    val longitude: Double
) : Parcelable

enum class TrafficLevel(val label: String, val colorHex: String) {
    FREE("Lancar", "#2ecc71"),
    SLOW("Padat", "#f39c12"),
    JAM("Macet", "#e74c3c")
}

enum class AlertType(
    val label: String,
    val emoji: String
) {
    HAZARD("Bahaya", "\uD83D\uDEA8"),
    POLICE("Polisi", "\uD83D\uDE94"),
    ACCIDENT("Kecelakaan", "\uD83D\uDEA8"),
    ROADWORK("Perbaikan", "\uD83D\uDEA9"),
    SPEED_CAM("Kamera", "\uD83D\uDCF8"),
    TRAFFIC("Macet", "\uD83D\uDCBF")
}

@Parcelize
data class TrafficAlert(
    val id: String,
    val type: AlertType,
    val point: GeoPoint,
    val description: String,
    val reporter: String,
    val timestamp: Long,
    val confidence: Int,
    val confirmedBy: Int
) : Parcelable

@Parcelize
data class PlaceCategory(
    val key: String,
    val label: String,
    val emoji: String
) : Parcelable {
    companion object {
        val FUEL = PlaceCategory("fuel", "BBM", "\u26FD")
        val FOOD = PlaceCategory("food", "Makanan", "\uD83C\uDF74")
        val PARKING = PlaceCategory("parking", "Parkir", "\uD83D\uDEA7")
        val HOSPITAL = PlaceCategory("hospital", "RS", "\uE2BE")
        val ATM = PlaceCategory("atm", "ATM", "\uD83D\uDCB0")
        val ALL = listOf(FUEL, FOOD, PARKING, HOSPITAL, ATM)
    }
}

@Parcelize
data class Place(
    val id: String,
    val name: String,
    val category: PlaceCategory,
    val point: GeoPoint,
    val distanceMeters: Double,
    val rating: Float,
    val isOpen: Boolean,
    val fuelPrice: String? = null,
    val extra: String? = null
) : Parcelable

@Parcelize
data class RouteSegment(
    val from: GeoPoint,
    val to: GeoPoint,
    val distanceMeters: Double,
    val traffic: TrafficLevel,
    val roadName: String
) : Parcelable

@Parcelize
data class RouteInfo(
    val points: List<GeoPoint>,
    val segments: List<RouteSegment>,
    val totalDistanceMeters: Double,
    val durationSeconds: Double,
    val freeFlowDurationSeconds: Double,
    val hasTolls: Boolean,
    val overallTraffic: TrafficLevel
) : Parcelable {
    val delaySeconds: Double
        get() = (durationSeconds - freeFlowDurationSeconds).coerceAtLeast(0.0)
}

@Parcelize
data class WeatherInfo(
    val condition: String,
    val emoji: String,
    val temperatureC: Int,
    val windKph: Int,
    val humidity: Int,
    val visibilityKm: Double,
    val roadRisk: String
) : Parcelable

@Parcelize
data class SpeedCamera(
    val id: String,
    val point: GeoPoint,
    val limitKmh: Int,
    val direction: String
) : Parcelable

package com.example.mapai.util

import com.example.mapai.data.GeoPoint
import org.osmdroid.util.GeoPoint as OsmGeoPoint
import kotlin.math.roundToInt

fun GeoPoint.toOsm(): OsmGeoPoint = OsmGeoPoint(latitude, longitude)

fun formatDistance(meters: Double): String = when {
    meters < 1000 -> "${meters.roundToInt()} m"
    else -> String.format("%.1f km", meters / 1000.0)
}

fun formatDuration(seconds: Double): String {
    val m = (seconds / 60).roundToInt()
    return if (m < 60) "$m mnt" else {
        val h = m / 60
        val rm = m % 60
        "${h} j ${rm} mnt"
    }
}

fun formatEta(seconds: Double): String {
    val etaMs = (System.currentTimeMillis() + seconds * 1000.0).toLong()
    val cal = java.util.Calendar.getInstance().apply { timeInMillis = etaMs }
    val h = cal.get(java.util.Calendar.HOUR_OF_DAY)
    val m = cal.get(java.util.Calendar.MINUTE)
    return String.format("%02d:%02d", h, m)
}

fun timeAgo(timestamp: Long): String {
    val diff = System.currentTimeMillis() - timestamp
    val m = (diff / 60000).toInt()
    return when {
        m < 1 -> "baru saja"
        m < 60 -> "$m mnt lalu"
        else -> "${m / 60} jam lalu"
    }
}

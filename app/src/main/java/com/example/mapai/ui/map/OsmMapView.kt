package com.example.mapai.ui.map

import android.graphics.Color
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.example.mapai.data.AlertType
import com.example.mapai.data.AppSettings
import com.example.mapai.data.GeoPoint
import com.example.mapai.data.SettingsStore
import com.example.mapai.util.toOsm
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase
import org.osmdroid.util.GeoPoint as OsmGeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.infowindow.InfoWindow

data class MapMarkers(
    val myLocation: GeoPoint? = null,
    val destination: GeoPoint? = null,
    val alerts: List<Pair<GeoPoint, AlertType>> = emptyList(),
    val speedCameras: List<GeoPoint> = emptyList(),
    val places: List<GeoPoint> = emptyList(),
    val route: List<GeoPoint> = emptyList()
)

@Composable
fun OsmMapView(
    markers: MapMarkers,
    modifier: Modifier = Modifier,
    mapSource: Int = AppSettings.MAP_SOURCE_OSM,
    onMapClick: (GeoPoint) -> Unit = {},
    onReady: (MapView) -> Unit = {}
) {
    val mapView = remember { MapView(androidx.compose.ui.platform.LocalContext.current) }
    val settings by SettingsStore.settings.collectAsState()

    LaunchedEffect(mapSource) {
        val source = when (mapSource) {
            AppSettings.MAP_SOURCE_TOPO -> TileSourceFactory.OpenTopo
            AppSettings.MAP_SOURCE_CYCLE -> TileSourceFactory.OpenCycleMap
            else -> TileSourceFactory.MAPNIK
        }
        mapView.setTileSource(source)
    }

    AndroidView(
        factory = { ctx ->
            mapView.apply {
                setMultiTouchControls(true)
                controller.setZoom(15.0)
                InfoWindow.closeAllInfoWindowsOn("")
            }
            onReady(mapView)
            mapView
        },
        modifier = modifier.fillMaxSize()
    ) { mv ->
        InfoWindow.closeAllInfoWindowsOn(mv)
        mv.overlays.clear()

        if (markers.route.isNotEmpty()) {
            val line = Polyline(mv).apply {
                setPoints(markers.route.map { it.toOsm() })
                outlinePaint.color = Color.argb(220, 33, 150, 243)
                outlinePaint.strokeWidth = 12f
            }
            mv.overlays.add(line)
        }

        markers.alerts.forEach { (p, type) ->
            val m = Marker(mv).apply {
                position = p.toOsm()
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                icon = null
                title = type.label
                textLabelBackgroundColor = Color.TRANSPARENT
                textLabelForegroundColor = Color.BLACK
                setTextIcon("${type.emoji}")
            }
            mv.overlays.add(m)
        }

        markers.speedCameras.forEach { p ->
            val m = Marker(mv).apply {
                position = p.toOsm()
                setTextIcon("\uD83D\uDCF8")
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
            }
            mv.overlays.add(m)
        }

        markers.places.forEach { p ->
            val m = Marker(mv).apply {
                position = p.toOsm()
                setTextIcon("\uD83D\uDCCD")
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
            }
            mv.overlays.add(m)
        }

        markers.destination?.let { p ->
            val m = Marker(mv).apply {
                position = p.toOsm()
                setTextIcon("\uD83C\uDDFA")
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            }
            mv.overlays.add(m)
        }

        markers.myLocation?.let { p ->
            val m = Marker(mv).apply {
                position = p.toOsm()
                setTextIcon("\uD83D\uDEA2")
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
            }
            mv.overlays.add(m)
        }

        mv.invalidate()
    }

    LaunchedEffect(markers.myLocation) {
        markers.myLocation?.let {
            mapView.controller.setCenter(it.toOsm())
        }
    }
}

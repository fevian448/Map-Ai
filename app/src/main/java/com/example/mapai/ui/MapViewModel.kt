package com.example.mapai.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mapai.data.AlertType
import com.example.mapai.data.GeoPoint
import com.example.mapai.data.MapRepository
import com.example.mapai.data.Place
import com.example.mapai.data.PlaceCategory
import com.example.mapai.data.RouteInfo
import com.example.mapai.data.SpeedCamera
import com.example.mapai.data.TrafficAlert
import com.example.mapai.data.TrafficLevel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class MapUiState(
    val myLocation: GeoPoint? = null,
    val destination: GeoPoint? = null,
    val destinationName: String = "",
    val route: RouteInfo? = null,
    val isNavigating: Boolean = false,
    val speedKmh: Float = 0f,
    val speedLimitKmh: Int = 50,
    val alerts: List<TrafficAlert> = emptyList(),
    val speedCameras: List<SpeedCamera> = emptyList(),
    val selectedCategory: PlaceCategory = PlaceCategory.FUEL,
    val places: List<Place> = emptyList(),
    val searchQuery: String = "",
    val searchResults: List<Place> = emptyList()
)

class MapViewModel : ViewModel() {

    private val _state = MutableStateFlow(MapUiState())
    val state: StateFlow<MapUiState> = _state.asStateFlow()

    fun setLocation(point: GeoPoint) {
        val first = _state.value.myLocation == null
        _state.value = _state.value.copy(myLocation = point)
        if (first) refreshAround(point)
    }

    fun refreshAround(point: GeoPoint) {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                alerts = MapRepository.generateAlerts(point),
                speedCameras = MapRepository.generateSpeedCameras(point),
                places = MapRepository.generatePlaces(point, _state.value.selectedCategory)
            )
        }
    }

    fun setDestination(point: GeoPoint, name: String) {
        _state.value = _state.value.copy(
            destination = point,
            destinationName = name,
            searchQuery = "",
            searchResults = emptyList()
        )
        val from = _state.value.myLocation ?: return
        viewModelScope.launch {
            _state.value = _state.value.copy(route = MapRepository.buildRoute(from, point))
        }
    }

    fun setDestinationRouteNull() {
        _state.value = _state.value.copy(destination = null, route = null, destinationName = "")
    }

    fun startNavigation() {
        _state.value = _state.value.copy(isNavigating = true)
        simulateSpeed()
    }

    fun stopNavigation() {
        _state.value = _state.value.copy(isNavigating = false, speedKmh = 0f)
    }

    private fun simulateSpeed() {
        viewModelScope.launch {
            while (_state.value.isNavigating) {
                val limit = _state.value.speedLimitKmh
                val target = if (Math.random() > 0.8) limit + 15 else (limit - 10..limit + 5).random()
                val current = _state.value.speedKmh
                val next = (current + (target - current) * 0.4f).coerceAtLeast(0f)
                _state.value = _state.value.copy(
                    speedKmh = next,
                    speedLimitKmh = listOf(40, 50, 60, 80, 100).random()
                )
                delay(1500)
            }
        }
    }

    fun setCategory(category: PlaceCategory) {
        val loc = _state.value.myLocation ?: return
        _state.value = _state.value.copy(
            selectedCategory = category,
            places = MapRepository.generatePlaces(loc, category)
        )
    }

    fun search(query: String) {
        _state.value = _state.value.copy(searchQuery = query)
        val loc = _state.value.myLocation ?: return
        if (query.isBlank()) {
            _state.value = _state.value.copy(searchResults = emptyList())
            return
        }
        val all = PlaceCategory.ALL.flatMap { MapRepository.generatePlaces(loc, it) }
        _state.value = _state.value.copy(
            searchResults = all.filter {
                it.name.contains(query, ignoreCase = true) ||
                        it.category.label.contains(query, ignoreCase = true)
            }.take(8)
        )
    }

    fun addAlert(type: AlertType) {
        val loc = _state.value.myLocation ?: return
        val alert = TrafficAlert(
            id = "user_${System.currentTimeMillis()}",
            type = type,
            point = loc,
            description = "Laporan Anda: ${type.label}",
            reporter = "Anda",
            timestamp = System.currentTimeMillis(),
            confidence = 100,
            confirmedBy = 1
        )
        _state.value = _state.value.copy(alerts = listOf(alert) + _state.value.alerts)
    }

    fun nearbyAlertsFor(level: TrafficLevel? = null): List<TrafficAlert> =
        if (level == null) _state.value.alerts else _state.value.alerts.filter { it.type == AlertType.TRAFFIC }
}

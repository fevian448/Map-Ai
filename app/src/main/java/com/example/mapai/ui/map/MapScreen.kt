package com.example.mapai.ui.map

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mapai.data.GeoPoint
import com.example.mapai.data.TrafficLevel
import com.example.mapai.location.LocationProvider
import com.example.mapai.ui.MapViewModel
import com.example.mapai.util.formatDistance
import com.example.mapai.util.formatDuration
import com.example.mapai.util.formatEta
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.flow.collectLatest
import org.osmdroid.util.GeoPoint as OsmGeoPoint
import org.osmdroid.views.MapView

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MapScreen(
    viewModel: MapViewModel = viewModel(),
    onOpenSearch: () -> Unit = {},
    mapViewRef: (MapView) -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val locationPermission = rememberPermissionState(
        android.Manifest.permission.ACCESS_FINE_LOCATION
    )
    val locationProvider = remember { LocationProvider(context) }
    var mapView: MapView? by remember { mutableStateOf(null) }

    LaunchedEffect(locationPermission.status) {
        if (locationPermission.status.isGranted) {
            locationProvider.lastKnown { p -> p?.let { viewModel.setLocation(it) } }
            locationProvider.locationFlow().collectLatest { p ->
                viewModel.setLocation(p)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        OsmMapView(
            markers = MapMarkers(
                myLocation = state.myLocation,
                destination = state.destination,
                alerts = state.alerts.map { it.point to it.type },
                speedCameras = state.speedCameras.map { it.point },
                route = state.route?.points ?: emptyList()
            ),
            modifier = Modifier.fillMaxSize(),
            onReady = { mv ->
                mapView = mv
                mapViewRef(mv)
            }
        )

        TopSearchBar(onOpenSearch = onOpenSearch)

        FloatingActionButton(
            onClick = {
                state.myLocation?.let {
                    mapView?.controller?.animateTo(OsmGeoPoint(it.latitude, it.longitude))
                }
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = if (state.route != null) 210.dp else 96.dp)
        ) {
            Icon(Icons.Default.MyLocation, contentDescription = "Pusatkan")
        }

        state.route?.let { route ->
            RouteInfoCard(
                distance = route.totalDistanceMeters,
                duration = route.durationSeconds,
                traffic = route.overallTraffic,
                hasTolls = route.hasTolls,
                destinationName = state.destinationName,
                onStart = { viewModel.startNavigation() },
                onClose = { viewModel.setDestinationRouteNull() },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(12.dp)
            )
        }
    }
}

@Composable
private fun TopSearchBar(onOpenSearch: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
    ) {
        OutlinedTextField(
            value = "",
            onValueChange = {},
            readOnly = true,
            enabled = false,
            placeholder = { Text("Cari tujuan, alamat, atau tempat…") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .clickable { onOpenSearch() },
            colors = TextFieldDefaults.colors(
                disabledContainerColor = MaterialTheme.colorScheme.surface,
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledPlaceholderColor = MaterialTheme.colorScheme.outline
            ),
            shape = RoundedCornerShape(14.dp)
        )
    }
}

@Composable
private fun RouteInfoCard(
    distance: Double,
    duration: Double,
    traffic: TrafficLevel,
    hasTolls: Boolean,
    destinationName: String,
    onStart: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val trafficColor = when (traffic) {
        TrafficLevel.FREE -> Color(0xFF2ECC71)
        TrafficLevel.SLOW -> Color(0xFFF39C12)
        TrafficLevel.JAM -> Color(0xFFE74C3C)
    }
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(destinationName.ifBlank { "Tujuan" }, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                InfoItem(formatDuration(duration), "Waktu")
                InfoItem(formatDistance(distance), "Jarak")
                InfoItem(formatEta(duration), "Tiba")
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(trafficColor, RoundedCornerShape(50))
                )
                Spacer(Modifier.width(6.dp))
                Text(traffic.label, fontWeight = FontWeight.SemiBold)
                if (hasTolls) {
                    Spacer(Modifier.width(12.dp))
                    Text("• Ada tol", fontSize = 13.sp)
                }
            }
            Button(
                onClick = onStart,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
            ) {
                Text("Mulai Navigasi")
            }
        }
    }
}

@Composable
private fun InfoItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
    }
}

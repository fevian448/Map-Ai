package com.example.mapai.ui.drive

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mapai.data.MapRepository
import com.example.mapai.ui.MapViewModel
import com.example.mapai.ui.components.SectionTitle

@Composable
fun DriveScreen(viewModel: MapViewModel = viewModel()) {
    val state by viewModel.state.collectAsState()
    val speed = state.speedKmh
    val limit = state.speedLimitKmh
    val over = speed > limit
    val weather = rememberWeather()
    val speedCameras = state.speedCameras

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (over) Color(0xFFE74C3C) else MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "${speed.toInt()}",
                        fontSize = 64.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (over) Color.White else MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        "km/jam  •  batas ${limit}",
                        fontSize = 16.sp,
                        color = if (over) Color.White else MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    if (over) {
                        Text(
                            "⚠ Kecepatan berlebih!",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }

        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(weather.emoji, fontSize = 40.sp)
                    Column(modifier = Modifier.padding(start = 14.dp)) {
                        Text(
                            "${weather.condition}  ${weather.temperatureC}°C",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            "Angin ${weather.windKph} km/j • Lembap ${weather.humidity}% • Vis ${weather.visibilityKm} km",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                        Text(
                            "Jalan: ${weather.roadRisk}",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        item { SectionTitle("Kamera Kecepatan Terdekat (${speedCameras.size})") }

        items(speedCameras) { cam ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("\uD83D\uDCF8", fontSize = 26.sp)
                    Column(modifier = Modifier.padding(start = 12.dp)) {
                        Text("Batas ${cam.limitKmh} km/jam", fontWeight = FontWeight.Bold)
                        Text("Arah ${cam.direction}", fontSize = 13.sp, color = MaterialTheme.colorScheme.outline)
                    }
                }
            }
        }

        if (state.isNavigating) {
            item {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = { viewModel.stopNavigation() },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Hentikan Navigasi") }
                }
            }
        }
    }
}

@Composable
private fun rememberWeather() = androidx.compose.runtime.remember { MapRepository.currentWeather() }

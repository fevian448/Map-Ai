package com.example.mapai.ui.alerts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mapai.data.AlertType
import com.example.mapai.ui.MapViewModel
import com.example.mapai.ui.components.SectionTitle
import com.example.mapai.util.timeAgo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertsScreen(viewModel: MapViewModel = viewModel()) {
    val state by viewModel.state.collectAsState()
    var showReport by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.padding(16.dp, 16.dp, 16.dp, 0.dp)) {
            SectionTitle("Laporan Pengguna & Lalu Lintas")
            Text(
                "${state.alerts.size} laporan aktif di sekitar Anda",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.outline
            )
        }

        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(state.alerts) { alert ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(alert.type.emoji, fontSize = 28.sp)
                        Column(
                            modifier = Modifier
                                .padding(start = 12.dp)
                                .weight(1f)
                        ) {
                            Text(alert.type.label, fontWeight = FontWeight.Bold)
                            Text(alert.description, fontSize = 13.sp)
                            Text(
                                "${alert.reporter} • ${timeAgo(alert.timestamp)} • ${alert.confirmedBy} konfirmasi",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                        Text(
                            "${alert.confidence}%",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }

    if (showReport) {
        ReportSheet(
            onDismiss = { showReport = false },
            onPick = { type ->
                viewModel.addAlert(type)
                showReport = false
            }
        )
    } else {
        androidx.compose.foundation.layout.Box(
            modifier = Modifier.fillMaxSize()
        ) {
            FloatingActionButton(
                onClick = { showReport = true },
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.BottomEnd)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Laporkan")
            }
        }
    }
}

@Composable
private fun ReportSheet(
    onDismiss: () -> Unit,
    onPick: (AlertType) -> Unit
) {
    val options = listOf(
        AlertType.HAZARD, AlertType.POLICE, AlertType.ACCIDENT,
        AlertType.ROADWORK, AlertType.SPEED_CAM, AlertType.TRAFFIC
    )
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Laporkan di sini") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                options.forEach { type ->
                    Card(
                        onClick = { onPick(type) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(type.emoji, fontSize = 22.sp)
                            Text(
                                type.label,
                                modifier = Modifier.padding(start = 10.dp),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            androidx.compose.material3.TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}

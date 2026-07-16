package com.example.mapai.ui.sos

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mapai.data.SettingsStore
import com.example.mapai.ui.MapViewModel
import com.example.mapai.data.remote.ApiClient
import com.example.mapai.util.DeviceActions
import kotlinx.coroutines.launch

@Composable
fun SosScreen(viewModel: MapViewModel = viewModel()) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val settings = remember { SettingsStore.get() }
    var sent by remember { mutableStateOf(false) }

    val loc = state.myLocation ?: com.example.mapai.data.GeoPoint(-6.2, 106.8)

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("Darurat / SOS", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

        Button(
            onClick = {
                val body = "SOS dari MapAi! Lokasi: https://maps.google.com/?q=${loc.latitude},${loc.longitude}"
                if (settings.sosContactPhone.isNotBlank()) {
                    DeviceActions.sendSms(context, settings.sosContactPhone, body)
                    DeviceActions.shareLocation(context, loc.latitude, loc.longitude, "SOS-MapAi")
                }
                scope.launch {
                    try {
                        ApiClient.api().sendSos(
                            com.example.mapai.data.remote.SosDto(
                                user = settings.sosContactName.ifBlank { "anon" },
                                lat = loc.latitude, lon = loc.longitude, message = "SOS"
                            )
                        )
                    } catch (_: Exception) { }
                }
                sent = true
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp)
                .clip(RoundedCornerShape(18.dp)),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935))
        ) {
            Text(
                if (sent) "SOS TERKIRIM" else "TEKAN UNTUK SOS",
                color = Color.White, fontWeight = FontWeight.Bold, fontSize = 24.sp, textAlign = TextAlign.Center
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ActionCard(
                "Panggil", Icons.Default.Call, Modifier.weight(1f)
            ) { if (settings.sosContactPhone.isNotBlank()) DeviceActions.call(context, settings.sosContactPhone) }
            ActionCard(
                "Pesan", Icons.Default.Sms, Modifier.weight(1f)
            ) {
                if (settings.sosContactPhone.isNotBlank())
                    DeviceActions.sendSms(context, settings.sosContactPhone, "Darurat! Lokasi: ${loc.latitude},${loc.longitude}")
            }
            ActionCard(
                "Lokasi", Icons.Default.LocationOn, Modifier.weight(1f)
            ) { DeviceActions.shareLocation(context, loc.latitude, loc.longitude, "MapAi") }
        }

        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text("Kontak Darurat", fontWeight = FontWeight.Bold)
                Text(
                    if (settings.sosContactPhone.isNotBlank())
                        "${settings.sosContactName} • ${settings.sosContactPhone}"
                    else "Belum diatur. Buka Pengaturan → Kontak Darurat.",
                    fontSize = 13.sp, color = MaterialTheme.colorScheme.outline, modifier = Modifier.padding(top = 4.dp)
                )
                Text(
                    "Lokasi saat ini: ${String.format("%.5f", loc.latitude)}, ${String.format("%.5f", loc.longitude)}",
                    fontSize = 13.sp, modifier = Modifier.padding(top = 6.dp)
                )
            }
        }
    }
}

@Composable
private fun ActionCard(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier, onClick: () -> Unit) {
    Card(
        modifier = modifier,
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(28.dp))
            Text(label, modifier = Modifier.padding(top = 6.dp))
        }
    }
}

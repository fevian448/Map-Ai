package com.example.mapai.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mapai.data.AppSettings
import com.example.mapai.data.SettingsStore
import com.example.mapai.util.ALL_LANGUAGES

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen() {
    val settingsState by SettingsStore.settings.collectAsState()
    var settings by remember { mutableStateOf(settingsState) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Settings", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

        SettingCard("Language / Bahasa") {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ALL_LANGUAGES.forEach { (code, label) ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clickable { settings = settings.copy(language = code) }
                            .padding(end = 8.dp)
                    ) {
                        RadioButton(
                            selected = settings.language == code,
                            onClick = { settings = settings.copy(language = code) }
                        )
                        Text(label, fontSize = 13.sp)
                    }
                }
            }
        }

        SettingCard("Tema Warna / Theme Color") {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(
                    AppSettings.THEME_BLUE to "Biru",
                    AppSettings.THEME_GREEN to "Hijau",
                    AppSettings.THEME_RED to "Merah",
                    AppSettings.THEME_PURPLE to "Ungu",
                    AppSettings.THEME_ORANGE to "Oranye",
                    AppSettings.THEME_TEAL to "Teal"
                ).forEach { (v, label) ->
                    Row(verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { settings = settings.copy(themeColor = v) }) {
                        RadioButton(selected = settings.themeColor == v, onClick = { settings = settings.copy(themeColor = v) })
                        Text(label, fontSize = 13.sp)
                    }
                }
            }
        }

        SettingCard("Warna Teks / Text Color") {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(
                    AppSettings.TEXT_AUTO to "Auto",
                    AppSettings.TEXT_WHITE to "Putih",
                    AppSettings.TEXT_BLACK to "Hitam",
                    AppSettings.TEXT_YELLOW to "Kuning"
                ).forEach { (v, label) ->
                    Row(verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { settings = settings.copy(textColor = v) }) {
                        RadioButton(selected = settings.textColor == v, onClick = { settings = settings.copy(textColor = v) })
                        Text(label, fontSize = 13.sp)
                    }
                }
            }
        }

        SettingCard("Units / Satuan") {
            Row(verticalAlignment = Alignment.CenterVertically) {
                UnitOption("metric", "km / \u00B0C", settings.units) { settings = settings.copy(units = it) }
                UnitOption("imperial", "mi / \u00B0F", settings.units) { settings = settings.copy(units = it) }
            }
        }

        SettingCard("Map Source / Sumber Peta") {
            MapSourceOption(settings.mapSource) { settings = settings.copy(mapSource = it) }
        }

        SwitchRow("Gunakan Tema Perangkat (Material You)", settings.darkTheme) {
            settings = settings.copy(darkTheme = it)
        }
        SwitchRow("Konfigurasi Otomatis", settings.autoConfig) { settings = settings.copy(autoConfig = it) }
        SwitchRow("Hanya Mode Offline", settings.offlineOnly) { settings = settings.copy(offlineOnly = it) }

        OutlinedTextField(
            value = settings.serverUrl,
            onValueChange = { settings = settings.copy(serverUrl = it) },
            label = { Text("URL Server Backend") },
            modifier = Modifier.fillMaxWidth()
        )
        Text("Contoh: http://10.0.2.2:3000", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)

        OutlinedTextField(
            value = settings.sosContactName,
            onValueChange = { settings = settings.copy(sosContactName = it) },
            label = { Text("Nama Kontak Darurat") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = settings.sosContactPhone,
            onValueChange = { settings = settings.copy(sosContactPhone = it) },
            label = { Text("Telepon Kontak Darurat") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = { SettingsStore.save(settings) },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Simpan / Save") }
    }
}

@Composable
private fun SettingCard(title: String, content: @Composable () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(title, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
            content()
        }
    }
}

@Composable
private fun UnitOption(value: String, label: String, selected: String, onSelect: (String) -> Unit) {
    Row(Modifier.clickable { onSelect(value) }) {
        RadioButton(selected = selected == value, onClick = { onSelect(value) })
        Text(label, modifier = Modifier.padding(start = 4.dp, end = 12.dp))
    }
}

@Composable
private fun MapSourceOption(selected: Int, onSelect: (Int) -> Unit) {
    Column {
        listOf(
            AppSettings.MAP_SOURCE_OSM to "OpenStreetMap (default)",
            AppSettings.MAP_SOURCE_TOPO to "Topografi",
            AppSettings.MAP_SOURCE_CYCLE to "Cycle / Light"
        ).forEach { (v, label) ->
            Row(Modifier.clickable { onSelect(v) }) {
                RadioButton(selected = selected == v, onClick = { onSelect(v) })
                Text(label, modifier = Modifier.padding(start = 4.dp, end = 12.dp))
            }
        }
    }
}

@Composable
private fun SwitchRow(label: String, checked: Boolean, onToggle: (Boolean) -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, modifier = Modifier.weight(1f))
            Switch(checked = checked, onCheckedChange = onToggle)
        }
    }
}

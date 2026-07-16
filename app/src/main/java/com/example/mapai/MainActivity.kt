package com.example.mapai

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Report
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mapai.ui.MapViewModel
import com.example.mapai.ui.alerts.AlertsScreen
import com.example.mapai.ui.drive.DriveScreen
import com.example.mapai.ui.explore.ExploreScreen
import com.example.mapai.ui.map.MapScreen
import com.example.mapai.ui.profile.ProfileScreen
import com.example.mapai.ui.search.SearchScreen
import com.example.mapai.ui.theme.MapAiTheme
import org.osmdroid.config.Configuration
import com.example.mapai.util.applyLanguageSetting

class MainActivity : ComponentActivity() {
    override fun attachBaseContext(newBase: Context) {
        com.example.mapai.data.SettingsStore.init(newBase)
        super.attachBaseContext(newBase.applyLanguageSetting())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Configuration.getInstance().load(this, getSharedPreferences("mapai", MODE_PRIVATE))
        enableEdgeToEdge()
        setContent {
            MapAiTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    MapAiApp()
                }
            }
        }
    }
}

enum class AppDestinations(
    val label: String,
    val icon: ImageVector
) {
    MAP("Peta", Icons.Filled.Map),
    ALERTS("Laporan", Icons.Filled.Report),
    EXPLORE("Jelajah", Icons.Filled.Explore),
    DRIVE("Kemudi", Icons.Filled.Speed),
    PROFILE("Profil", Icons.Filled.AccountCircle)
}

@Composable
fun MapAiApp(viewModel: MapViewModel = viewModel()) {
    var currentDestination by remember { mutableStateOf(AppDestinations.MAP) }
    var showSearch by remember { mutableStateOf(false) }

    if (showSearch) {
        SearchScreen(
            viewModel = viewModel,
            onBack = { showSearch = false }
        )
        return
    }

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            AppDestinations.entries.forEach {
                item(
                    icon = { Icon(it.icon, contentDescription = it.label) },
                    label = { Text(it.label) },
                    selected = it == currentDestination,
                    onClick = { currentDestination = it }
                )
            }
        }
    ) {
        when (currentDestination) {
            AppDestinations.MAP -> MapScreen(
                viewModel = viewModel,
                onOpenSearch = { showSearch = true }
            )
            AppDestinations.ALERTS -> AlertsScreen(viewModel = viewModel)
            AppDestinations.EXPLORE -> ExploreScreen(viewModel = viewModel)
            AppDestinations.DRIVE -> DriveScreen(viewModel = viewModel)
            AppDestinations.PROFILE -> ProfileScreen()
        }
    }
}

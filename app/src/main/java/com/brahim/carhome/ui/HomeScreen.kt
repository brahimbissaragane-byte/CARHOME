package com.brahim.carhome.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AcUnit
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.Cloud
import androidx.compose.material.icons.rounded.FilterDrama
import androidx.compose.material.icons.rounded.Grain
import androidx.compose.material.icons.rounded.Thunderstorm
import androidx.compose.material.icons.rounded.Umbrella
import androidx.compose.material.icons.rounded.WbCloudy
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material.icons.rounded.NightsStay
import androidx.compose.material.icons.rounded.GpsFixed
import androidx.compose.material.icons.rounded.GpsOff
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.brahim.carhome.Screen
import com.brahim.carhome.data.AppEntry
import com.brahim.carhome.data.Prefs
import com.brahim.carhome.data.Weather
import com.brahim.carhome.data.WeatherKind
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    prefs: Prefs,
    speedKmh: Float,
    gpsOk: Boolean,
    weather: Weather?,
    pinnedApps: List<AppEntry>,
    onOpen: (Screen) -> Unit,
    onLaunchApp: (AppEntry) -> Unit,
) {
    Column(Modifier.fillMaxSize().padding(24.dp)) {
        TopBar(speedKmh, gpsOk, weather)
        Spacer(Modifier.height(20.dp))
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 170.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item { Tile("Navigateur", onClick = { onOpen(Screen.BROWSER) }) { TileIcon(Icons.Rounded.Language, CarColors.Blue) } }
            item { Tile("Favoris", onClick = { onOpen(Screen.FAVORITES) }) { TileIcon(Icons.Rounded.Star, CarColors.Amber) } }
            item { Tile("Applications", onClick = { onOpen(Screen.APPS) }) { TileIcon(Icons.Rounded.Apps, CarColors.Green) } }
            item { Tile("Réglages", onClick = { onOpen(Screen.SETTINGS) }) { TileIcon(Icons.Rounded.Settings, CarColors.Gray) } }
            items(pinnedApps, key = { it.packageName }) { app ->
                Tile(app.label, onClick = { onLaunchApp(app) }) { TileAppIcon(app.icon) }
            }
        }
        if (pinnedApps.isEmpty()) {
            Spacer(Modifier.height(16.dp))
            Text(
                "Astuce : dans Applications, appuie longtemps sur une appli (Waze, Spotify…) pour l'épingler ici.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun TopBar(speedKmh: Float, gpsOk: Boolean, weather: Weather?) {
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            now = System.currentTimeMillis()
            delay(1_000)
        }
    }
    val time = remember(now / 60_000) { SimpleDateFormat("HH:mm", Locale.FRANCE).format(Date(now)) }
    val date = remember(now / 3_600_000) { SimpleDateFormat("EEEE d MMMM", Locale.FRANCE).format(Date(now)) }

    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(time, fontSize = 56.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.width(20.dp))
        Text(
            date.replaceFirstChar { it.titlecase(Locale.FRANCE) },
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
        if (weather != null) {
            WeatherPill(weather)
            Spacer(Modifier.width(12.dp))
        }
        Row(
            Modifier
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(50))
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                if (gpsOk) Icons.Rounded.GpsFixed else Icons.Rounded.GpsOff,
                contentDescription = null,
                tint = if (gpsOk) CarColors.Green else CarColors.Red,
                modifier = Modifier.size(28.dp),
            )
            Spacer(Modifier.width(10.dp))
            Text(
                if (gpsOk) "${speedKmh.toInt()} km/h" else "GPS off",
                style = MaterialTheme.typography.titleLarge,
            )
        }
    }
}

@Composable
private fun WeatherPill(weather: Weather) {
    val (icon, tint) = weatherIcon(weather)
    Row(
        Modifier
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(50))
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = weather.label, tint = tint, modifier = Modifier.size(28.dp))
        Spacer(Modifier.width(10.dp))
        Text("${weather.temperatureC}°", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.width(10.dp))
        Text(
            weather.label,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
        )
    }
}

private fun weatherIcon(weather: Weather): Pair<ImageVector, Color> = when (weather.kind) {
    WeatherKind.CLEAR ->
        if (weather.isDay) Icons.Rounded.WbSunny to CarColors.Amber
        else Icons.Rounded.NightsStay to Color(0xFFB0BEC5)
    WeatherKind.PARTLY_CLOUDY -> Icons.Rounded.FilterDrama to Color(0xFFCFD8DC)
    WeatherKind.CLOUDY -> Icons.Rounded.Cloud to Color(0xFFB0BEC5)
    WeatherKind.FOG -> Icons.Rounded.WbCloudy to Color(0xFF90A4AE)
    WeatherKind.DRIZZLE -> Icons.Rounded.Grain to CarColors.Blue
    WeatherKind.RAIN -> Icons.Rounded.Umbrella to CarColors.Blue
    WeatherKind.SNOW -> Icons.Rounded.AcUnit to Color.White
    WeatherKind.STORM -> Icons.Rounded.Thunderstorm to CarColors.Amber
}

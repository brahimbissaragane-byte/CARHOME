package com.brahim.carhome.ui

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.brahim.carhome.data.AppEntry
import com.brahim.carhome.data.Prefs

@Composable
fun AppsScreen(apps: List<AppEntry>, prefs: Prefs, onBack: () -> Unit, onLaunch: (AppEntry) -> Unit) {
    val context = LocalContext.current

    Column(Modifier.fillMaxSize().padding(24.dp)) {
        ScreenHeader("Applications", onBack)
        if (apps.isEmpty()) CircularProgressIndicator()
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 170.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            items(apps, key = { it.packageName }) { app ->
                val pinned = app.packageName in prefs.pinnedApps
                val pinBadge: (@Composable () -> Unit)? = if (pinned) {
                    @Composable {
                        Icon(Icons.Rounded.PushPin, contentDescription = "Épinglée", tint = CarColors.Amber, modifier = Modifier.size(28.dp))
                    }
                } else null
                Tile(
                    label = app.label,
                    onClick = { onLaunch(app) },
                    onLongClick = {
                        val nowPinned = prefs.togglePin(app.packageName)
                        Toast.makeText(
                            context,
                            if (nowPinned) "${app.label} épinglée à l'accueil" else "${app.label} retirée de l'accueil",
                            Toast.LENGTH_SHORT,
                        ).show()
                    },
                    badge = pinBadge,
                ) { TileAppIcon(app.icon) }
            }
        }
    }
}

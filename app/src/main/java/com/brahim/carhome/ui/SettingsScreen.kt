package com.brahim.carhome.ui

import android.content.Intent
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.Color
import com.brahim.carhome.data.CarPaint
import com.brahim.carhome.data.CarPhoto
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.brahim.carhome.data.Prefs
import com.brahim.carhome.data.SearchEngine
import kotlin.math.roundToInt

@Composable
fun SettingsScreen(prefs: Prefs, onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val pickPhoto = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) scope.launch {
            if (CarPhoto.save(context, uri)) {
                prefs.updateCarPhotoVersion(System.currentTimeMillis())
            } else {
                Toast.makeText(context, "Impossible de lire cette image", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun openSystem(action: String) {
        val intent = Intent(action).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        runCatching { context.startActivity(intent) }.onFailure {
            context.startActivity(Intent(Settings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        ScreenHeader("Réglages", onBack)

        Section("Ma voiture")
        OutlinedTextField(
            value = prefs.carName,
            onValueChange = { prefs.updateCarName(it.take(30)) },
            label = { Text("Nom affiché sur l'accueil") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Text("Couleur du dessin")
        Row(
            Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            CarPaint.entries.forEach { paint ->
                FilterChip(
                    selected = prefs.carPaint == paint,
                    onClick = { prefs.updateCarPaint(paint) },
                    label = { Text(paint.label) },
                    leadingIcon = {
                        Box(Modifier.size(18.dp).background(Color(paint.argb), CircleShape))
                    },
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = {
                    pickPhoto.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                },
                modifier = Modifier.height(56.dp),
            ) { Text(if (prefs.carPhotoVersion != 0L) "Changer la photo" else "Mettre une photo de ma voiture") }
            if (prefs.carPhotoVersion != 0L) {
                OutlinedButton(
                    onClick = {
                        CarPhoto.delete(context)
                        prefs.updateCarPhotoVersion(0L)
                    },
                    modifier = Modifier.height(56.dp),
                ) { Text("Revenir au dessin") }
            }
        }

        Section("Blocage du navigateur en roulant")
        Text("Le navigateur se bloque au-dessus de ${prefs.speedThreshold} km/h (mesuré par le GPS).")
        Slider(
            value = prefs.speedThreshold.toFloat(),
            onValueChange = {
                // roundToInt : toInt() tronquerait 3,9999 en 3
                val kmh = it.roundToInt()
                if (kmh != prefs.speedThreshold) prefs.updateSpeedThreshold(kmh)
            },
            valueRange = 3f..20f,
            steps = 16,
        )

        Section("Navigateur")
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("Afficher la version ordinateur des sites", modifier = Modifier.weight(1f))
            Switch(checked = prefs.desktopMode, onCheckedChange = { prefs.updateDesktopMode(it) })
        }
        Text("Moteur de recherche")
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SearchEngine.entries.forEach { engine ->
                FilterChip(
                    selected = prefs.searchEngine == engine,
                    onClick = { prefs.updateSearchEngine(engine) },
                    label = { Text(engine.label) },
                )
            }
        }

        Section("Système")
        Button(onClick = { openSystem(Settings.ACTION_HOME_SETTINGS) }, modifier = Modifier.height(56.dp)) {
            Text("Définir CarHome comme écran d'accueil")
        }
        OutlinedButton(onClick = { openSystem(Settings.ACTION_SETTINGS) }, modifier = Modifier.height(56.dp)) {
            Text("Ouvrir les réglages Android")
        }

        Spacer(Modifier.height(12.dp))
        Text(
            "CarHome 1.0 — le navigateur est réservé à l'arrêt.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun Section(title: String) {
    Spacer(Modifier.height(8.dp))
    Text(title, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
}

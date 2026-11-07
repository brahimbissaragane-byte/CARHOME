package com.brahim.carhome.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.GpsOff
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val TileShape = RoundedCornerShape(28.dp)

/** Grosse tuile tactile, façon CarPlay. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Tile(
    label: String,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
    badge: (@Composable () -> Unit)? = null,
    icon: @Composable () -> Unit,
) {
    Box(
        Modifier
            .fillMaxWidth()
            .height(150.dp)
            .clip(TileShape)
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
    ) {
        Surface(color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.fillMaxSize()) {
            Column(
                Modifier.padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                icon()
                Spacer(Modifier.height(12.dp))
                Text(
                    label,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        if (badge != null) {
            Box(Modifier.align(Alignment.TopEnd).padding(10.dp)) { badge() }
        }
    }
}

@Composable
fun TileIcon(vector: ImageVector, color: Color) {
    Box(
        Modifier.size(76.dp).background(color, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(vector, contentDescription = null, tint = Color.White, modifier = Modifier.size(42.dp))
    }
}

@Composable
fun TileLetter(text: String, color: Color) {
    Box(
        Modifier.size(76.dp).background(color, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text.take(1).uppercase(),
            color = Color.White,
            fontSize = 34.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
fun TileAppIcon(bitmap: ImageBitmap) {
    Image(bitmap = bitmap, contentDescription = null, modifier = Modifier.size(76.dp))
}

/** Barre de titre avec bouton retour, pour les écrans secondaires. */
@Composable
fun ScreenHeader(title: String, onBack: () -> Unit, actions: @Composable () -> Unit = {}) {
    Row(
        Modifier.fillMaxWidth().padding(bottom = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        FilledTonalIconButton(onClick = onBack, modifier = Modifier.size(64.dp)) {
            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Retour", modifier = Modifier.size(32.dp))
        }
        Spacer(Modifier.width(20.dp))
        Text(title, style = MaterialTheme.typography.headlineMedium, modifier = Modifier.weight(1f))
        actions()
    }
}

/** Affiché à la place du navigateur quand la voiture roule (ou sans GPS). */
@Composable
fun LockedScreen(speedKmh: Float, gpsOk: Boolean, onHome: () -> Unit) {
    Column(
        Modifier.fillMaxSize().background(Color.Black).padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            if (gpsOk) Icons.Rounded.DirectionsCar else Icons.Rounded.GpsOff,
            contentDescription = null,
            tint = CarColors.Amber,
            modifier = Modifier.size(96.dp),
        )
        Spacer(Modifier.height(24.dp))
        Text(
            "Navigateur disponible à l'arrêt",
            style = MaterialTheme.typography.headlineLarge,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(12.dp))
        Text(
            if (gpsOk) "Vitesse actuelle : ${speedKmh.toInt()} km/h"
            else "GPS indisponible : autorise la localisation pour CarHome dans les réglages Android.",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(32.dp))
        Button(onClick = onHome, modifier = Modifier.height(64.dp)) {
            Icon(Icons.Rounded.Home, contentDescription = null)
            Spacer(Modifier.width(12.dp))
            Text("Retour à l'accueil", style = MaterialTheme.typography.titleMedium)
        }
    }
}

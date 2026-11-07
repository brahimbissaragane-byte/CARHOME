package com.brahim.carhome.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.brahim.carhome.data.NowPlaying
import com.brahim.carhome.data.NowPlayingMonitor

/** Carte « musique en cours » de l'accueil : pochette, titre, artiste, commandes. */
@Composable
fun MediaCard(
    music: NowPlaying?,
    hasAccess: Boolean,
    monitor: NowPlayingMonitor,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier
            .clip(RoundedCornerShape(28.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Pochette (ou note de musique), touche = ouvrir l'appli qui joue
        Box(
            Modifier
                .size(88.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(CarColors.Red.copy(alpha = 0.85f))
                .clickable(enabled = music != null) { monitor.openApp() },
            contentAlignment = Alignment.Center,
        ) {
            val art = music?.art
            if (art != null) {
                Image(art, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
            } else {
                Icon(Icons.Rounded.MusicNote, contentDescription = null, tint = Color.White, modifier = Modifier.size(44.dp))
            }
        }
        Spacer(Modifier.width(16.dp))

        when {
            !hasAccess -> Column(Modifier.weight(1f)) {
                Text("Musique", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    "Autorise CarHome à voir la musique en cours",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                )
                TextButton(onClick = { monitor.openAccessSettings() }) { Text("Autoriser") }
            }

            music == null -> Column(Modifier.weight(1f)) {
                Text("Aucune musique en cours", style = MaterialTheme.typography.titleMedium)
                Text(
                    "Lance Spotify, Deezer, la radio…",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            else -> {
                Column(Modifier.weight(1f)) {
                    Text(
                        music.title.ifBlank { music.appLabel },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        listOf(music.artist, music.appLabel).filter { it.isNotBlank() }.distinct().joinToString(" · "),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                ControlButton(Icons.Rounded.SkipPrevious, "Précédent") { monitor.previous() }
                FilledIconButton(onClick = { monitor.playPause() }, modifier = Modifier.size(64.dp), shape = CircleShape) {
                    Icon(
                        if (music.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                        contentDescription = if (music.isPlaying) "Pause" else "Lecture",
                        modifier = Modifier.size(36.dp),
                    )
                }
                ControlButton(Icons.Rounded.SkipNext, "Suivant") { monitor.next() }
            }
        }
    }
}

@Composable
private fun ControlButton(icon: ImageVector, description: String, onClick: () -> Unit) {
    IconButton(onClick = onClick, modifier = Modifier.size(56.dp)) {
        Icon(icon, contentDescription = description, modifier = Modifier.size(36.dp))
    }
}

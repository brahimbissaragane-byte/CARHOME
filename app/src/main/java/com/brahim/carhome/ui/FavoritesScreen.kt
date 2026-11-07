package com.brahim.carhome.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.brahim.carhome.data.Favorite
import com.brahim.carhome.data.Prefs

private val FavColors = listOf(CarColors.Blue, CarColors.Red, CarColors.Green, CarColors.Amber, CarColors.Gray)

@Composable
fun FavoritesScreen(prefs: Prefs, onBack: () -> Unit, onOpen: (Favorite) -> Unit) {
    var toDelete by remember { mutableStateOf<Favorite?>(null) }

    Column(Modifier.fillMaxSize().padding(24.dp)) {
        ScreenHeader("Favoris", onBack)
        if (prefs.favorites.isEmpty()) {
            Text(
                "Aucun favori. Dans le navigateur, appuie sur l'étoile pour ajouter une page.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 170.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            items(prefs.favorites, key = { it.url }) { fav ->
                val color = FavColors[(fav.title.hashCode() and 0x7fffffff) % FavColors.size]
                Tile(
                    label = fav.title,
                    onClick = { onOpen(fav) },
                    onLongClick = { toDelete = fav },
                ) { TileLetter(fav.title, color) }
            }
        }
    }

    toDelete?.let { fav ->
        AlertDialog(
            onDismissRequest = { toDelete = null },
            title = { Text("Supprimer ce favori ?") },
            text = { Text(fav.title) },
            confirmButton = {
                TextButton(onClick = {
                    prefs.removeFavorite(fav.url)
                    toDelete = null
                }) { Text("Supprimer") }
            },
            dismissButton = { TextButton(onClick = { toDelete = null }) { Text("Annuler") } },
        )
    }
}

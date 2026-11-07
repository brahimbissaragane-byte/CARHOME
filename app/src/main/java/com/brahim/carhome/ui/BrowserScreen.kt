package com.brahim.carhome.ui

import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.brahim.carhome.data.BrowserController
import com.brahim.carhome.data.Favorite
import com.brahim.carhome.data.Prefs

@Composable
fun BrowserScreen(controller: BrowserController, prefs: Prefs, onHome: () -> Unit) {
    val isFav = prefs.isFavorite(controller.currentUrl)

    Row(Modifier.fillMaxSize()) {
        // Barre latérale à gauche : garde toute la largeur pour la page
        Column(
            Modifier
                .width(96.dp)
                .fillMaxHeight()
                .background(MaterialTheme.colorScheme.surface),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly,
        ) {
            SideButton(Icons.Rounded.Home, "Accueil", onClick = onHome)
            SideButton(Icons.AutoMirrored.Rounded.ArrowBack, "Précédent", controller.canGoBack) { controller.goBack() }
            SideButton(Icons.AutoMirrored.Rounded.ArrowForward, "Suivant", controller.canGoForward) { controller.goForward() }
            SideButton(Icons.Rounded.Refresh, "Actualiser") { controller.reload() }
            SideButton(
                if (isFav) Icons.Rounded.Star else Icons.Rounded.StarBorder,
                if (isFav) "Retirer des favoris" else "Ajouter aux favoris",
                tint = if (isFav) CarColors.Amber else Color.White,
            ) {
                if (isFav) prefs.removeFavorite(controller.currentUrl)
                else prefs.addFavorite(
                    Favorite(controller.title.ifBlank { controller.currentUrl }, controller.currentUrl)
                )
            }
        }

        Column(Modifier.weight(1f)) {
            AddressBar(controller.currentUrl) { controller.load(prefs.toUrl(it)) }
            if (controller.progress < 100) {
                LinearProgressIndicator(
                    progress = { controller.progress / 100f },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            AndroidView(
                factory = {
                    // Le même WebView est réutilisé : on le détache de son ancien parent
                    (controller.webView.parent as? ViewGroup)?.removeView(controller.webView)
                    controller.webView
                },
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Composable
private fun SideButton(
    icon: ImageVector,
    description: String,
    enabled: Boolean = true,
    tint: Color = Color.White,
    onClick: () -> Unit,
) {
    IconButton(onClick = onClick, enabled = enabled, modifier = Modifier.size(72.dp)) {
        Icon(
            icon,
            contentDescription = description,
            tint = if (enabled) tint else tint.copy(alpha = 0.3f),
            modifier = Modifier.size(38.dp),
        )
    }
}

@Composable
private fun AddressBar(currentUrl: String, onSubmit: (String) -> Unit) {
    var text by remember(currentUrl) { mutableStateOf(currentUrl) }
    val keyboard = LocalSoftwareKeyboardController.current
    val submit = {
        if (text.isNotBlank()) {
            onSubmit(text)
            keyboard?.hide()
        }
    }
    OutlinedTextField(
        value = text,
        onValueChange = { text = it },
        singleLine = true,
        placeholder = { Text("Rechercher ou saisir une adresse") },
        leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri, imeAction = ImeAction.Go),
        keyboardActions = KeyboardActions(onGo = { submit() }),
        shape = RoundedCornerShape(28.dp),
        textStyle = MaterialTheme.typography.titleMedium,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
    )
}

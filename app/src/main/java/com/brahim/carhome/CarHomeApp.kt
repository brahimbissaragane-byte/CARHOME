package com.brahim.carhome

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.brahim.carhome.data.AppEntry
import com.brahim.carhome.data.BrowserController
import com.brahim.carhome.data.Prefs
import com.brahim.carhome.data.SpeedMonitor
import com.brahim.carhome.data.launchApp
import com.brahim.carhome.data.loadApps
import com.brahim.carhome.ui.AppsScreen
import com.brahim.carhome.ui.BrowserScreen
import com.brahim.carhome.ui.FavoritesScreen
import com.brahim.carhome.ui.HomeScreen
import com.brahim.carhome.ui.LockedScreen
import com.brahim.carhome.ui.SettingsScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

enum class Screen { HOME, BROWSER, FAVORITES, APPS, SETTINGS }

@Composable
fun CarHomeApp(prefs: Prefs, speedMonitor: SpeedMonitor) {
    val context = LocalContext.current
    var screen by rememberSaveable { mutableStateOf(Screen.HOME) }

    val speed by speedMonitor.speedKmh.collectAsState()
    val gpsOk by speedMonitor.gpsAvailable.collectAsState()

    // "En mouvement" passe à vrai tout de suite, et repasse à faux
    // seulement après 3 s sous le seuil (évite les clignotements du GPS).
    val overThreshold = speed > prefs.speedThreshold
    var moving by remember { mutableStateOf(false) }
    LaunchedEffect(overThreshold) {
        if (overThreshold) moving = true else {
            delay(3_000)
            moving = false
        }
    }
    // Sans GPS, on ne peut pas savoir si la voiture roule : on bloque par sécurité.
    val browserLocked = moving || !gpsOk

    val browser = remember { BrowserController(context) }
    DisposableEffect(Unit) { onDispose { browser.destroy() } }
    LaunchedEffect(prefs.desktopMode) { browser.setDesktopMode(prefs.desktopMode) }
    LaunchedEffect(browserLocked) { if (browserLocked) browser.pause() else browser.resume() }

    var apps by remember { mutableStateOf(emptyList<AppEntry>()) }
    LaunchedEffect(screen) {
        if (screen == Screen.HOME || screen == Screen.APPS) {
            apps = withContext(Dispatchers.IO) { loadApps(context) }
        }
    }

    fun openBrowser(url: String? = null) {
        when {
            url != null -> browser.load(url)
            browser.currentUrl.isEmpty() -> browser.load(prefs.searchEngine.homeUrl)
        }
        screen = Screen.BROWSER
    }

    BackHandler(enabled = screen != Screen.HOME) {
        if (screen == Screen.BROWSER && browser.canGoBack && !browserLocked) browser.goBack()
        else screen = Screen.HOME
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .imePadding()
    ) {
        when (screen) {
            Screen.HOME -> HomeScreen(
                prefs = prefs,
                speedKmh = speed,
                gpsOk = gpsOk,
                pinnedApps = apps.filter { it.packageName in prefs.pinnedApps },
                onOpen = { target ->
                    if (target == Screen.BROWSER) openBrowser() else screen = target
                },
                onLaunchApp = { launchApp(context, it.packageName) },
            )

            Screen.BROWSER ->
                if (browserLocked) {
                    LockedScreen(speedKmh = speed, gpsOk = gpsOk, onHome = { screen = Screen.HOME })
                } else {
                    BrowserScreen(controller = browser, prefs = prefs, onHome = { screen = Screen.HOME })
                }

            Screen.FAVORITES -> FavoritesScreen(
                prefs = prefs,
                onBack = { screen = Screen.HOME },
                onOpen = { openBrowser(it.url) },
            )

            Screen.APPS -> AppsScreen(
                apps = apps,
                prefs = prefs,
                onBack = { screen = Screen.HOME },
                onLaunch = { launchApp(context, it.packageName) },
            )

            Screen.SETTINGS -> SettingsScreen(prefs = prefs, onBack = { screen = Screen.HOME })
        }
    }
}

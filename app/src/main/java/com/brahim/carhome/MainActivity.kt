package com.brahim.carhome

import android.Manifest
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.brahim.carhome.data.Prefs
import com.brahim.carhome.data.SpeedMonitor
import com.brahim.carhome.ui.CarHomeTheme

class MainActivity : ComponentActivity() {

    private lateinit var prefs: Prefs
    private lateinit var speedMonitor: SpeedMonitor

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { speedMonitor.start() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = Prefs(this)
        speedMonitor = SpeedMonitor(this)

        // Plein écran + écran toujours allumé, comme un vrai écran de voiture
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        if (speedMonitor.hasPermission()) {
            speedMonitor.start()
        } else {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                )
            )
        }

        setContent {
            CarHomeTheme {
                CarHomeApp(prefs = prefs, speedMonitor = speedMonitor)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        speedMonitor.start()
    }

    override fun onDestroy() {
        speedMonitor.stop()
        super.onDestroy()
    }
}

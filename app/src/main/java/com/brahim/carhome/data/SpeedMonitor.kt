package com.brahim.carhome.data

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Suit la vitesse du véhicule avec le GPS.
 * - speedKmh : vitesse actuelle en km/h
 * - gpsAvailable : true seulement si une position GPS a été reçue il y a moins de
 *   [FIX_TIMEOUT_MS]. Sans position récente (pas encore de fix, tunnel, GPS coupé,
 *   permission refusée), on ne connaît pas la vitesse : false.
 * - location : dernière position connue (sert à la météo, peut être ancienne)
 */
class SpeedMonitor(private val context: Context) : LocationListener {

    private val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    private val handler = Handler(Looper.getMainLooper())
    private var running = false
    private var last: Location? = null

    private val _speedKmh = MutableStateFlow(0f)
    val speedKmh: StateFlow<Float> = _speedKmh

    private val _gpsAvailable = MutableStateFlow(false)
    val gpsAvailable: StateFlow<Boolean> = _gpsAvailable

    private val _location = MutableStateFlow<Location?>(null)
    val location: StateFlow<Location?> = _location

    /** Appelé quand aucune position n'est arrivée depuis trop longtemps. */
    private val fixLost = Runnable {
        _gpsAvailable.value = false
        last = null
    }

    fun hasPermission() = ContextCompat.checkSelfPermission(
        context, Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    @SuppressLint("MissingPermission")
    fun start() {
        if (running) return
        if (!hasPermission()) {
            _gpsAvailable.value = false
            return
        }
        try {
            // Position approximative pour afficher la météo sans attendre le premier fix.
            // Elle ne débloque PAS le navigateur.
            if (_location.value == null) {
                _location.value = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                    ?: runCatching { lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER) }.getOrNull()
            }
            lm.requestLocationUpdates(
                LocationManager.GPS_PROVIDER, 1000L, 0f, this, Looper.getMainLooper()
            )
            running = true
        } catch (e: Exception) {
            // Pas de GPS sur l'appareil ou permission retirée
            _gpsAvailable.value = false
        }
    }

    fun stop() {
        if (!running) return
        lm.removeUpdates(this)
        handler.removeCallbacks(fixLost)
        running = false
    }

    override fun onLocationChanged(location: Location) {
        val kmh = if (location.hasSpeed()) {
            location.speed * 3.6f
        } else {
            // Calcul de secours : distance / temps depuis la dernière position
            val prev = last
            if (prev == null) 0f else {
                val seconds = (location.time - prev.time) / 1000f
                if (seconds > 0f) location.distanceTo(prev) / seconds * 3.6f else _speedKmh.value
            }
        }
        last = location
        _location.value = location
        _speedKmh.value = kmh.coerceAtLeast(0f)
        _gpsAvailable.value = true
        handler.removeCallbacks(fixLost)
        handler.postDelayed(fixLost, FIX_TIMEOUT_MS)
    }

    override fun onProviderEnabled(provider: String) {
        // On attend une vraie position avant de considérer le GPS comme disponible.
    }

    override fun onProviderDisabled(provider: String) {
        handler.removeCallbacks(fixLost)
        fixLost.run()
    }

    // Nécessaire pour les anciennes versions d'Android (avant 10)
    @Deprecated("Deprecated in Java")
    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) = Unit

    companion object {
        private const val FIX_TIMEOUT_MS = 10_000L
    }
}

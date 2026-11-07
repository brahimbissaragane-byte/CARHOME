package com.brahim.carhome.data

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Suit la vitesse du véhicule avec le GPS.
 * - speedKmh : vitesse actuelle en km/h
 * - gpsAvailable : false si la localisation est refusée ou désactivée
 */
class SpeedMonitor(private val context: Context) : LocationListener {

    private val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    private var running = false
    private var last: Location? = null

    private val _speedKmh = MutableStateFlow(0f)
    val speedKmh: StateFlow<Float> = _speedKmh

    private val _gpsAvailable = MutableStateFlow(false)
    val gpsAvailable: StateFlow<Boolean> = _gpsAvailable

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
            lm.requestLocationUpdates(
                LocationManager.GPS_PROVIDER, 1000L, 0f, this, Looper.getMainLooper()
            )
            running = true
            _gpsAvailable.value = lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
        } catch (e: Exception) {
            // Pas de GPS sur l'appareil ou permission retirée
            _gpsAvailable.value = false
        }
    }

    fun stop() {
        if (!running) return
        lm.removeUpdates(this)
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
        _speedKmh.value = kmh.coerceAtLeast(0f)
        _gpsAvailable.value = true
    }

    override fun onProviderEnabled(provider: String) {
        _gpsAvailable.value = true
    }

    override fun onProviderDisabled(provider: String) {
        _gpsAvailable.value = false
    }

    // Nécessaire pour les anciennes versions d'Android (avant 10)
    @Deprecated("Deprecated in Java")
    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) = Unit
}

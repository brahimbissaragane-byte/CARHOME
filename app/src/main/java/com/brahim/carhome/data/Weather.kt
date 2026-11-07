package com.brahim.carhome.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale

enum class WeatherKind { CLEAR, PARTLY_CLOUDY, CLOUDY, FOG, DRIZZLE, RAIN, SNOW, STORM }

data class Weather(
    val temperatureC: Int,
    val kind: WeatherKind,
    val label: String,
    val isDay: Boolean,
)

/**
 * Météo actuelle via Open-Meteo (gratuit, sans clé).
 * La position est arrondie à ~1 km avant d'être envoyée.
 * Renvoie null en cas d'erreur (pas de réseau, réponse invalide…).
 */
suspend fun fetchWeather(latitude: Double, longitude: Double): Weather? = withContext(Dispatchers.IO) {
    runCatching {
        // Locale.US : sinon le français écrit "48,85" et l'URL est invalide
        val url = URL(
            String.format(
                Locale.US,
                "https://api.open-meteo.com/v1/forecast?latitude=%.2f&longitude=%.2f" +
                    "&current=temperature_2m,weather_code,is_day",
                latitude, longitude,
            )
        )
        val conn = url.openConnection() as HttpURLConnection
        try {
            conn.connectTimeout = 10_000
            conn.readTimeout = 10_000
            if (conn.responseCode != 200) return@runCatching null
            val body = conn.inputStream.bufferedReader().use { it.readText() }
            val current = JSONObject(body).getJSONObject("current")
            val (kind, label) = describeWmoCode(current.getInt("weather_code"))
            Weather(
                temperatureC = Math.round(current.getDouble("temperature_2m")).toInt(),
                kind = kind,
                label = label,
                isDay = current.optInt("is_day", 1) == 1,
            )
        } finally {
            conn.disconnect()
        }
    }.getOrNull()
}

/** Codes météo WMO utilisés par Open-Meteo. */
private fun describeWmoCode(code: Int): Pair<WeatherKind, String> = when (code) {
    0 -> WeatherKind.CLEAR to "Ensoleillé"
    1 -> WeatherKind.CLEAR to "Plutôt dégagé"
    2 -> WeatherKind.PARTLY_CLOUDY to "Partiellement nuageux"
    3 -> WeatherKind.CLOUDY to "Couvert"
    45, 48 -> WeatherKind.FOG to "Brouillard"
    51, 53, 55, 56, 57 -> WeatherKind.DRIZZLE to "Bruine"
    61, 63, 65, 66, 67 -> WeatherKind.RAIN to "Pluie"
    71, 73, 75, 77 -> WeatherKind.SNOW to "Neige"
    80, 81, 82 -> WeatherKind.RAIN to "Averses"
    85, 86 -> WeatherKind.SNOW to "Averses de neige"
    95, 96, 99 -> WeatherKind.STORM to "Orage"
    else -> WeatherKind.CLOUDY to "—"
}

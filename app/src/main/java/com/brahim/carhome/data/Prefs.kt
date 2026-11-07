package com.brahim.carhome.data

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder

data class Favorite(val title: String, val url: String)

enum class SearchEngine(val label: String, val homeUrl: String, val queryUrl: String) {
    GOOGLE("Google", "https://www.google.com", "https://www.google.com/search?q="),
    DUCKDUCKGO("DuckDuckGo", "https://duckduckgo.com", "https://duckduckgo.com/?q="),
    QWANT("Qwant", "https://www.qwant.com", "https://www.qwant.com/?q="),
    BING("Bing", "https://www.bing.com", "https://www.bing.com/search?q="),
}

/** Teintes de carrosserie proposées pour le dessin de la voiture (ARGB). */
enum class CarPaint(val label: String, val argb: Long) {
    BLANC("Blanc nacré", 0xFFF2F2EE),
    GRIS_CLAIR("Gris Platinium", 0xFF9AA3AD),
    GRIS_FONCE("Gris Artense", 0xFF5F656C),
    NOIR("Noir Perla Nera", 0xFF16181B),
    ROUGE("Rouge Ultimate", 0xFFB3121D),
    BLEU("Bleu Magnetic", 0xFF1F4E9C),
}

/** Réglages et favoris, sauvegardés sur la box et observables par Compose. */
class Prefs(context: Context) {

    private val sp = context.getSharedPreferences("carhome", Context.MODE_PRIVATE)

    var favorites by mutableStateOf(loadFavorites())
        private set
    var pinnedApps by mutableStateOf(sp.getStringSet(KEY_PINNED, emptySet())!!.toSet())
        private set
    var speedThreshold by mutableStateOf(sp.getInt(KEY_SPEED, 5))
        private set
    var desktopMode by mutableStateOf(sp.getBoolean(KEY_DESKTOP, false))
        private set
    var searchEngine by mutableStateOf(
        runCatching { SearchEngine.valueOf(sp.getString(KEY_ENGINE, null)!!) }
            .getOrDefault(SearchEngine.GOOGLE)
    )
        private set

    var carName by mutableStateOf(sp.getString(KEY_CAR_NAME, null) ?: "Peugeot 308")
        private set
    var carPaint by mutableStateOf(
        runCatching { CarPaint.valueOf(sp.getString(KEY_CAR_PAINT, null)!!) }
            .getOrDefault(CarPaint.GRIS_CLAIR)
    )
        private set

    /** Change à chaque nouvelle photo pour forcer son rechargement (0 = pas de photo). */
    var carPhotoVersion by mutableStateOf(sp.getLong(KEY_CAR_PHOTO, 0L))
        private set

    fun updateCarName(name: String) {
        carName = name
        sp.edit().putString(KEY_CAR_NAME, name).apply()
    }

    fun updateCarPaint(paint: CarPaint) {
        carPaint = paint
        sp.edit().putString(KEY_CAR_PAINT, paint.name).apply()
    }

    fun updateCarPhotoVersion(version: Long) {
        carPhotoVersion = version
        sp.edit().putLong(KEY_CAR_PHOTO, version).apply()
    }

    fun isFavorite(url: String) = favorites.any { it.url == url }

    fun addFavorite(fav: Favorite) {
        if (fav.url.isBlank() || isFavorite(fav.url)) return
        favorites = favorites + fav
        saveFavorites()
    }

    fun removeFavorite(url: String) {
        favorites = favorites.filterNot { it.url == url }
        saveFavorites()
    }

    fun togglePin(packageName: String): Boolean {
        val pinned = packageName !in pinnedApps
        pinnedApps = if (pinned) pinnedApps + packageName else pinnedApps - packageName
        sp.edit().putStringSet(KEY_PINNED, pinnedApps).apply()
        return pinned
    }

    fun updateSpeedThreshold(kmh: Int) {
        speedThreshold = kmh
        sp.edit().putInt(KEY_SPEED, kmh).apply()
    }

    fun updateDesktopMode(enabled: Boolean) {
        desktopMode = enabled
        sp.edit().putBoolean(KEY_DESKTOP, enabled).apply()
    }

    fun updateSearchEngine(engine: SearchEngine) {
        searchEngine = engine
        sp.edit().putString(KEY_ENGINE, engine.name).apply()
    }

    /** Transforme ce qui est tapé dans la barre d'adresse en URL (site ou recherche). */
    fun toUrl(input: String): String {
        val t = input.trim()
        if (t.startsWith("http://") || t.startsWith("https://")) return t
        if (!t.contains(' ') && t.contains('.')) return "https://$t"
        return searchEngine.queryUrl + URLEncoder.encode(t, "UTF-8")
    }

    private fun loadFavorites(): List<Favorite> {
        val raw = sp.getString(KEY_FAVORITES, null) ?: return DEFAULT_FAVORITES
        return runCatching {
            val arr = JSONArray(raw)
            List(arr.length()) { i ->
                val o = arr.getJSONObject(i)
                Favorite(o.getString("title"), o.getString("url"))
            }
        }.getOrDefault(DEFAULT_FAVORITES)
    }

    private fun saveFavorites() {
        val arr = JSONArray()
        favorites.forEach { arr.put(JSONObject().put("title", it.title).put("url", it.url)) }
        sp.edit().putString(KEY_FAVORITES, arr.toString()).apply()
    }

    companion object {
        private const val KEY_FAVORITES = "favorites"
        private const val KEY_PINNED = "pinned"
        private const val KEY_SPEED = "speed"
        private const val KEY_DESKTOP = "desktop"
        private const val KEY_ENGINE = "engine"
        private const val KEY_CAR_NAME = "car_name"
        private const val KEY_CAR_PAINT = "car_paint"
        private const val KEY_CAR_PHOTO = "car_photo"

        val DEFAULT_FAVORITES = listOf(
            Favorite("Google", "https://www.google.com"),
            Favorite("YouTube", "https://m.youtube.com"),
            Favorite("Météo-France", "https://meteofrance.com"),
            Favorite("Wikipédia", "https://fr.wikipedia.org"),
        )
    }
}

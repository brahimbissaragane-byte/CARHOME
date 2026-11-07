package com.brahim.carhome.data

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.service.notification.NotificationListenerService
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.app.NotificationManagerCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Service vide : Android exige un « écouteur de notifications » activé par l'utilisateur
 * pour qu'une appli puisse voir et piloter la musique des autres applis.
 * CarHome ne lit pas le contenu des notifications.
 */
class MediaListenerService : NotificationListenerService()

data class NowPlaying(
    val title: String,
    val artist: String,
    val art: ImageBitmap?,
    val isPlaying: Boolean,
    val appLabel: String,
    val packageName: String,
)

/** Suit la musique en cours (Spotify, YouTube Music, radio…) et permet de la piloter. */
class NowPlayingMonitor(private val context: Context) {

    private val sessions = context.getSystemService(Context.MEDIA_SESSION_SERVICE) as MediaSessionManager
    private val component = ComponentName(context, MediaListenerService::class.java)
    private val handler = Handler(Looper.getMainLooper())

    private val _hasAccess = MutableStateFlow(false)
    val hasAccess: StateFlow<Boolean> = _hasAccess

    private val _nowPlaying = MutableStateFlow<NowPlaying?>(null)
    val nowPlaying: StateFlow<NowPlaying?> = _nowPlaying

    private var listening = false
    private var controller: MediaController? = null
    private var lastArtKey: Any? = null
    private var lastArt: ImageBitmap? = null

    private val sessionsListener = MediaSessionManager.OnActiveSessionsChangedListener { list ->
        pick(list.orEmpty())
    }

    private val callback = object : MediaController.Callback() {
        override fun onMetadataChanged(metadata: MediaMetadata?) = publish()
        override fun onPlaybackStateChanged(state: PlaybackState?) = publish()
        override fun onSessionDestroyed() {
            controller = null
            refresh()
        }
    }

    fun accessGranted(): Boolean =
        NotificationManagerCompat.getEnabledListenerPackages(context).contains(context.packageName)

    /** Ouvre l'écran Android où l'on autorise CarHome à voir la musique en cours. */
    fun openAccessSettings() {
        val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        runCatching { context.startActivity(intent) }
    }

    /** À appeler au démarrage et à chaque retour dans l'appli (l'accès a pu être donné entre-temps). */
    fun start() {
        _hasAccess.value = accessGranted()
        if (!_hasAccess.value || listening) {
            if (listening) refresh()
            return
        }
        try {
            sessions.addOnActiveSessionsChangedListener(sessionsListener, component, handler)
            listening = true
            refresh()
        } catch (e: SecurityException) {
            _hasAccess.value = false
        }
    }

    fun stop() {
        if (listening) {
            runCatching { sessions.removeOnActiveSessionsChangedListener(sessionsListener) }
            listening = false
        }
        controller?.unregisterCallback(callback)
        controller = null
    }

    fun playPause() {
        val c = controller ?: return
        if (c.playbackState?.state == PlaybackState.STATE_PLAYING) c.transportControls.pause()
        else c.transportControls.play()
    }

    fun next() {
        controller?.transportControls?.skipToNext()
    }

    fun previous() {
        controller?.transportControls?.skipToPrevious()
    }

    /** Ouvre l'appli qui joue la musique. */
    fun openApp() {
        controller?.packageName?.let { launchApp(context, it) }
    }

    private fun refresh() {
        val list = runCatching { sessions.getActiveSessions(component) }.getOrNull()
        if (list == null) {
            _hasAccess.value = accessGranted()
            return
        }
        pick(list)
    }

    /** Garde la session qui joue ; sinon la plus récente. */
    private fun pick(list: List<MediaController>) {
        val best = list.firstOrNull { it.playbackState?.state == PlaybackState.STATE_PLAYING }
            ?: list.firstOrNull()
        if (best?.sessionToken != controller?.sessionToken) {
            controller?.unregisterCallback(callback)
            controller = best
            best?.registerCallback(callback, handler)
        }
        publish()
    }

    private fun publish() {
        val c = controller
        val meta = c?.metadata
        if (c == null || meta == null) {
            _nowPlaying.value = null
            return
        }
        val title = meta.getString(MediaMetadata.METADATA_KEY_TITLE)
            ?: meta.getString(MediaMetadata.METADATA_KEY_DISPLAY_TITLE)
            ?: ""
        val artist = meta.getString(MediaMetadata.METADATA_KEY_ARTIST)
            ?: meta.getString(MediaMetadata.METADATA_KEY_ALBUM_ARTIST)
            ?: meta.getString(MediaMetadata.METADATA_KEY_DISPLAY_SUBTITLE)
            ?: ""
        val bitmap: Bitmap? = meta.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART)
            ?: meta.getBitmap(MediaMetadata.METADATA_KEY_ART)
            ?: meta.getBitmap(MediaMetadata.METADATA_KEY_DISPLAY_ICON)
        // On ne reconvertit la pochette que si elle a changé
        if (bitmap !== lastArtKey) {
            lastArtKey = bitmap
            lastArt = bitmap?.let { runCatching { it.asImageBitmap() }.getOrNull() }
        }
        val pm = context.packageManager
        val label = runCatching {
            pm.getApplicationLabel(pm.getApplicationInfo(c.packageName, 0)).toString()
        }.getOrDefault(c.packageName)

        _nowPlaying.value = NowPlaying(
            title = title,
            artist = artist,
            art = lastArt,
            isPlaying = c.playbackState?.state == PlaybackState.STATE_PLAYING,
            appLabel = label,
            packageName = c.packageName,
        )
    }
}

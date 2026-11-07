package com.brahim.carhome.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/** Photo de la voiture choisie par l'utilisateur, copiée dans l'appli (redimensionnée). */
object CarPhoto {
    private const val FILE_NAME = "car_photo.jpg"
    private const val MAX_SIDE = 1280

    private fun file(context: Context) = File(context.filesDir, FILE_NAME)

    /** Copie l'image choisie dans l'appli. Renvoie false si elle n'a pas pu être lue. */
    suspend fun save(context: Context, uri: Uri): Boolean = withContext(Dispatchers.IO) {
        runCatching {
            val resolver = context.contentResolver
            // 1er passage : taille seulement, pour ne pas charger une photo de 12 Mpx en entier
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
            var sample = 1
            while (maxOf(bounds.outWidth, bounds.outHeight) / (sample * 2) >= MAX_SIDE) sample *= 2

            val bitmap = resolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply { inSampleSize = sample })
            } ?: return@runCatching false
            file(context).outputStream().use { bitmap.compress(Bitmap.CompressFormat.JPEG, 90, it) }
            bitmap.recycle()
            true
        }.getOrDefault(false)
    }

    suspend fun load(context: Context): ImageBitmap? = withContext(Dispatchers.IO) {
        val f = file(context)
        if (!f.exists()) null
        else runCatching { BitmapFactory.decodeFile(f.path)?.asImageBitmap() }.getOrNull()
    }

    fun delete(context: Context) {
        file(context).delete()
    }
}

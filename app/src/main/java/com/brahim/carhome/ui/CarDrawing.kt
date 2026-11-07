package com.brahim.carhome.ui

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.vector.PathParser
import kotlin.math.min

/**
 * Peugeot 308 (T9, 2013-2021) vue de profil, dessinée en vectoriel.
 * Les coordonnées sont dans une boîte de 360 × 150 ; le dessin s'adapte à la taille donnée.
 */
@Composable
fun Car308Drawing(paint: Color, modifier: Modifier = Modifier) {
    val paths = remember {
        CarPaths(
            body = path(
                "M14 118 L12 100 Q12 86 24 82 L66 74 Q100 66 120 62 L166 33 Q172 29 182 29 " +
                    "L284 30 Q306 31 318 38 Q324 41 324 46 L328 64 Q333 70 333 82 L334 104 " +
                    "Q334 116 326 118 L318 118 A33 33 0 0 0 252 118 L105 118 A33 33 0 0 0 39 118 Z"
            ),
            windows = path("M128 63 L170 37 Q175 34 183 34 L280 35 Q298 36 310 42 L316 56 Z"),
            pillars = path("M221 34 L228 34 L228 62 L221 62 Z M272 34 L281 34 L294 58 L285 58 Z"),
            mirror = path("M132 62 L146 58 L150 64 L136 66 Z"),
            headlight = path("M20 84 L64 75 L60 82 L26 90 Z"),
            taillight = path("M313 50 L325 47 L328 58 L311 58 Z"),
            crease = path("M40 93 L330 86"),
            sill = path("M105 111 L252 109 L252 118 L105 118 Z"),
        )
    }
    // Contour clair pour que les teintes sombres (noir) restent visibles sur fond noir
    val outline = Color.White.copy(alpha = 0.18f)

    Canvas(modifier) {
        val scale = min(size.width / VIEW_W, size.height / VIEW_H)
        val dx = (size.width - VIEW_W * scale) / 2f
        val dy = (size.height - VIEW_H * scale) / 2f
        withTransform({
            translate(dx, dy)
            scale(scale, scale, pivot = Offset.Zero)
        }) {
            // Ombre au sol
            drawOval(
                Color.Black.copy(alpha = 0.55f),
                topLeft = Offset(8f, 141f),
                size = androidx.compose.ui.geometry.Size(336f, 9f),
            )
            drawPath(paths.body, paint)
            drawPath(paths.body, outline, style = Stroke(width = 1.2f))
            drawPath(paths.windows, Color(0xFF1B2430))
            drawPath(paths.pillars, paint)
            drawPath(paths.mirror, paint)
            drawPath(paths.headlight, Color(0xFFE8F1FF))
            drawPath(paths.taillight, Color(0xFFD32F2F))
            drawPath(paths.crease, Color.Black.copy(alpha = 0.18f), style = Stroke(width = 1.2f))
            drawPath(paths.sill, Color.Black.copy(alpha = 0.25f))
            for (x in WHEELS_X) {
                drawCircle(Color(0xFF111111), radius = 27f, center = Offset(x, 118f))
                drawCircle(Color(0xFFC0C6CC), radius = 17f, center = Offset(x, 118f))
                drawCircle(Color(0xFF8A9096), radius = 17f, center = Offset(x, 118f), style = Stroke(width = 2f))
                drawCircle(Color(0xFF4A4F55), radius = 5f, center = Offset(x, 118f))
            }
        }
    }
}

private class CarPaths(
    val body: Path,
    val windows: Path,
    val pillars: Path,
    val mirror: Path,
    val headlight: Path,
    val taillight: Path,
    val crease: Path,
    val sill: Path,
)

private fun path(d: String): Path = PathParser().parsePathString(d).toPath()

private const val VIEW_W = 360f
private const val VIEW_H = 150f
private val WHEELS_X = floatArrayOf(72f, 285f)

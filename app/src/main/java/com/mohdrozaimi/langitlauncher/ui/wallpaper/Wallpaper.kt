package com.mohdrozaimi.langitlauncher.ui.wallpaper

import android.graphics.BitmapFactory
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.layout.ContentScale

/** Wallpaper: gambar sendiri jika ada, jika tidak gelombang biru-oren rekaan Langit. */
@Composable
fun Wallpaper(image: ImageBitmap?, dim: Float, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize()) {
        if (image != null) {
            Image(image, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
        } else {
            Canvas(Modifier.fillMaxSize()) { drawWaves() }
        }
        if (dim > 0f) Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = dim)))
    }
}

private fun DrawScope.drawWaves() {
    val w = size.width
    val h = size.height
    drawRect(Brush.verticalGradient(listOf(Color(0xFF2B98EE), Color(0xFF55BCF8), Color(0xFF86D0FB))))

    val cream = Path().apply {
        moveTo(w * 0.36f, h * 0.41f)
        cubicTo(w * 0.66f, h * 0.31f, w * 0.92f, h * 0.38f, w, h * 0.45f)
        lineTo(w, h * 0.82f)
        cubicTo(w * 0.84f, h * 0.76f, w * 0.72f, h * 0.62f, w * 0.57f, h * 0.57f)
        cubicTo(w * 0.47f, h * 0.53f, w * 0.41f, h * 0.47f, w * 0.36f, h * 0.41f)
        close()
    }
    drawPath(
        cream,
        Brush.linearGradient(
            listOf(Color(0xFFFCEBDA), Color(0xFFF1C9A9)),
            start = Offset(w * 0.4f, h * 0.35f),
            end = Offset(w, h * 0.8f),
        ),
    )

    val orange = Path().apply {
        moveTo(0f, h * 0.39f)
        cubicTo(w * 0.30f, h * 0.32f, w * 0.52f, h * 0.42f, w * 0.60f, h * 0.58f)
        cubicTo(w * 0.68f, h * 0.74f, w * 0.60f, h * 0.90f, w * 0.80f, h)
        lineTo(0f, h)
        close()
    }
    drawPath(
        orange,
        Brush.linearGradient(
            listOf(Color(0xFFFFA043), Color(0xFFFF5A1F)),
            start = Offset(0f, h * 0.4f),
            end = Offset(w * 0.7f, h),
        ),
    )

    val deep = Path().apply {
        moveTo(w * 0.64f, h)
        cubicTo(w * 0.72f, h * 0.90f, w * 0.86f, h * 0.87f, w, h * 0.85f)
        lineTo(w, h)
        close()
    }
    drawPath(deep, Color(0xFF3A8CF0))
}

/** Baca gambar wallpaper dengan saiz dikecilkan supaya tidak guna memori berlebihan. */
fun decodeWallpaper(path: String, maxWidth: Int, maxHeight: Int): ImageBitmap? = try {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeFile(path, bounds)
    var sample = 1
    while (bounds.outWidth / (sample * 2) >= maxWidth && bounds.outHeight / (sample * 2) >= maxHeight) sample *= 2
    BitmapFactory.decodeFile(path, BitmapFactory.Options().apply { inSampleSize = sample })?.asImageBitmap()
} catch (e: Exception) {
    null
}

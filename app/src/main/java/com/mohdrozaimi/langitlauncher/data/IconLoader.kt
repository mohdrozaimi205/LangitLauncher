package com.mohdrozaimi.langitlauncher.data

import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.Drawable
import android.util.LruCache
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap

/** Tukar ikon app jadi bitmap penuh (tanpa bingkai) supaya boleh dipotong bentuk squircle. */
class IconLoader(private val pm: PackageManager, private val sizePx: Int) {
    private val cache = LruCache<String, ImageBitmap>(300)

    fun peek(pkg: String): ImageBitmap? = cache.get(pkg)

    fun clear() = cache.evictAll()

    fun load(pkg: String): ImageBitmap? {
        cache.get(pkg)?.let { return it }
        val drawable = try {
            pm.getApplicationIcon(pkg)
        } catch (e: PackageManager.NameNotFoundException) {
            return null
        }
        val bitmap = render(drawable.mutate()).asImageBitmap()
        cache.put(pkg, bitmap)
        return bitmap
    }

    private fun render(d: Drawable): Bitmap {
        val size = sizePx
        val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        if (d is AdaptiveIconDrawable) {
            // Lapisan adaptif 108dp, kawasan nampak 72dp di tengah: besarkan 1.5x supaya penuh.
            val extra = size / 4
            d.background?.let {
                it.setBounds(-extra, -extra, size + extra, size + extra)
                it.draw(canvas)
            }
            d.foreground?.let {
                it.setBounds(-extra, -extra, size + extra, size + extra)
                it.draw(canvas)
            }
        } else {
            canvas.drawColor(Color.WHITE)
            val pad = size / 9
            d.setBounds(pad, pad, size - pad, size - pad)
            d.draw(canvas)
        }
        return bmp
    }
}

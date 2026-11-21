package com.mohdrozaimi.langitlauncher.domain

/** Gaya ikon seperti pilihan "Sesuaikan" iOS 18. */
enum class IconStyle { DEFAULT, DARK, TINTED }

/**
 * Matriks warna 4x5 untuk gaya ikon. TINTED: kelabukan dahulu (luminans) kemudian darab dengan warna tint.
 * DARK: gelapkan sedikit. DEFAULT: identiti.
 */
fun iconColorMatrix(style: IconStyle, tintArgb: Long): FloatArray {
    val r = ((tintArgb shr 16) and 0xFF) / 255f
    val g = ((tintArgb shr 8) and 0xFF) / 255f
    val b = (tintArgb and 0xFF) / 255f
    return when (style) {
        IconStyle.DEFAULT -> floatArrayOf(
            1f, 0f, 0f, 0f, 0f,
            0f, 1f, 0f, 0f, 0f,
            0f, 0f, 1f, 0f, 0f,
            0f, 0f, 0f, 1f, 0f,
        )
        IconStyle.DARK -> floatArrayOf(
            0.78f, 0f, 0f, 0f, 0f,
            0f, 0.78f, 0f, 0f, 0f,
            0f, 0f, 0.78f, 0f, 0f,
            0f, 0f, 0f, 1f, 0f,
        )
        IconStyle.TINTED -> floatArrayOf(
            0.299f * r, 0.587f * r, 0.114f * r, 0f, 0f,
            0.299f * g, 0.587f * g, 0.114f * g, 0f, 0f,
            0.299f * b, 0.587f * b, 0.114f * b, 0f, 0f,
            0f, 0f, 0f, 1f, 0f,
        )
    }
}

package com.mohdrozaimi.langitlauncher.ui.components

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import com.mohdrozaimi.langitlauncher.domain.superellipsePoint
import kotlin.math.PI

object SquircleShape : Shape {
    private const val STEPS = 96

    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val path = Path()
        val rx = size.width / 2f
        val ry = size.height / 2f
        for (i in 0 until STEPS) {
            val (x, y) = superellipsePoint(2 * PI * i / STEPS, 1f)
            val px = rx + x * rx
            val py = ry + y * ry
            if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
        }
        path.close()
        return Outline.Generic(path)
    }
}

package com.mohdrozaimi.langitlauncher.domain

import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sign
import kotlin.math.sin

/**
 * Titik pada superellipse |x|^n + |y|^n = r^n untuk sudut parameter [t].
 * n = 5 memberi bentuk "squircle" bucu bulat lembut.
 */
fun superellipsePoint(t: Double, radius: Float, n: Double = 5.0): Pair<Float, Float> {
    val c = cos(t)
    val s = sin(t)
    val x = radius * sign(c) * abs(c).pow(2.0 / n)
    val y = radius * sign(s) * abs(s).pow(2.0 / n)
    return x.toFloat() to y.toFloat()
}

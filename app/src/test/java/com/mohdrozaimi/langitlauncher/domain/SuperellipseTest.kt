package com.mohdrozaimi.langitlauncher.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.PI
import kotlin.math.cos

class SuperellipseTest {
    @Test
    fun startPointIsOnRightEdge() {
        val (x, y) = superellipsePoint(0.0, 30f)
        assertEquals(30f, x, 0.01f)
        assertEquals(0f, y, 0.01f)
    }

    @Test
    fun diagonalIsFullerThanCircle() {
        val (x, y) = superellipsePoint(PI / 4, 30f)
        assertEquals(x, y, 0.001f)
        assertTrue(x > 30f * cos(PI / 4).toFloat())
        assertTrue(x < 30f)
    }
}

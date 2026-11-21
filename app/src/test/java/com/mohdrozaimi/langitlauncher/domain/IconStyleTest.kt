package com.mohdrozaimi.langitlauncher.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class IconStyleTest {
    @Test
    fun tintedMatrixScalesLuminanceByTint() {
        val m = iconColorMatrix(IconStyle.TINTED, 0xFFFF0000)
        assertEquals(0.299f, m[0], 0.001f)
        assertEquals(0f, m[5], 0.001f)
        assertEquals(20, m.size)
    }
}

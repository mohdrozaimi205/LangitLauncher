package com.mohdrozaimi.langitlauncher.domain

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalTime

class ClockFormatTest {
    @Test
    fun formats() {
        assertEquals("12:02", formatClock(LocalTime.of(12, 2), false))
        assertEquals("12:05", formatClock(LocalTime.of(0, 5), false))
        assertEquals("13:07", formatClock(LocalTime.of(13, 7), true))
        assertEquals("1:07", formatClock(LocalTime.of(13, 7), false))
    }
}

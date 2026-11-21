package com.mohdrozaimi.langitlauncher.domain

import java.time.LocalTime
import java.util.Locale

fun formatClock(time: LocalTime, is24h: Boolean): String {
    if (is24h) return String.format(Locale.US, "%02d:%02d", time.hour, time.minute)
    val h = time.hour % 12
    return String.format(Locale.US, "%d:%02d", if (h == 0) 12 else h, time.minute)
}

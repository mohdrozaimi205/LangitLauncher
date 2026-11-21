package com.mohdrozaimi.langitlauncher.domain

import android.content.pm.ApplicationInfo
import com.mohdrozaimi.langitlauncher.model.AppEntry
import org.junit.Assert.assertEquals
import org.junit.Test

class LibraryGrouperTest {
    @Test
    fun groupsBiggestFirstOtherLast() {
        val apps = listOf(
            AppEntry("x", "Xyz", -1),
            AppEntry("i", "Instagram", ApplicationInfo.CATEGORY_SOCIAL),
            AppEntry("w", "WhatsApp", ApplicationInfo.CATEGORY_SOCIAL),
            AppEntry("g", "Game", ApplicationInfo.CATEGORY_GAME),
        )
        val r = LibraryGrouper.group(apps)
        assertEquals(listOf("Sosial", "Permainan", "Lain-lain"), r.map { it.first })
        assertEquals(listOf("Instagram", "WhatsApp"), r[0].second.map { it.label })
    }
}

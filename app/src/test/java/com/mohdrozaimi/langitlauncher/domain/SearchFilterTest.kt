package com.mohdrozaimi.langitlauncher.domain

import com.mohdrozaimi.langitlauncher.model.AppEntry
import org.junit.Assert.assertEquals
import org.junit.Test

class SearchFilterTest {
    private val apps = listOf("YouTube Music", "YouTube", "Netflix", "Café Rio", "Tube Map").map { AppEntry(it, it) }

    @Test
    fun caseInsensitivePrefix() {
        assertEquals(listOf("YouTube", "YouTube Music"), SearchFilter.filter(apps, "YOU").map { it.label })
    }

    @Test
    fun accentInsensitive() {
        assertEquals(listOf("Café Rio"), SearchFilter.filter(apps, "cafe").map { it.label })
    }

    @Test
    fun prefixBeforeContains() {
        assertEquals(listOf("Tube Map", "YouTube", "YouTube Music"), SearchFilter.filter(apps, "tube").map { it.label })
    }

    @Test
    fun emptyQueryGivesNothing() {
        assertEquals(emptyList<AppEntry>(), SearchFilter.filter(apps, "  "))
    }
}

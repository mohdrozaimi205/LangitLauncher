package com.mohdrozaimi.langitlauncher.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class BadgeTest {
    @Test
    fun countSkipsSummaryAndOngoingAndUsesNumber() {
        val r = BadgeCounter.count(
            listOf(
                NotifInfo("wa", isGroupSummary = true, isOngoing = false, number = 0),
                NotifInfo("wa", isGroupSummary = false, isOngoing = false, number = 3),
                NotifInfo("wa", isGroupSummary = false, isOngoing = false, number = 0),
                NotifInfo("music", isGroupSummary = false, isOngoing = true, number = 0),
                NotifInfo("yt", isGroupSummary = false, isOngoing = false, number = 0),
            ),
        )
        assertEquals(mapOf("wa" to 4, "yt" to 1), r)
    }

    @Test
    fun formatBadgeValues() {
        assertNull(formatBadge(0))
        assertEquals("5", formatBadge(5))
        assertEquals("1,299", formatBadge(1299))
    }
}

package com.mohdrozaimi.langitlauncher.domain

import java.util.Locale

data class NotifInfo(
    val pkg: String,
    val isGroupSummary: Boolean,
    val isOngoing: Boolean,
    val number: Int,
)

object BadgeCounter {
    /** Kira badge setiap pakej. Ringkasan kumpulan & notifikasi berterusan tidak dikira. */
    fun count(items: List<NotifInfo>): Map<String, Int> =
        items.filter { !it.isGroupSummary && !it.isOngoing }
            .groupBy { it.pkg }
            .mapValues { (_, list) -> list.sumOf { if (it.number > 0) it.number else 1 } }
}

/** 0 -> null (tiada badge), 1299 -> "1,299". */
fun formatBadge(n: Int): String? = if (n <= 0) null else String.format(Locale.US, "%,d", n)

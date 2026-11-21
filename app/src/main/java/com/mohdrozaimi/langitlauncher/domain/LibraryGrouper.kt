package com.mohdrozaimi.langitlauncher.domain

import android.content.pm.ApplicationInfo
import com.mohdrozaimi.langitlauncher.model.AppEntry

object LibraryGrouper {
    const val OTHER = "Lain-lain"

    fun categoryName(category: Int): String = when (category) {
        ApplicationInfo.CATEGORY_SOCIAL -> "Sosial"
        ApplicationInfo.CATEGORY_GAME -> "Permainan"
        ApplicationInfo.CATEGORY_AUDIO -> "Muzik & Audio"
        ApplicationInfo.CATEGORY_VIDEO -> "Hiburan"
        ApplicationInfo.CATEGORY_IMAGE -> "Foto & Video"
        ApplicationInfo.CATEGORY_NEWS -> "Berita"
        ApplicationInfo.CATEGORY_MAPS -> "Navigasi"
        ApplicationInfo.CATEGORY_PRODUCTIVITY -> "Produktiviti"
        else -> OTHER
    }

    /** Kumpulan terbesar dahulu, "Lain-lain" sentiasa di hujung. */
    fun group(apps: List<AppEntry>): List<Pair<String, List<AppEntry>>> =
        apps.groupBy { categoryName(it.category) }
            .map { (name, list) -> name to list.sortedBy { it.label.lowercase() } }
            .sortedWith(compareBy({ it.first == OTHER }, { -it.second.size }, { it.first }))
}

package com.mohdrozaimi.langitlauncher.domain

import com.mohdrozaimi.langitlauncher.model.AppEntry
import java.text.Normalizer
import java.util.Locale

object SearchFilter {
    private val marks = Regex("\\p{Mn}+")

    fun normalize(s: String): String =
        Normalizer.normalize(s, Normalizer.Form.NFD).replace(marks, "").lowercase(Locale.ROOT)

    /** Padanan awalan dahulu, kemudian awalan perkataan, kemudian di tengah. */
    fun filter(apps: List<AppEntry>, query: String): List<AppEntry> {
        val q = normalize(query.trim())
        if (q.isEmpty()) return emptyList()
        return apps.mapNotNull { app ->
            val label = normalize(app.label)
            val rank = when {
                label.startsWith(q) -> 0
                label.split(' ').any { it.startsWith(q) } -> 1
                label.contains(q) -> 2
                else -> null
            }
            rank?.let { Triple(it, label, app) }
        }.sortedWith(compareBy({ it.first }, { it.second })).map { it.third }
    }
}

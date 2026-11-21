package com.mohdrozaimi.langitlauncher.model

/** Satu app yang boleh dibuka dari launcher. [category] ikut ApplicationInfo.category (-1 = tiada). */
data class AppEntry(
    val packageName: String,
    val label: String,
    val category: Int = -1,
)

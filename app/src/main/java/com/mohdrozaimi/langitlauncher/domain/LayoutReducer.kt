package com.mohdrozaimi.langitlauncher.domain

import kotlinx.serialization.Serializable

/** Susunan skrin utama: halaman ikon, dok, dan app yang disorok (hanya dalam Pustaka Apl). */
@Serializable
data class HomeLayout(
    val pages: List<List<String>> = emptyList(),
    val dock: List<String> = emptyList(),
    val hidden: List<String> = emptyList(),
    val initialized: Boolean = false,
)

/** Kedudukan satu ikon. [page] = [DOCK] untuk dok. */
data class Slot(val page: Int, val index: Int) {
    companion object {
        const val DOCK = -1
    }
}

object LayoutReducer {
    const val DOCK_SIZE = 4
    const val FIRST_PAGE_SIZE = 16
    const val PAGE_SIZE = 24

    fun capacity(page: Int, firstPageSize: Int = FIRST_PAGE_SIZE, pageSize: Int = PAGE_SIZE): Int =
        if (page == 0) firstPageSize else pageSize

    /** Selaraskan susunan dengan senarai app yang dipasang sekarang. */
    fun sync(
        layout: HomeLayout,
        installed: List<String>,
        defaultDock: List<String>,
        firstPageSize: Int = FIRST_PAGE_SIZE,
        pageSize: Int = PAGE_SIZE,
    ): HomeLayout {
        val installedSet = installed.toSet()
        val dock = if (!layout.initialized) {
            defaultDock.filter { it in installedSet }.distinct().take(DOCK_SIZE)
        } else {
            layout.dock.filter { it in installedSet }
        }
        val hidden = layout.hidden.filter { it in installedSet }
        val pages = layout.pages.map { page -> page.filter { it in installedSet && it !in dock } }
        val placed = (dock + hidden + pages.flatten()).toSet()
        val result = pages.filter { it.isNotEmpty() }.map { it.toMutableList() }.toMutableList()
        for (pkg in installed) {
            if (pkg in placed) continue
            if (result.isEmpty() || result.last().size >= capacity(result.lastIndex, firstPageSize, pageSize)) {
                result.add(mutableListOf())
            }
            result.last().add(pkg)
        }
        return HomeLayout(normalize(result, firstPageSize, pageSize), dock, hidden, initialized = true)
    }

    /** Alih satu ikon dari [from] ke [to]. Dok penuh -> tiada perubahan. */
    fun move(
        layout: HomeLayout,
        from: Slot,
        to: Slot,
        firstPageSize: Int = FIRST_PAGE_SIZE,
        pageSize: Int = PAGE_SIZE,
    ): HomeLayout {
        val pkg = get(layout, from) ?: return layout
        if (to.page == Slot.DOCK && from.page != Slot.DOCK && layout.dock.size >= DOCK_SIZE) return layout
        if (to.page != Slot.DOCK && (to.page < 0 || to.page > layout.pages.size)) return layout

        val dock = layout.dock.toMutableList()
        val pages = layout.pages.map { it.toMutableList() }.toMutableList()
        if (from.page == Slot.DOCK) dock.removeAt(from.index) else pages[from.page].removeAt(from.index)

        if (to.page == Slot.DOCK) {
            dock.add(to.index.coerceIn(0, dock.size), pkg)
        } else {
            if (to.page == pages.size) pages.add(mutableListOf())
            val target = pages[to.page]
            target.add(to.index.coerceIn(0, target.size), pkg)
        }
        return layout.copy(pages = normalize(pages, firstPageSize, pageSize), dock = dock)
    }

    /** Buang dari skrin utama (masih ada dalam Pustaka Apl). */
    fun hide(layout: HomeLayout, pkg: String): HomeLayout = layout.copy(
        pages = normalize(layout.pages.map { p -> p.filter { it != pkg } }),
        dock = layout.dock.filter { it != pkg },
        hidden = (layout.hidden + pkg).distinct(),
    )

    /** Kembalikan app tersorok ke hujung halaman terakhir. */
    fun unhide(layout: HomeLayout, pkg: String): HomeLayout {
        if (pkg !in layout.hidden) return layout
        val pages = layout.pages.map { it.toMutableList() }.toMutableList()
        if (pages.isEmpty() || pages.last().size >= capacity(pages.lastIndex)) pages.add(mutableListOf())
        pages.last().add(pkg)
        return layout.copy(pages = normalize(pages), hidden = layout.hidden - pkg)
    }

    private fun get(layout: HomeLayout, slot: Slot): String? =
        if (slot.page == Slot.DOCK) layout.dock.getOrNull(slot.index)
        else layout.pages.getOrNull(slot.page)?.getOrNull(slot.index)

    /** Pastikan setiap halaman tidak melebihi kapasiti dan buang halaman kosong. */
    fun normalize(
        pages: List<List<String>>,
        firstPageSize: Int = FIRST_PAGE_SIZE,
        pageSize: Int = PAGE_SIZE,
    ): List<List<String>> {
        val work = pages.filter { it.isNotEmpty() }.map { it.toMutableList() }.toMutableList()
        var i = 0
        while (i < work.size) {
            val cap = capacity(i, firstPageSize, pageSize)
            if (work[i].size > cap) {
                val overflow = work[i].subList(cap, work[i].size).toList()
                repeat(overflow.size) { work[i].removeAt(work[i].lastIndex) }
                if (i + 1 == work.size) work.add(mutableListOf())
                work[i + 1].addAll(0, overflow)
            }
            i++
        }
        return work.filter { it.isNotEmpty() }
    }
}

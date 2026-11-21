package com.mohdrozaimi.langitlauncher.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

class LayoutReducerTest {
    private fun apps(n: Int) = (1..n).map { "app%02d".format(it) }

    @Test
    fun firstSyncFillsDockFromDefaultsAndRestToPage() {
        val r = LayoutReducer.sync(HomeLayout(), listOf("a", "b", "c", "d", "e"), listOf("a", "b", "zzz"))
        assertEquals(listOf("a", "b"), r.dock)
        assertEquals(listOf(listOf("c", "d", "e")), r.pages)
    }

    @Test
    fun overflowGoesToSecondPage() {
        val r = LayoutReducer.sync(HomeLayout(), apps(17), emptyList())
        assertEquals(16, r.pages[0].size)
        assertEquals(listOf("app17"), r.pages[1])
    }

    @Test
    fun newAppAppendsToLastPage() {
        val start = HomeLayout(pages = listOf(listOf("a", "b")), dock = listOf("d"), initialized = true)
        val r = LayoutReducer.sync(start, listOf("a", "b", "d", "new"), emptyList())
        assertEquals(listOf(listOf("a", "b", "new")), r.pages)
        assertEquals(listOf("d"), r.dock)
    }

    @Test
    fun uninstalledAppRemovedFromPagesAndDockAndEmptyPageDropped() {
        val start = HomeLayout(pages = listOf(listOf("a"), listOf("x")), dock = listOf("y", "a2"), initialized = true)
        val r = LayoutReducer.sync(start, listOf("a", "a2"), emptyList())
        assertEquals(listOf(listOf("a")), r.pages)
        assertEquals(listOf("a2"), r.dock)
    }

    @Test
    fun hiddenAppIsNotReAdded() {
        val start = HomeLayout(pages = listOf(listOf("a")), hidden = listOf("h"), initialized = true)
        val r = LayoutReducer.sync(start, listOf("a", "h"), emptyList())
        assertEquals(listOf(listOf("a")), r.pages)
        assertEquals(listOf("h"), r.hidden)
    }

    @Test
    fun moveReordersWithinPage() {
        val start = HomeLayout(pages = listOf(listOf("a", "b", "c")), initialized = true)
        val r = LayoutReducer.move(start, Slot(0, 0), Slot(0, 2))
        assertEquals(listOf("b", "c", "a"), r.pages[0])
    }

    @Test
    fun moveToFullDockIsRefused() {
        val start = HomeLayout(pages = listOf(listOf("a")), dock = listOf("1", "2", "3", "4"), initialized = true)
        val r = LayoutReducer.move(start, Slot(0, 0), Slot(Slot.DOCK, 1))
        assertSame(start, r)
    }

    @Test
    fun moveFromDockToPageAndToNewPage() {
        val start = HomeLayout(pages = listOf(listOf("a")), dock = listOf("d"), initialized = true)
        val r = LayoutReducer.move(start, Slot(Slot.DOCK, 0), Slot(1, 0))
        assertEquals(listOf(listOf("a"), listOf("d")), r.pages)
        assertEquals(emptyList<String>(), r.dock)
    }

    @Test
    fun movingIntoFullFirstPagePushesOverflow() {
        val start = HomeLayout(pages = listOf(apps(16), listOf("x")), initialized = true)
        val r = LayoutReducer.move(start, Slot(1, 0), Slot(0, 0))
        assertEquals("x", r.pages[0][0])
        assertEquals(16, r.pages[0].size)
        assertEquals(listOf("app16"), r.pages[1])
    }

    @Test
    fun hideAndUnhide() {
        val start = HomeLayout(pages = listOf(listOf("a", "b")), dock = listOf("c"), initialized = true)
        val hidden = LayoutReducer.hide(start, "a")
        assertEquals(listOf(listOf("b")), hidden.pages)
        assertEquals(listOf("a"), hidden.hidden)
        val back = LayoutReducer.unhide(hidden, "a")
        assertEquals(listOf(listOf("b", "a")), back.pages)
        assertEquals(emptyList<String>(), back.hidden)
    }
}

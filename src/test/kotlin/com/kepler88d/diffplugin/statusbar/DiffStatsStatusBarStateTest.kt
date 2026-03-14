package com.kepler88d.diffplugin.statusbar

import com.kepler88d.diffplugin.diff.DiffStats
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DiffStatsStatusBarStateTest {
    @Test
    fun prioritizesSelectionBeforeEditorAndCommitTotals() {
        val state = DiffStatsStatusBarState(
            commitTotal = DiffStats(10, 2),
            selectionTotal = DiffStats(4, 1),
            editorFileTotal = DiffStats(7, 3),
            showSelection = true,
            showEditorFile = true
        )

        assertEquals(DiffStats(4, 1), state.displayedStats)
        assertEquals("status.bar.diff.stats.tooltip.selection", state.tooltipKey)
    }

    @Test
    fun fallsBackToFocusedEditorFileBeforeCommitTotal() {
        val state = DiffStatsStatusBarState(
            commitTotal = DiffStats(10, 2),
            selectionTotal = null,
            editorFileTotal = DiffStats(7, 3),
            showSelection = false,
            showEditorFile = true
        )

        assertEquals(DiffStats(7, 3), state.displayedStats)
        assertEquals("status.bar.diff.stats.tooltip.file", state.tooltipKey)
    }

    @Test
    fun hidesWhenThereIsNoAvailableStats() {
        val state = DiffStatsStatusBarState.EMPTY

        assertNull(state.displayedStats)
        assertNull(state.tooltipKey)
    }
}

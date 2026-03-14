package com.kepler88d.diffplugin.statusbar

import com.kepler88d.diffplugin.diff.DiffStats

internal data class DiffStatsStatusBarState(
    val commitTotal: DiffStats,
    val selectionTotal: DiffStats?,
    val editorFileTotal: DiffStats?,
    val showSelection: Boolean,
    val showEditorFile: Boolean
) {
    companion object {
        val EMPTY = DiffStatsStatusBarState(DiffStats.EMPTY, null, null, false, false)
    }

    val displayedStats: DiffStats?
        get() = when {
            showSelection && selectionTotal?.hasValue == true -> selectionTotal
            showEditorFile && editorFileTotal?.hasValue == true -> editorFileTotal
            commitTotal.hasValue -> commitTotal
            else -> null
        }

    val tooltipKey: String?
        get() = when {
            showSelection && selectionTotal?.hasValue == true -> "status.bar.diff.stats.tooltip.selection"
            showEditorFile && editorFileTotal?.hasValue == true -> "status.bar.diff.stats.tooltip.file"
            commitTotal.hasValue -> "status.bar.diff.stats.tooltip.commit"
            else -> null
        }
}

package com.kepler88d.diffplugin.diff

internal data class DiffStatsSnapshot(
    val statsByPath: Map<String, DiffStats>,
    val commitTotal: DiffStats,
    val visibleTotal: DiffStats
) {
    companion object {
        val EMPTY = DiffStatsSnapshot(emptyMap(), DiffStats.EMPTY, DiffStats.EMPTY)
    }
}

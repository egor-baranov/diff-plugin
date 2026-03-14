package com.kepler88d.diffplugin.diff

internal data class DiffStats(
    val added: Int,
    val removed: Int
) {
    companion object {
        val EMPTY = DiffStats(0, 0)
    }

    val hasValue: Boolean
        get() = added != 0 || removed != 0

    operator fun plus(other: DiffStats) = DiffStats(added + other.added, removed + other.removed)
}

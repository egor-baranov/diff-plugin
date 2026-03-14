package com.kepler88d.diffplugin.git

import com.kepler88d.diffplugin.diff.DiffStats

internal data class GitNumstatEntry(
    val path: String,
    val stats: DiffStats
)

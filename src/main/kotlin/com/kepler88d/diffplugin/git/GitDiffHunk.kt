package com.kepler88d.diffplugin.git

internal data class GitDiffHunk(
    val addedStartLine: Int,
    val addedLineCount: Int,
    val removedLineCount: Int
) {
    var removedLines: List<String> = emptyList()
}

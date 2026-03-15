package com.kepler88d.diffplugin.git

internal object GitDiffHunkParser {
    private val hunkHeaderPattern = Regex("^@@ -(\\d+)(?:,(\\d+))? \\+(\\d+)(?:,(\\d+))? @@.*$")

    fun parse(diffText: String): List<GitDiffHunk> {
        val hunks = mutableListOf<GitDiffHunk>()
        var currentHunk: GitDiffHunk? = null
        diffText.lineSequence().forEach { line ->
            val parsedHeader = parseHeader(line)
            if (parsedHeader != null) {
                currentHunk = parsedHeader.takeIf { it.addedLineCount > 0 || it.removedLineCount > 0 }
                currentHunk?.let { hunks += it }
                return@forEach
            }
            if (currentHunk == null || !line.startsWith("-") || line.startsWith("---")) return@forEach
            currentHunk = currentHunk.also { it.removedLines += line.removePrefix("-") }
        }
        return hunks
    }

    private fun parseHeader(line: String): GitDiffHunk? {
        val match = hunkHeaderPattern.matchEntire(line) ?: return null
        val newStart = match.groupValues[3].toInt()
        val newCount = match.groupValues[4].toIntOrNull() ?: 1
        val oldCount = match.groupValues[2].toIntOrNull() ?: 1
        return GitDiffHunk(
            addedStartLine = (newStart - 1).coerceAtLeast(0),
            addedLineCount = newCount,
            removedLineCount = oldCount
        )
    }
}

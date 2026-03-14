package com.kepler88d.diffplugin.projectview

import com.kepler88d.diffplugin.diff.DiffStats
import com.intellij.openapi.util.io.FileUtil

internal object DiffStatsProjectPaths {
    fun sumUnderRoots(rootPaths: Collection<String>, statsByPath: Map<String, DiffStats>): DiffStats {
        val normalizedRoots = rootPaths.map { normalize(it) }.distinct()
        return statsByPath.entries.fold(DiffStats.EMPTY) { total, (path, stats) ->
            if (normalizedRoots.any { contains(it, path) }) total + stats else total
        }
    }

    private fun contains(rootPath: String, path: String): Boolean {
        val normalizedPath = normalize(path)
        return normalizedPath == rootPath || normalizedPath.startsWith("$rootPath/")
    }

    private fun normalize(path: String) = FileUtil.toSystemIndependentName(path).trimEnd('/')
}

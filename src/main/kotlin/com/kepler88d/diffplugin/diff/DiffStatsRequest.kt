package com.kepler88d.diffplugin.diff

import com.intellij.openapi.vcs.FilePath
import com.intellij.openapi.vcs.changes.Change
import com.intellij.openapi.vfs.VirtualFile

internal data class DiffStatsRequest(
    val key: String,
    val primaryPath: FilePath,
    val beforePath: FilePath?,
    val afterPath: FilePath?,
    val change: Change?,
    val virtualFile: VirtualFile?
)

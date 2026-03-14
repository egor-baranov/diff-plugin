package com.kepler88d.diffplugin.diff

import com.intellij.openapi.vcs.FilePath
import com.intellij.openapi.vcs.changes.Change
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.vcsUtil.VcsUtil

internal object DiffStatsRequestFactory {
    fun fromChange(change: Change?): DiffStatsRequest? {
        val beforePath = change?.beforeRevision?.file
        val afterPath = change?.afterRevision?.file
        val primaryPath = afterPath ?: beforePath ?: return null
        return DiffStatsRequest(
            DiffStatsPathKey.fromFilePath(primaryPath),
            primaryPath,
            beforePath,
            afterPath,
            change,
            change?.virtualFile
        )
    }

    fun fromFilePath(filePath: FilePath?): DiffStatsRequest? {
        if (filePath == null || filePath.isDirectory) return null
        return DiffStatsRequest(
            DiffStatsPathKey.fromFilePath(filePath),
            filePath,
            null,
            filePath,
            null,
            filePath.virtualFile
        )
    }

    fun fromVirtualFile(virtualFile: VirtualFile?): DiffStatsRequest? {
        val file = virtualFile?.takeUnless { it.isDirectory } ?: return null
        val filePath = VcsUtil.getFilePath(file)
        return DiffStatsRequest(
            DiffStatsPathKey.fromFilePath(filePath),
            filePath,
            null,
            filePath,
            null,
            file
        )
    }
}

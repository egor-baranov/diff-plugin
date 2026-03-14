package com.kepler88d.diffplugin.git

import com.kepler88d.diffplugin.diff.DiffStats
import com.kepler88d.diffplugin.diff.DiffStatsRequest
import com.kepler88d.diffplugin.diff.DiffStatsRequestFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.changes.ChangeListManager
import com.intellij.openapi.vfs.VirtualFile

internal object EditorFileDiffStatsLoader {
    suspend fun load(project: Project, virtualFile: VirtualFile?): DiffStats? {
        val request = createRequest(project, virtualFile) ?: return null
        return GitDiffStatsReader.load(project, listOf(request))[request.key]
    }

    private fun createRequest(project: Project, virtualFile: VirtualFile?): DiffStatsRequest? {
        val file = virtualFile?.takeUnless { it.isDirectory || it.fileType.isBinary } ?: return null
        val changeListManager = ChangeListManager.getInstance(project)
        val change = changeListManager.getChange(file)
        if (change != null) return DiffStatsRequestFactory.fromChange(change)
        if (!changeListManager.isUnversioned(file)) return null
        return DiffStatsRequestFactory.fromVirtualFile(file)
    }
}

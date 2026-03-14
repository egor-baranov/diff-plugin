package com.kepler88d.diffplugin.git

import com.intellij.diff.comparison.ComparisonManager
import com.intellij.diff.comparison.ComparisonPolicy
import com.intellij.diff.comparison.DiffTooBigException
import com.intellij.openapi.editor.Document
import com.intellij.openapi.progress.EmptyProgressIndicator
import com.intellij.openapi.project.Project
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.vcs.VcsException
import com.intellij.openapi.vcs.changes.ChangeListManager
import com.intellij.openapi.vfs.VirtualFile
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers

internal object EditorFileDiffHunksLoader {
    suspend fun load(project: Project, virtualFile: VirtualFile?, document: Document): List<GitDiffHunk> {
        val file = virtualFile?.takeUnless { it.isDirectory || it.fileType.isBinary } ?: return emptyList()
        val changeListManager = ChangeListManager.getInstance(project)
        if (changeListManager.isUnversioned(file)) return unversionedFileHunks(document)
        val change = changeListManager.getChange(file) ?: return emptyList()
        return withContext(Dispatchers.IO) {
            try {
                val beforeContent = change.beforeRevision?.content ?: return@withContext unversionedFileHunks(document)
                ComparisonManager.getInstance()
                    .compareLines(beforeContent, document.text, ComparisonPolicy.DEFAULT, EmptyProgressIndicator())
                    .map { fragment ->
                        GitDiffHunk(
                            fragment.startLine2,
                            fragment.endLine2 - fragment.startLine2,
                            fragment.endLine1 - fragment.startLine1
                        )
                    }
                    .filter { it.addedLineCount > 0 || it.removedLineCount > 0 }
            } catch (exception: ProcessCanceledException) {
                throw exception
            } catch (_: DiffTooBigException) {
                emptyList()
            } catch (_: VcsException) {
                emptyList()
            }
        }
    }

    private fun unversionedFileHunks(document: Document): List<GitDiffHunk> {
        val lineCount = if (document.textLength == 0) 0 else document.lineCount
        return if (lineCount == 0) emptyList() else listOf(GitDiffHunk(0, lineCount, 0))
    }
}

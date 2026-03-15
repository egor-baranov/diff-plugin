package com.kepler88d.diffplugin.git

import com.kepler88d.diffplugin.diff.DiffStatsRequestFactory
import com.kepler88d.diffplugin.editor.EditorDiffDocumentSnapshot
import com.intellij.diff.comparison.ComparisonManager
import com.intellij.diff.comparison.ComparisonPolicy
import com.intellij.diff.comparison.DiffTooBigException
import com.intellij.openapi.progress.EmptyProgressIndicator
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.text.StringUtil
import com.intellij.openapi.vcs.VcsException
import com.intellij.openapi.vcs.changes.ChangeListManager
import com.intellij.openapi.vfs.VirtualFile
import git4idea.GitUtil
import git4idea.commands.GitBinaryHandler
import git4idea.commands.GitCommand
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal object EditorFileDiffHunksLoader {
    suspend fun load(project: Project, virtualFile: VirtualFile?, snapshot: EditorDiffDocumentSnapshot): List<GitDiffHunk> {
        val file = virtualFile?.takeUnless { it.isDirectory || it.fileType.isBinary } ?: return emptyList()
        val changeListManager = ChangeListManager.getInstance(project)
        if (changeListManager.isUnversioned(file)) return unversionedFileHunks(snapshot)
        val change = changeListManager.getChange(file) ?: return emptyList()
        if (!snapshot.isUnsaved) {
            loadSavedGitHunks(project, change)?.let { return it }
        }
        return withContext(Dispatchers.IO) {
            try {
                val beforeContent = change.beforeRevision?.content ?: return@withContext unversionedFileHunks(snapshot)
                val beforeLines = StringUtil.splitByLinesDontTrim(beforeContent)
                ComparisonManager.getInstance()
                    .compareLines(beforeContent, snapshot.text, ComparisonPolicy.DEFAULT, EmptyProgressIndicator())
                    .map { fragment ->
                        GitDiffHunk(
                            fragment.startLine2,
                            fragment.endLine2 - fragment.startLine2,
                            fragment.endLine1 - fragment.startLine1
                        ).also {
                            it.removedLines = removedLines(beforeLines, fragment.startLine1, fragment.endLine1)
                        }
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

    private suspend fun loadSavedGitHunks(project: Project, change: com.intellij.openapi.vcs.changes.Change) = withContext(Dispatchers.IO) {
        val request = DiffStatsRequestFactory.fromChange(change) ?: return@withContext null
        val root = GitUtil.sortFilePathsByGitRootIgnoringMissing(project, listOf(request.primaryPath)).keys.firstOrNull()
            ?: return@withContext null
        try {
            GitBinaryHandler(project, root, GitCommand.DIFF).apply {
                setSilent(true)
                withNoTty()
                addParameters("--unified=0", "--find-renames", "--no-color", "HEAD")
                endOptions()
                addRelativePaths(listOf(request.primaryPath))
            }.run().decodeToString().let { GitDiffHunkParser.parse(it) }
        } catch (exception: ProcessCanceledException) {
            throw exception
        } catch (_: VcsException) {
            null
        }
    }

    private fun unversionedFileHunks(snapshot: EditorDiffDocumentSnapshot): List<GitDiffHunk> {
        return if (snapshot.lineCount == 0) emptyList() else listOf(GitDiffHunk(0, snapshot.lineCount, 0))
    }

    private fun removedLines(lines: Array<String>, startLine: Int, endLine: Int): List<String> {
        if (startLine >= endLine) return emptyList()
        val safeStart = startLine.coerceAtLeast(0).coerceAtMost(lines.size)
        val safeEnd = endLine.coerceAtLeast(safeStart).coerceAtMost(lines.size)
        return lines.slice(safeStart until safeEnd)
    }
}

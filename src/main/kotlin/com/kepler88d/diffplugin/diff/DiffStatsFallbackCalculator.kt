package com.kepler88d.diffplugin.diff

import com.intellij.diff.comparison.ComparisonManager
import com.intellij.diff.comparison.DiffTooBigException
import com.intellij.diff.comparison.ComparisonPolicy
import com.intellij.diff.fragments.LineFragment
import com.intellij.openapi.progress.EmptyProgressIndicator
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.vcs.changes.Change
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile

internal object DiffStatsFallbackCalculator {
    fun calculate(request: DiffStatsRequest): DiffStats? {
        val change = request.change ?: return countFileLines(request.virtualFile, request.primaryPath.path)
        return calculateFromChange(change) ?: countFileLines(request.virtualFile, request.primaryPath.path)
    }

    private fun calculateFromChange(change: Change): DiffStats? {
        val beforeContent = loadContent(change.beforeRevision?.content)
        val afterContent = loadContent(change.afterRevision?.content)
        return try {
            val fragments = ComparisonManager.getInstance()
                .compareLines(beforeContent, afterContent, ComparisonPolicy.DEFAULT, EmptyProgressIndicator())
            fragments.fold(DiffStats.EMPTY) { total, fragment -> total + fragment.toDiffStats() }
        } catch (exception: ProcessCanceledException) {
            throw exception
        } catch (pce: ProcessCanceledException) {
            throw pce
        } catch (e: DiffTooBigException) {
            null
        }
    }

    private fun countFileLines(virtualFile: VirtualFile?, path: String): DiffStats? {
        val file = virtualFile ?: LocalFileSystem.getInstance().findFileByPath(path) ?: return null
        if (file.fileType.isBinary || file.isDirectory) return null
        return runCatching { DiffStats(readLineCount(file), 0) }.onFailure {
            if (it is ProcessCanceledException) throw it
        }.getOrNull()
    }

    private fun loadContent(content: String?) = content ?: ""

    private fun readLineCount(file: VirtualFile): Int {
        file.inputStream.use { inputStream ->
            var lineCount = 0
            var previousByte = -1
            while (true) {
                val currentByte = inputStream.read()
                if (currentByte < 0) {
                    return when {
                        previousByte < 0 -> 0
                        previousByte == '\n'.code -> lineCount
                        else -> lineCount + 1
                    }
                }
                if (currentByte == '\n'.code) lineCount++
                previousByte = currentByte
            }
        }
    }

    private fun LineFragment.toDiffStats() = DiffStats(endLine2 - startLine2, endLine1 - startLine1)
}

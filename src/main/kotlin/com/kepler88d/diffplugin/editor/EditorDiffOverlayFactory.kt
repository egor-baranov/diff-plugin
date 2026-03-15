package com.kepler88d.diffplugin.editor

import com.kepler88d.diffplugin.diff.DiffStatsColors
import com.kepler88d.diffplugin.git.GitDiffHunk
import com.kepler88d.diffplugin.settings.DiffStatsSettingsState
import com.intellij.openapi.editor.ComponentInlayAlignment
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.Inlay
import com.intellij.openapi.editor.InlayProperties
import com.intellij.openapi.editor.addComponentInlay
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.editor.markup.HighlighterLayer
import com.intellij.openapi.editor.markup.HighlighterTargetArea
import com.intellij.openapi.editor.markup.RangeHighlighter
import com.intellij.openapi.editor.markup.TextAttributes
import com.intellij.openapi.vfs.VirtualFile
import java.awt.Color

internal object EditorDiffOverlayFactory {
    fun create(editor: Editor, file: VirtualFile, hunks: List<GitDiffHunk>, settings: DiffStatsSettingsState): EditorDiffOverlay? {
        val addedColor = DiffStatsColors.added(settings)
        val removedColor = DiffStatsColors.removed(settings)
        val cleanupActions = mutableListOf<() -> Unit>()
        val inlays = mutableListOf<Inlay<*>>()
        val highlighters = mutableListOf<RangeHighlighter>()
        return runCatching {
            hunks.forEach { inlays += createInlays(editor, file, it, addedColor, removedColor, cleanupActions) }
            hunks.forEach { highlighters += createHighlighters(editor, it, addedColor) }
            if (inlays.isEmpty() && highlighters.isEmpty()) {
                cleanupActions.forEach { it() }
                return null
            }
            EditorDiffOverlay(file, editor, inlays, highlighters, cleanupActions)
        }.onFailure {
            if (it is ProcessCanceledException) throw it
            inlays.filter { inlay -> inlay.isValid }.forEach { inlay -> inlay.dispose() }
            highlighters.forEach { highlighter -> editor.markupModel.removeHighlighter(highlighter) }
            cleanupActions.forEach { cleanup -> cleanup() }
        }.getOrNull()
    }

    private fun createInlays(
        editor: Editor,
        file: VirtualFile,
        hunk: GitDiffHunk,
        addedColor: Color,
        removedColor: Color,
        cleanupActions: MutableList<() -> Unit>
    ): List<Inlay<*>> {
        val headerSegments = createHeaderSegments(hunk, addedColor, removedColor)
        if (headerSegments.isEmpty()) return emptyList()
        val inlays = mutableListOf<Inlay<*>>()
        editor.inlayModel.addBlockElement(
            inlayOffset(editor.document, hunk),
            InlayProperties().showAbove(true).priority(100),
            EditorDiffHintRenderer(headerSegments)
        )?.let { inlays.add(it) }
        if (hunk.removedLines.isNotEmpty()) {
            val deletedBlock = EditorDiffDeletedBlockComponentFactory.create(
                editor,
                file,
                hunk.removedLines,
                EditorDiffHintColors.lineBackground(removedColor)
            )
            editor.addComponentInlay(
                inlayOffset(editor.document, hunk),
                InlayProperties().showAbove(true).priority(200),
                deletedBlock.component,
                ComponentInlayAlignment.FIT_VIEWPORT_X_SPAN
            )?.let {
                inlays.add(it)
                cleanupActions.add(deletedBlock.release)
            } ?: deletedBlock.release()
        }
        return inlays
    }

    private fun createHighlighters(editor: Editor, hunk: GitDiffHunk, addedColor: Color): List<RangeHighlighter> {
        if (hunk.addedLineCount <= 0 || editor.document.lineCount <= 0) return emptyList()
        val startLine = hunk.addedStartLine.coerceIn(0, editor.document.lineCount - 1)
        val endLine = (hunk.addedStartLine + hunk.addedLineCount - 1).coerceIn(startLine, editor.document.lineCount - 1)
        return (startLine..endLine)
            .filter { hasVisibleLineContent(editor.document, it) }
            .map {
                editor.markupModel.addRangeHighlighter(
                    editor.document.getLineStartOffset(it),
                    editor.document.getLineEndOffset(it),
                    HighlighterLayer.ADDITIONAL_SYNTAX,
                    TextAttributes().apply {
                        backgroundColor = EditorDiffHintColors.lineBackground(addedColor)
                    },
                    HighlighterTargetArea.LINES_IN_RANGE
                )
            }
    }

    private fun inlayOffset(document: Document, hunk: GitDiffHunk): Int {
        if (document.textLength == 0) return 0
        if (hunk.addedLineCount > 0) {
            val line = hunk.addedStartLine.coerceIn(0, document.lineCount - 1)
            return document.getLineStartOffset(line)
        }
        val line = hunk.addedStartLine.coerceAtLeast(0)
        return if (line >= document.lineCount) document.textLength else document.getLineStartOffset(line)
    }

    private fun createHeaderSegments(hunk: GitDiffHunk, addedColor: Color, removedColor: Color) = buildList {
        if (hunk.removedLineCount > 0) add(EditorDiffHintSegment("-${hunk.removedLineCount}", removedColor, EditorDiffHintColors.chipBackground(removedColor)))
        if (hunk.addedLineCount > 0) add(EditorDiffHintSegment("+${hunk.addedLineCount}", addedColor, EditorDiffHintColors.chipBackground(addedColor)))
    }

    private fun hasVisibleLineContent(document: Document, line: Int): Boolean {
        val startOffset = document.getLineStartOffset(line)
        val endOffset = document.getLineEndOffset(line)
        if (startOffset >= endOffset) return false
        return document.charsSequence.subSequence(startOffset, endOffset).any { !it.isWhitespace() }
    }
}

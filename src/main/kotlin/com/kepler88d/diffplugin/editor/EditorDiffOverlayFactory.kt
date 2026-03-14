package com.kepler88d.diffplugin.editor

import com.kepler88d.diffplugin.diff.DiffStatsColors
import com.kepler88d.diffplugin.git.GitDiffHunk
import com.kepler88d.diffplugin.settings.DiffStatsSettingsState
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.Inlay
import com.intellij.openapi.editor.InlayProperties
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
        val inlays = hunks.mapNotNull { createInlay(editor, it, addedColor, removedColor) }
        val highlighters = hunks.mapNotNull { createHighlighter(editor, it, addedColor) }
        return if (inlays.isEmpty() && highlighters.isEmpty()) null else EditorDiffOverlay(file, editor, inlays, highlighters)
    }

    private fun createInlay(editor: Editor, hunk: GitDiffHunk, addedColor: Color, removedColor: Color): Inlay<*>? {
        val segments = buildList {
            if (hunk.removedLineCount > 0) add(EditorDiffHintSegment("-${hunk.removedLineCount}", removedColor, EditorDiffHintColors.chipBackground(removedColor)))
            if (hunk.addedLineCount > 0) add(EditorDiffHintSegment("+${hunk.addedLineCount}", addedColor, EditorDiffHintColors.chipBackground(addedColor)))
        }
        if (segments.isEmpty()) return null
        return editor.inlayModel.addBlockElement(
            inlayOffset(editor.document, hunk),
            InlayProperties().showAbove(true).priority(100),
            EditorDiffHintRenderer(segments)
        )
    }

    private fun createHighlighter(editor: Editor, hunk: GitDiffHunk, addedColor: Color): RangeHighlighter? {
        if (hunk.addedLineCount <= 0 || editor.document.lineCount <= 0) return null
        val startLine = hunk.addedStartLine.coerceIn(0, editor.document.lineCount - 1)
        val endLine = (hunk.addedStartLine + hunk.addedLineCount - 1).coerceIn(startLine, editor.document.lineCount - 1)
        return editor.markupModel.addRangeHighlighter(
            editor.document.getLineStartOffset(startLine),
            editor.document.getLineEndOffset(endLine),
            HighlighterLayer.ADDITIONAL_SYNTAX,
            TextAttributes().apply {
                backgroundColor = EditorDiffHintColors.lineBackground(addedColor)
            },
            HighlighterTargetArea.LINES_IN_RANGE
        )
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
}

package com.kepler88d.diffplugin.editor

import com.intellij.openapi.editor.EditorCustomElementRenderer
import com.intellij.openapi.editor.Inlay
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.colors.EditorFontType
import com.intellij.openapi.editor.markup.TextAttributes
import com.intellij.util.ui.JBUI
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.geom.Rectangle2D

internal class EditorDiffHintRenderer(
    private val segments: List<EditorDiffHintSegment>
) : EditorCustomElementRenderer {
    companion object {
        private val chipHorizontalPadding = JBUI.scale(6)
        private val chipGap = JBUI.scale(4)
        private val chipArc = JBUI.scale(8)
        private val verticalPadding = JBUI.scale(2)
    }

    override fun calcWidthInPixels(inlay: Inlay<*>) = segmentsWidth(inlay.editor)

    override fun calcHeightInPixels(inlay: Inlay<*>) = inlay.editor.lineHeight

    override fun paint(inlay: Inlay<*>, graphics: Graphics2D, targetRegion: Rectangle2D, textAttributes: TextAttributes) {
        val editor = inlay.editor
        val font = editor.colorsScheme.getFont(EditorFontType.PLAIN)
        val metrics = editor.component.getFontMetrics(font)
        val chipHeight = editor.lineHeight - verticalPadding * 2
        var x = targetRegion.x.toInt()
        val y = targetRegion.y.toInt() + verticalPadding
        val baseline = y + ((chipHeight - metrics.height) / 2).coerceAtLeast(0) + metrics.ascent
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        graphics.font = font
        segments.forEach { segment ->
            val width = metrics.stringWidth(segment.text) + chipHorizontalPadding * 2
            graphics.color = segment.backgroundColor
            graphics.fillRoundRect(x, y, width, chipHeight, chipArc, chipArc)
            graphics.color = segment.foregroundColor
            graphics.drawString(segment.text, x + chipHorizontalPadding, baseline)
            x += width + chipGap
        }
    }

    private fun segmentsWidth(editor: Editor): Int {
        val font = editor.colorsScheme.getFont(EditorFontType.PLAIN)
        val metrics = editor.component.getFontMetrics(font)
        return segments.sumOf { metrics.stringWidth(it.text) + chipHorizontalPadding * 2 } + chipGap * (segments.size - 1).coerceAtLeast(0)
    }
}

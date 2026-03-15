package com.kepler88d.diffplugin.editor

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.colors.EditorColorsScheme
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import java.awt.Container
import java.awt.Font
import javax.swing.JPanel

internal object EditorDiffDeletedBlockComponentFactory {
    fun create(
        editor: Editor,
        file: VirtualFile,
        removedLines: List<String>,
        backgroundColor: java.awt.Color
    ): EditorDiffDeletedBlock {
        val viewer = createViewer(editor, file, removedLines, backgroundColor)
        return EditorDiffDeletedBlock(createWrapper(viewer.component, backgroundColor)) {
            ApplicationManager.getApplication().invokeLater({
                if (viewer.isDisposed) return@invokeLater
                viewer.component.parent?.remove(viewer.component)
                EditorFactory.getInstance().releaseEditor(viewer)
            }, ModalityState.any())
        }
    }

    private fun createWrapper(component: javax.swing.JComponent, backgroundColor: java.awt.Color) = JPanel(BorderLayout()).apply {
        isOpaque = true
        background = backgroundColor
        border = JBUI.Borders.empty()
        add(component, BorderLayout.CENTER)
    }

    private fun createViewer(
        editor: Editor,
        file: VirtualFile,
        removedLines: List<String>,
        blockBackgroundColor: java.awt.Color
    ): EditorEx {
        val document = EditorFactory.getInstance().createDocument(removedLines.joinToString("\n"))
        return (EditorFactory.getInstance().createEditor(document, editor.project, file.fileType, true) as EditorEx).apply {
            setViewer(true)
            setOneLineMode(false)
            setEmbeddedIntoDialogWrapper(true)
            colorsScheme = editor.colorsScheme
            setBackgroundColor(blockBackgroundColor)
            scrollPane.border = JBUI.Borders.empty()
            scrollPane.viewportBorder = JBUI.Borders.empty()
            scrollPane.isOpaque = false
            scrollPane.viewport.isOpaque = false
            gutterComponentEx.setPaintBackground(false)
            gutterComponentEx.setInitialIconAreaWidth(0)
            contentComponent.border = JBUI.Borders.empty()
            contentComponent.isOpaque = false
            component.border = JBUI.Borders.empty()
            component.isOpaque = false
            settings.apply {
                isLineNumbersShown = false
                isFoldingOutlineShown = false
                isLineMarkerAreaShown = false
                setGutterIconsShown(false)
                isRightMarginShown = false
                isCaretRowShown = false
                isWhitespacesShown = false
                isIndentGuidesShown = false
                additionalColumnsCount = 0
                additionalLinesCount = 0
                setUseSoftWraps(false)
            }
            setVerticalScrollbarVisible(false)
            setHorizontalScrollbarVisible(false)
            contentComponent.font = editorFont(editor.colorsScheme)
        }
    }

    private fun editorFont(colorsScheme: EditorColorsScheme): Font = colorsScheme.getFont(com.intellij.openapi.editor.colors.EditorFontType.PLAIN)
}

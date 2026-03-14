package com.kepler88d.diffplugin.editor

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.Inlay
import com.intellij.openapi.editor.markup.RangeHighlighter
import com.intellij.openapi.vfs.VirtualFile

internal data class EditorDiffOverlay(
    val file: VirtualFile,
    val editor: Editor,
    val inlays: List<Inlay<*>>,
    val highlighters: List<RangeHighlighter>
) {
    fun dispose() {
        inlays.filter { it.isValid }.forEach { it.dispose() }
        highlighters.forEach { editor.markupModel.removeHighlighter(it) }
    }
}

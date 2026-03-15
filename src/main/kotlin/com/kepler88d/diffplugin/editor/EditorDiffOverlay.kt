package com.kepler88d.diffplugin.editor

import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.Inlay
import com.intellij.openapi.editor.markup.RangeHighlighter
import com.intellij.openapi.vfs.VirtualFile

internal data class EditorDiffOverlay(
    val file: VirtualFile,
    val editor: Editor,
    val inlays: List<Inlay<*>>,
    val highlighters: List<RangeHighlighter>,
    val cleanupActions: List<() -> Unit> = emptyList()
) {
    fun dispose() {
        highlighters.forEach { highlighter ->
            runCatching { editor.markupModel.removeHighlighter(highlighter) }
                .onFailure { if (it is ProcessCanceledException) throw it }
        }
        inlays.filter { it.isValid }.forEach { inlay ->
            runCatching { inlay.dispose() }
                .onFailure { if (it is ProcessCanceledException) throw it }
        }
        cleanupActions.forEach { cleanup ->
            runCatching { cleanup() }
                .onFailure { if (it is ProcessCanceledException) throw it }
        }
    }
}

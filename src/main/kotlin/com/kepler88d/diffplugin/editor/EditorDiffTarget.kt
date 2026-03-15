package com.kepler88d.diffplugin.editor

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.vfs.VirtualFile

internal data class EditorDiffTarget(
    val editor: Editor,
    val file: VirtualFile
)

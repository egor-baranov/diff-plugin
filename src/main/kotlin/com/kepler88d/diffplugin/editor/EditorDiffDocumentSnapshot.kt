package com.kepler88d.diffplugin.editor

internal data class EditorDiffDocumentSnapshot(
    val text: String,
    val lineCount: Int,
    val isUnsaved: Boolean,
    val modificationStamp: Long
)

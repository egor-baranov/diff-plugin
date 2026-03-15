package com.kepler88d.diffplugin.editor

import javax.swing.JComponent

internal data class EditorDiffDeletedBlock(
    val component: JComponent,
    val release: () -> Unit
)

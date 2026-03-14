package com.kepler88d.diffplugin.editor

import java.awt.Color

internal object EditorDiffHintColors {
    fun chipBackground(color: Color) = Color(color.red, color.green, color.blue, 44)

    fun lineBackground(color: Color) = Color(color.red, color.green, color.blue, 28)
}

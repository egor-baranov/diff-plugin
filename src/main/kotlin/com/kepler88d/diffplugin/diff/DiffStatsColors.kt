package com.kepler88d.diffplugin.diff

import com.kepler88d.diffplugin.settings.DiffStatsSettingsState
import com.intellij.ui.JBColor
import java.awt.Color

internal object DiffStatsColors {
    private val added = JBColor.namedColor("VersionControl.FileStatus.addedForeground", JBColor(0x2E7D32, 0x629755))
    private val removed = JBColor.namedColor("VersionControl.FileStatus.deletedForeground", JBColor(0xC62828, 0xCF6679))

    fun added(settings: DiffStatsSettingsState) = DiffStatsColorHex.parse(settings.addedColorHex) ?: added

    fun removed(settings: DiffStatsSettingsState) = DiffStatsColorHex.parse(settings.removedColorHex) ?: removed

    fun defaultAdded(): Color = added

    fun defaultRemoved(): Color = removed
}

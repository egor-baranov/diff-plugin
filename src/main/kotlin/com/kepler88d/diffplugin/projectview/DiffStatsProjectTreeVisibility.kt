package com.kepler88d.diffplugin.projectview

import com.kepler88d.diffplugin.settings.DiffStatsSettingsState

internal object DiffStatsProjectTreeVisibility {
    fun shouldShow(nodeType: DiffStatsProjectNodeType, settings: DiffStatsSettingsState) = when (nodeType) {
        DiffStatsProjectNodeType.FILE -> settings.showProjectFiles
        DiffStatsProjectNodeType.DIRECTORY -> settings.showProjectDirectories
        DiffStatsProjectNodeType.GROUP -> settings.showProjectGroups
        DiffStatsProjectNodeType.NONE -> false
    }
}

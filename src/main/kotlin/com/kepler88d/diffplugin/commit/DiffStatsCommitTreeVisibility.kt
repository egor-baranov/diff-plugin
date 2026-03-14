package com.kepler88d.diffplugin.commit

import com.kepler88d.diffplugin.diff.DiffStatsTreeNodes
import com.kepler88d.diffplugin.settings.DiffStatsSettingsState
import com.intellij.openapi.vcs.changes.ui.ChangesBrowserNode

internal object DiffStatsCommitTreeVisibility {
    fun shouldShow(node: ChangesBrowserNode<*>, settings: DiffStatsSettingsState) = when (DiffStatsTreeNodes.commitNodeType(node)) {
        DiffStatsCommitNodeType.FILE -> settings.showCommitFiles
        DiffStatsCommitNodeType.DIRECTORY -> settings.showCommitDirectories
        DiffStatsCommitNodeType.CHANGES -> settings.showCommitChanges
        DiffStatsCommitNodeType.NONE -> false
    }
}

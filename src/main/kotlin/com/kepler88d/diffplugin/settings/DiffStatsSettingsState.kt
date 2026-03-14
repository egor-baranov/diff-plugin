package com.kepler88d.diffplugin.settings

internal data class DiffStatsSettingsState(
    var addedColorHex: String = "",
    var removedColorHex: String = "",
    var showStatusBar: Boolean = true,
    var showCommitFiles: Boolean = true,
    var showCommitDirectories: Boolean = true,
    var showCommitChanges: Boolean = true,
    var showCommitSummary: Boolean = true,
    var showProjectFiles: Boolean = false,
    var showProjectDirectories: Boolean = false,
    var showProjectGroups: Boolean = false
)

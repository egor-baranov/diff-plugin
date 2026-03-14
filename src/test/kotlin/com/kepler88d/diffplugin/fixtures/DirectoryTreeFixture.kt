package com.kepler88d.diffplugin.fixtures

import com.kepler88d.diffplugin.diff.DiffStats
import com.intellij.openapi.vcs.changes.ui.ChangesBrowserNode

internal data class DirectoryTreeFixture(
    val root: ChangesBrowserNode<*>,
    val directoryNode: ChangesBrowserNode<*>,
    val fileNode: ChangesBrowserNode<*>,
    val siblingNode: ChangesBrowserNode<*>,
    val statsByPath: Map<String, DiffStats>
)

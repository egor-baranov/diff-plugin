package com.kepler88d.diffplugin.diff

import com.kepler88d.diffplugin.commit.DiffStatsCommitNodeType
import com.intellij.openapi.vcs.changes.ui.AbstractChangesBrowserFilePathNode
import com.intellij.openapi.vcs.changes.Change
import com.intellij.openapi.vcs.changes.ui.ChangesBrowserChangeNode
import com.intellij.openapi.vcs.changes.ui.ChangesBrowserFileNode
import com.intellij.openapi.vcs.changes.ui.ChangesBrowserNode
import com.intellij.openapi.vcs.changes.ui.ChangesListView
import com.intellij.openapi.vcs.FilePath
import com.intellij.openapi.vfs.VirtualFile

internal object DiffStatsTreeNodes {
    fun collectRequests(root: ChangesBrowserNode<*>) = root.traverse()
        .mapNotNull { createRequest(it) }
        .distinctBy { it.key }
        .toList()

    fun collectSelectedNodes(tree: ChangesListView) = tree.selectedChangesNodes
        .filter { supportsInlineStats(it) }
        .toList()

    fun directKey(node: ChangesBrowserNode<*>) = createRequest(node)?.key

    fun commitNodeType(node: ChangesBrowserNode<*>) = when (val userObject = node.userObject) {
        is Change -> DiffStatsCommitNodeType.FILE
        is FilePath -> if (userObject.isDirectory) DiffStatsCommitNodeType.DIRECTORY else DiffStatsCommitNodeType.FILE
        is VirtualFile -> if (userObject.isDirectory) DiffStatsCommitNodeType.DIRECTORY else DiffStatsCommitNodeType.FILE
        else -> if (descendantKeys(node).isNotEmpty()) DiffStatsCommitNodeType.CHANGES else DiffStatsCommitNodeType.NONE
    }

    fun supportsInlineStats(node: ChangesBrowserNode<*>) = descendantKeys(node).isNotEmpty()

    fun descendantKeys(node: ChangesBrowserNode<*>) = node.traverse()
        .mapNotNull { directKey(it) }
        .toSet()

    private fun createRequest(node: ChangesBrowserNode<*>) = when (node) {
        is ChangesBrowserChangeNode -> DiffStatsRequestFactory.fromChange(node.userObject)
        is ChangesBrowserFileNode -> DiffStatsRequestFactory.fromVirtualFile(node.userObject)
        is AbstractChangesBrowserFilePathNode<*> -> DiffStatsRequestFactory.fromFilePath(node.userObject as? FilePath)
        else -> null
    }
}

package com.kepler88d.diffplugin.diff

import com.intellij.openapi.vcs.changes.ui.ChangesBrowserNode
import com.intellij.openapi.vcs.changes.ui.ChangesListView

internal object DiffStatsSelectionCalculator {
    fun aggregate(node: ChangesBrowserNode<*>, statsByPath: Map<String, DiffStats>): DiffStats? {
        val keys = DiffStatsTreeNodes.descendantKeys(node)
        return sum(keys, statsByPath).takeIf { it.hasValue }
    }

    fun sumAll(stats: Collection<DiffStats>) = stats.fold(DiffStats.EMPTY) { total, entry -> total + entry }

    fun sumIncluded(tree: ChangesListView, statsByPath: Map<String, DiffStats>): DiffStats {
        val keys = tree.root.traverse()
            .mapNotNull { node ->
                val key = DiffStatsTreeNodes.directKey(node) ?: return@mapNotNull null
                if (tree.isIncluded(node.userObject)) key else null
            }
            .toSet()
        return sum(keys, statsByPath)
    }

    fun sumSelected(tree: ChangesListView, statsByPath: Map<String, DiffStats>): DiffStats? {
        return sumNodes(DiffStatsTreeNodes.collectSelectedNodes(tree), statsByPath)
    }

    fun sumNodes(nodes: Collection<ChangesBrowserNode<*>>, statsByPath: Map<String, DiffStats>): DiffStats? {
        val keys = nodes.flatMap { DiffStatsTreeNodes.descendantKeys(it) }.toSet()
        return sum(keys, statsByPath).takeIf { it.hasValue }
    }

    private fun sum(keys: Set<String>, statsByPath: Map<String, DiffStats>) =
        keys.fold(DiffStats.EMPTY) { total, key -> total + (statsByPath[key] ?: DiffStats.EMPTY) }
}

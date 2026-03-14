package com.kepler88d.diffplugin.projectview

import com.kepler88d.diffplugin.diff.DiffStats
import com.kepler88d.diffplugin.diff.DiffStatsPathKey
import com.kepler88d.diffplugin.diff.DiffStatsSelectionCalculator
import com.intellij.ide.projectView.ProjectViewNode
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project

internal object DiffStatsProjectNodes {
    fun aggregate(node: ProjectViewNode<*>, statsByPath: Map<String, DiffStats>) = when (nodeType(node)) {
        DiffStatsProjectNodeType.FILE -> fileStats(node, statsByPath)
        DiffStatsProjectNodeType.DIRECTORY -> directoryStats(node, statsByPath)
        DiffStatsProjectNodeType.GROUP -> groupStats(node, statsByPath)
        DiffStatsProjectNodeType.NONE -> null
    }

    fun nodeType(node: ProjectViewNode<*>) = when {
        node.value is Project || node.value is Module -> DiffStatsProjectNodeType.GROUP
        node.virtualFile?.isDirectory == false -> DiffStatsProjectNodeType.FILE
        node.virtualFile?.isDirectory == true -> DiffStatsProjectNodeType.DIRECTORY
        node.roots.isNotEmpty() -> DiffStatsProjectNodeType.GROUP
        else -> DiffStatsProjectNodeType.NONE
    }

    private fun directoryStats(node: ProjectViewNode<*>, statsByPath: Map<String, DiffStats>) = node.virtualFile
        ?.takeIf { it.isDirectory }
        ?.let { DiffStatsProjectPaths.sumUnderRoots(listOf(it.path), statsByPath) }
        ?.takeIf { it.hasValue }

    private fun fileStats(node: ProjectViewNode<*>, statsByPath: Map<String, DiffStats>) = node.virtualFile
        ?.takeUnless { it.isDirectory }
        ?.let { DiffStatsPathKey.fromVirtualFile(it) }
        ?.let { statsByPath[it] }
        ?.takeIf { it.hasValue }

    private fun groupStats(node: ProjectViewNode<*>, statsByPath: Map<String, DiffStats>) = when (node.value) {
        is Project -> DiffStatsSelectionCalculator.sumAll(statsByPath.values).takeIf { it.hasValue }
        else -> DiffStatsProjectPaths.sumUnderRoots(node.roots.map { it.path }, statsByPath).takeIf { it.hasValue }
    }
}

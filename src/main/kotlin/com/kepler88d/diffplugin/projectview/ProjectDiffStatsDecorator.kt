package com.kepler88d.diffplugin.projectview

import com.kepler88d.diffplugin.diff.DiffStatsUi
import com.kepler88d.diffplugin.settings.DiffStatsSettingsService
import com.intellij.ide.projectView.PresentationData
import com.intellij.ide.projectView.ProjectViewNode
import com.intellij.ide.projectView.ProjectViewNodeDecorator
import com.intellij.openapi.components.service

internal class ProjectDiffStatsDecorator : ProjectViewNodeDecorator {
    private val settingsService = service<DiffStatsSettingsService>()

    override fun decorate(node: ProjectViewNode<*>, data: PresentationData) {
        val settings = settingsService.currentState
        val nodeType = DiffStatsProjectNodes.nodeType(node)
        if (!DiffStatsProjectTreeVisibility.shouldShow(nodeType, settings)) return
        val statsByPath = node.project.service<ProjectDiffStatsService>().statsByPath
        val stats = DiffStatsProjectNodes.aggregate(node, statsByPath) ?: return
        DiffStatsUi.append(data, stats, settings)
    }
}

package com.kepler88d.diffplugin.commit

import com.kepler88d.diffplugin.diff.DiffStatsSelectionCalculator
import com.kepler88d.diffplugin.diff.DiffStatsStateService
import com.kepler88d.diffplugin.diff.DiffStatsUi
import com.kepler88d.diffplugin.settings.DiffStatsSettingsService
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.BooleanGetter
import com.intellij.openapi.vcs.changes.ui.ChangesBrowserNode
import com.intellij.openapi.vcs.changes.ui.ChangesBrowserNodeRenderer
import javax.swing.JTree

internal class CommitDiffStatsRenderer(
    project: Project,
    flattenState: BooleanGetter,
    highlightProblems: Boolean,
    private val stateService: DiffStatsStateService,
    private val settingsService: DiffStatsSettingsService = service()
) : ChangesBrowserNodeRenderer(project, flattenState, highlightProblems) {
    override fun customizeCellRenderer(
        tree: JTree,
        value: Any,
        selected: Boolean,
        expanded: Boolean,
        leaf: Boolean,
        row: Int,
        hasFocus: Boolean
    ) {
        super.customizeCellRenderer(tree, value, selected, expanded, leaf, row, hasFocus)
        val node = value as? ChangesBrowserNode<*> ?: return
        val settings = settingsService.currentState
        if (!DiffStatsCommitTreeVisibility.shouldShow(node, settings)) return
        val stats = DiffStatsSelectionCalculator.aggregate(node, stateService.snapshotFlow.value.statsByPath) ?: return
        DiffStatsUi.append(this, stats, settings)
    }
}

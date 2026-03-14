package com.kepler88d.diffplugin.commit

import com.intellij.openapi.components.service
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.vcs.changes.CommitChangesViewWithToolbarPanel
import kotlinx.coroutines.CoroutineScope

internal class CommitDiffStatsInitializer : CommitChangesViewWithToolbarPanel.Initializer {
    override fun init(scope: CoroutineScope, panel: CommitChangesViewWithToolbarPanel) {
        val controller = CommitDiffStatsController(panel.project, scope, panel, panel.changesView, panel.project.service())
        Disposer.register(panel, controller)
    }
}

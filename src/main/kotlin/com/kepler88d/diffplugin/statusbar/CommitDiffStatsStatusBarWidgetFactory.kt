package com.kepler88d.diffplugin.statusbar

import com.kepler88d.diffplugin.common.MyMessageBundle
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.StatusBarWidget
import com.intellij.openapi.wm.StatusBarWidgetFactory
import kotlinx.coroutines.CoroutineScope

internal class CommitDiffStatsStatusBarWidgetFactory : StatusBarWidgetFactory {
    override fun getId() = CommitDiffStatsStatusBarWidget.ID

    override fun getDisplayName() = MyMessageBundle.message("status.bar.diff.stats.display.name")

    override fun isAvailable(project: Project) = true

    override fun createWidget(project: Project, scope: CoroutineScope): StatusBarWidget {
        return CommitDiffStatsStatusBarWidget(project, scope)
    }

    override fun isEnabledByDefault() = true
}

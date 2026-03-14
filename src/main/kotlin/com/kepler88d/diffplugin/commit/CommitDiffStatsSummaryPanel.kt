package com.kepler88d.diffplugin.commit

import com.kepler88d.diffplugin.diff.DiffStatsStateService
import com.kepler88d.diffplugin.diff.DiffStatsUi
import com.kepler88d.diffplugin.settings.DiffStatsSettingsService
import com.intellij.openapi.components.service
import com.intellij.vcs.commit.CommitStatusPanel
import com.intellij.ui.SimpleColoredComponent
import com.intellij.util.ui.EdtInvocationManager
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.components.BorderLayoutPanel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.awt.Component
import java.awt.Container
import javax.swing.JComponent

internal class CommitDiffStatsSummaryPanel(
    scope: CoroutineScope,
    panel: BorderLayoutPanel,
    private val stateService: DiffStatsStateService,
    private val settingsService: DiffStatsSettingsService = service()
) {
    companion object {
        const val NAME = "CommitDiffStatsSummaryPanel"
    }

    private val statsComponent = SimpleColoredComponent().apply {
        isOpaque = false
    }
    private val component = BorderLayoutPanel().apply {
        name = NAME
        isOpaque = false
        border = JBUI.Borders.emptyLeft(8)
        addToRight(statsComponent)
        isVisible = false
    }

    init {
        panel.addHierarchyListener { install(panel) }
        EdtInvocationManager.invokeLaterIfNeeded { install(panel) }
        scope.launch {
            combine(stateService.snapshotFlow, settingsService.settingsFlow) { snapshot, settings -> snapshot to settings }.collectLatest { (snapshot, settings) ->
                EdtInvocationManager.invokeLaterIfNeeded {
                    val stats = snapshot.visibleTotal.takeIf { settings.showCommitSummary && it.hasValue }
                    DiffStatsUi.update(statsComponent, stats, settings)
                    component.isVisible = settings.showCommitSummary && snapshot.visibleTotal.hasValue
                    (component.parent as? JComponent)?.let {
                        it.revalidate()
                        it.repaint()
                    }
                }
            }
        }
    }

    private fun install(panel: BorderLayoutPanel) {
        val statusPanel = findCommitStatusPanel(panel)
        if (statusPanel != null) {
            attach(component, statusPanel)
            return
        }
        if (component.parent == null) attach(component, panel, true)
    }

    private fun attach(component: Component, panel: BorderLayoutPanel, bottom: Boolean = false) {
        val previousParent = component.parent as? JComponent
        if (previousParent === panel) return
        previousParent?.remove(component)
        if (bottom) panel.addToBottom(component) else panel.addToRight(component)
        previousParent?.revalidate()
        previousParent?.repaint()
        panel.revalidate()
        panel.repaint()
    }

    private fun findCommitStatusPanel(component: JComponent): CommitStatusPanel? {
        var current: Container? = component.parent
        while (current != null) {
            findCommitStatusPanel(current)?.let { return it }
            current = current.parent
        }
        return null
    }

    private fun findCommitStatusPanel(container: Container): CommitStatusPanel? {
        container.components.forEach { child ->
            when (child) {
                is CommitStatusPanel -> return child
                is Container -> findCommitStatusPanel(child)?.let { return it }
            }
        }
        return null
    }
}

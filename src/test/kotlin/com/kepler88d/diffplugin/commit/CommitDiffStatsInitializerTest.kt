package com.kepler88d.diffplugin.commit

import com.intellij.openapi.components.service
import com.intellij.openapi.vcs.changes.CommitChangesViewWithToolbarPanel
import com.intellij.openapi.vcs.changes.LocalChangesListView
import com.intellij.openapi.vcs.changes.ui.ChangesBrowserNode
import com.intellij.testFramework.PlatformTestUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.vcsUtil.VcsUtil
import com.kepler88d.diffplugin.diff.DiffStats
import com.kepler88d.diffplugin.diff.DiffStatsStateService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import java.awt.event.FocusEvent
import java.io.File
import javax.swing.JComponent
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.TreePath

class CommitDiffStatsInitializerTest : BasePlatformTestCase() {
    private lateinit var scope: CoroutineScope

    override fun setUp() {
        super.setUp()
        scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    }

    override fun tearDown() {
        try {
            scope.cancel()
        } finally {
            super.tearDown()
        }
    }

    fun testInitializerInstallsRendererAndUpdatesStatusState() {
        val tempDirectory = kotlin.io.path.createTempDirectory("diff-stats-test").toFile()
        val file = File(tempDirectory, "tracked.txt").apply { writeText("one\ntwo\n") }
        val filePath = VcsUtil.getFilePath(file)
        val root = ChangesBrowserNode.createRoot()
        val fileNode = ChangesBrowserNode.createFilePath(filePath)
        val changesView = LocalChangesListView(project)
        val panel = CommitChangesViewWithToolbarPanel(changesView, testRootDisposable)
        val stateService = project.service<DiffStatsStateService>()

        root.add(fileNode)
        changesView.model = DefaultTreeModel(root)
        changesView.setIncludedChanges(listOf(filePath))
        CommitDiffStatsInitializer().init(scope, panel)

        assertInstanceOf(changesView.cellRenderer, CommitDiffStatsRenderer::class.java)
        assertNotNull(findNamedComponent(panel, CommitDiffStatsSummaryPanel.NAME))
        waitForCondition {
            val snapshot = stateService.snapshotFlow.value
            snapshot.commitTotal == DiffStats(2, 0) && snapshot.visibleTotal == DiffStats(2, 0)
        }

        changesView.selectionPath = TreePath(fileNode.path)
        changesView.focusListeners.forEach { listener ->
            listener.focusGained(FocusEvent(changesView, FocusEvent.FOCUS_GAINED))
        }
        waitForCondition {
            val state = stateService.statusBarStateFlow.value
            state.showSelection && state.selectionTotal == DiffStats(2, 0)
        }

        changesView.setIncludedChanges(emptyList<Any>())
        waitForCondition { !stateService.snapshotFlow.value.commitTotal.hasValue }
    }

    private fun waitForCondition(condition: () -> Boolean) {
        repeat(50) {
            PlatformTestUtil.dispatchAllInvocationEventsInIdeEventQueue()
            if (condition()) return
            Thread.sleep(100)
        }
        fail("Condition was not met in time")
    }

    private fun findNamedComponent(component: JComponent, name: String): JComponent? {
        if (component.name == name) return component
        component.components.forEach { child ->
            val nested = (child as? JComponent)?.let { findNamedComponent(it, name) }
            if (nested != null) return nested
        }
        return null
    }
}

package com.kepler88d.diffplugin.commit

import com.kepler88d.diffplugin.diff.*
import com.kepler88d.diffplugin.git.GitDiffStatsReader
import com.kepler88d.diffplugin.settings.DiffStatsSettingsService
import com.kepler88d.diffplugin.statusbar.DiffStatsStatusBarState
import com.intellij.openapi.Disposable
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.BooleanGetter
import com.intellij.openapi.vcs.changes.InclusionListener
import com.intellij.openapi.vcs.changes.CommitChangesViewWithToolbarPanel
import com.intellij.openapi.vcs.changes.ui.ChangesListView
import com.intellij.util.ui.EdtInvocationManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.awt.event.FocusAdapter
import java.awt.event.FocusEvent
import java.beans.PropertyChangeEvent
import java.beans.PropertyChangeListener
import javax.swing.event.TreeModelEvent
import javax.swing.event.TreeModelListener
import javax.swing.event.TreeSelectionListener
import javax.swing.tree.TreeModel

internal class CommitDiffStatsController(
    private val project: Project,
    scope: CoroutineScope,
    panel: CommitChangesViewWithToolbarPanel,
    private val changesView: ChangesListView,
    private val stateService: DiffStatsStateService = project.service(),
    private val settingsService: DiffStatsSettingsService = service()
) : Disposable {

    private val refreshRequests = MutableSharedFlow<List<DiffStatsRequest>>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    private var isTreeFocused = false
    private val focusListener = object : FocusAdapter() {
        override fun focusGained(event: FocusEvent) {
            isTreeFocused = true
            publishStatusState()
        }

        override fun focusLost(event: FocusEvent) {
            isTreeFocused = false
            publishStatusState()
        }
    }
    private val inclusionListener = object : InclusionListener {
        override fun inclusionChanged() = requestSnapshotRefresh()
    }
    private val modelListener = object : TreeModelListener {
        override fun treeNodesChanged(event: TreeModelEvent) = requestSnapshotRefresh()

        override fun treeNodesInserted(event: TreeModelEvent) = requestSnapshotRefresh()

        override fun treeNodesRemoved(event: TreeModelEvent) = requestSnapshotRefresh()

        override fun treeStructureChanged(event: TreeModelEvent) = requestSnapshotRefresh()
    }
    private val modelPropertyListener = PropertyChangeListener { event -> onModelChanged(event) }
    private val selectionListener = TreeSelectionListener { publishStatusState() }
    private var attachedModel: TreeModel? = null
    private var disposed = false

    init {
        installRenderer()
        CommitDiffStatsSummaryPanel(scope, panel, stateService)
        changesView.addFocusListener(focusListener)
        changesView.addTreeSelectionListener(selectionListener)
        changesView.inclusionModel.addInclusionListener(inclusionListener)
        changesView.addPropertyChangeListener("model", modelPropertyListener)
        attachModelListener(changesView.model)
        scope.launch {
            refreshRequests.collectLatest { requests ->
                val statsByPath = GitDiffStatsReader.load(project, requests)
                EdtInvocationManager.invokeLaterIfNeeded { publishSnapshot(statsByPath) }
            }
        }
        scope.launch {
            settingsService.settingsFlow.collectLatest {
                EdtInvocationManager.invokeLaterIfNeeded { changesView.repaint() }
            }
        }
        requestSnapshotRefresh()
    }

    override fun dispose() {
        disposed = true
        attachedModel?.removeTreeModelListener(modelListener)
        changesView.removeFocusListener(focusListener)
        changesView.removeTreeSelectionListener(selectionListener)
        changesView.inclusionModel.removeInclusionListener(inclusionListener)
        changesView.removePropertyChangeListener("model", modelPropertyListener)
        stateService.updateSnapshot(DiffStatsSnapshot.EMPTY)
        stateService.updateStatusBarState(DiffStatsStatusBarState.EMPTY)
    }

    private fun installRenderer() {
        changesView.cellRenderer = CommitDiffStatsRenderer(
            project,
            { changesView.isShowFlatten },
            changesView.isHighlightProblems,
            stateService
        )
    }

    private fun onModelChanged(event: PropertyChangeEvent) {
        attachModelListener(event.newValue as? TreeModel)
        publishStatusState()
        requestSnapshotRefresh()
    }

    private fun publishSnapshot(statsByPath: Map<String, DiffStats>) {
        if (disposed) return
        val commitTotal = DiffStatsSelectionCalculator.sumIncluded(changesView, statsByPath)
        val visibleTotal = DiffStatsSelectionCalculator.sumAll(statsByPath.values)
        stateService.updateSnapshot(DiffStatsSnapshot(statsByPath, commitTotal, visibleTotal))
        publishStatusState()
        changesView.repaint()
    }

    private fun publishStatusState() {
        if (disposed) return
        val snapshot = stateService.snapshotFlow.value
        val selectionTotal = DiffStatsSelectionCalculator.sumSelected(changesView, snapshot.statsByPath)
        stateService.updateStatusBarState(
            DiffStatsStatusBarState(
                snapshot.commitTotal,
                selectionTotal,
                null,
                isTreeFocused && selectionTotal?.hasValue == true,
                false
            )
        )
    }

    private fun requestSnapshotRefresh() {
        if (disposed) return
        refreshRequests.tryEmit(DiffStatsTreeNodes.collectRequests(changesView.root))
    }

    private fun attachModelListener(model: TreeModel?) {
        if (attachedModel === model) return
        attachedModel?.removeTreeModelListener(modelListener)
        model?.addTreeModelListener(modelListener)
        attachedModel = model
    }
}

package com.kepler88d.diffplugin.projectview

import com.kepler88d.diffplugin.diff.DiffStats
import com.kepler88d.diffplugin.diff.DiffStatsRequestFactory
import com.kepler88d.diffplugin.git.GitDiffStatsReader
import com.kepler88d.diffplugin.settings.DiffStatsSettingsService
import com.intellij.ide.projectView.ProjectView
import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.changes.ChangeListListener
import com.intellij.openapi.vcs.changes.ChangeListManager
import com.intellij.util.ui.EdtInvocationManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@Service(Service.Level.PROJECT)
internal class ProjectDiffStatsService(
    private val project: Project
) : Disposable {
    private val settingsService = service<DiffStatsSettingsService>()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val refreshRequests = MutableSharedFlow<Unit>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    private val changeListListener = object : ChangeListListener {
        override fun allChangeListsMappingsChanged() = requestRefresh()

        override fun changeListUpdateDone() = requestRefresh()

        override fun changedFileStatusChanged() = requestRefresh()

        override fun unchangedFileStatusChanged() = requestRefresh()
    }
    var statsByPath: Map<String, DiffStats> = emptyMap()
        private set

    init {
        ChangeListManager.getInstance(project).addChangeListListener(changeListListener, this)
        scope.launch {
            refreshRequests.collectLatest {
                val loadedStats = loadStats()
                EdtInvocationManager.invokeLaterIfNeeded {
                    if (project.isDisposed) return@invokeLaterIfNeeded
                    statsByPath = loadedStats
                    ProjectView.getInstance(project).refresh()
                }
            }
        }
        scope.launch {
            settingsService.settingsFlow.collectLatest {
                EdtInvocationManager.invokeLaterIfNeeded {
                    if (project.isDisposed) return@invokeLaterIfNeeded
                    ProjectView.getInstance(project).refresh()
                }
            }
        }
        requestRefresh()
    }

    override fun dispose() {
        scope.cancel()
    }

    private suspend fun loadStats(): Map<String, DiffStats> {
        val changeListManager = ChangeListManager.getInstance(project)
        val requests = changeListManager.allChanges
            .mapNotNull { DiffStatsRequestFactory.fromChange(it) } +
            changeListManager.unversionedFilesPaths.mapNotNull { DiffStatsRequestFactory.fromFilePath(it) }
        return GitDiffStatsReader.load(project, requests.distinctBy { it.key })
    }

    private fun requestRefresh() {
        refreshRequests.tryEmit(Unit)
    }
}

package com.kepler88d.diffplugin.diff

import com.kepler88d.diffplugin.statusbar.DiffStatsStatusBarState
import com.intellij.openapi.components.Service
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@Service(Service.Level.PROJECT)
internal class DiffStatsStateService {
    private val snapshotState = MutableStateFlow(DiffStatsSnapshot.EMPTY)
    private val statusBarState = MutableStateFlow(DiffStatsStatusBarState.EMPTY)

    val snapshotFlow: StateFlow<DiffStatsSnapshot> = snapshotState.asStateFlow()
    val statusBarStateFlow: StateFlow<DiffStatsStatusBarState> = statusBarState.asStateFlow()

    fun updateSnapshot(snapshot: DiffStatsSnapshot) {
        snapshotState.value = snapshot
    }

    fun updateStatusBarState(state: DiffStatsStatusBarState) {
        statusBarState.value = state
    }
}

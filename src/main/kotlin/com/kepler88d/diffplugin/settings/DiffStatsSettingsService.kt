package com.kepler88d.diffplugin.settings

import com.kepler88d.diffplugin.diff.DiffStatsColorHex
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.SettingsCategory
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@State(
    name = "CommitDiffStatsSettings",
    storages = [Storage("commit-diff-stats.xml")],
    category = SettingsCategory.PLUGINS
)
@Service(Service.Level.APP)
internal class DiffStatsSettingsService : PersistentStateComponent<DiffStatsSettingsState> {
    private var state = DiffStatsSettingsState()
    private val settingsState = MutableStateFlow(state.copy())

    val settingsFlow: StateFlow<DiffStatsSettingsState> = settingsState.asStateFlow()
    val currentState: DiffStatsSettingsState
        get() = settingsState.value.copy()

    override fun getState() = state

    override fun loadState(state: DiffStatsSettingsState) {
        update(state)
    }

    fun update(state: DiffStatsSettingsState) {
        val normalized = state.copy(
            addedColorHex = DiffStatsColorHex.normalize(state.addedColorHex),
            removedColorHex = DiffStatsColorHex.normalize(state.removedColorHex)
        )
        if (this.state == normalized) return
        this.state = normalized
        settingsState.value = normalized.copy()
    }
}

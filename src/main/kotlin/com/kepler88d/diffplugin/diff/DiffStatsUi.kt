package com.kepler88d.diffplugin.diff

import com.intellij.ide.projectView.PresentationData
import com.kepler88d.diffplugin.settings.DiffStatsSettingsState
import com.intellij.openapi.vcs.changes.ui.ChangesBrowserNodeRenderer
import com.intellij.ui.SimpleColoredComponent
import com.intellij.ui.SimpleTextAttributes

internal object DiffStatsUi {
    fun append(renderer: ChangesBrowserNodeRenderer, stats: DiffStats, settings: DiffStatsSettingsState) {
        renderer.append(" ", SimpleTextAttributes.REGULAR_ATTRIBUTES)
        renderer.append("+${stats.added}", SimpleTextAttributes(SimpleTextAttributes.STYLE_PLAIN, DiffStatsColors.added(settings)))
        renderer.append(" ", SimpleTextAttributes.REGULAR_ATTRIBUTES)
        renderer.append("-${stats.removed}", SimpleTextAttributes(SimpleTextAttributes.STYLE_PLAIN, DiffStatsColors.removed(settings)))
    }

    fun append(presentation: PresentationData, stats: DiffStats, settings: DiffStatsSettingsState) {
        ensurePresentableText(presentation)
        presentation.addText(" ", SimpleTextAttributes.REGULAR_ATTRIBUTES)
        presentation.addText("+${stats.added}", SimpleTextAttributes(SimpleTextAttributes.STYLE_PLAIN, DiffStatsColors.added(settings)))
        presentation.addText(" ", SimpleTextAttributes.REGULAR_ATTRIBUTES)
        presentation.addText("-${stats.removed}", SimpleTextAttributes(SimpleTextAttributes.STYLE_PLAIN, DiffStatsColors.removed(settings)))
    }

    fun update(component: SimpleColoredComponent, stats: DiffStats?, settings: DiffStatsSettingsState) {
        component.clear()
        component.isVisible = stats?.hasValue == true
        if (stats?.hasValue != true) return
        component.append("+${stats.added}", SimpleTextAttributes(SimpleTextAttributes.STYLE_PLAIN, DiffStatsColors.added(settings)))
        component.append(" ", SimpleTextAttributes.REGULAR_ATTRIBUTES)
        component.append("-${stats.removed}", SimpleTextAttributes(SimpleTextAttributes.STYLE_PLAIN, DiffStatsColors.removed(settings)))
    }

    private fun ensurePresentableText(presentation: PresentationData) {
        if (presentation.coloredText.isNotEmpty()) return
        val presentableText = presentation.presentableText ?: return
        val textAttributes = presentation.forcedTextForeground
            ?.let { SimpleTextAttributes(SimpleTextAttributes.STYLE_PLAIN, it) }
            ?: SimpleTextAttributes.REGULAR_ATTRIBUTES
        presentation.addText(presentableText, textAttributes)
    }
}

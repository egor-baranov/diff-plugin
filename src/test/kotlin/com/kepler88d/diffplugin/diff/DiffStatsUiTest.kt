package com.kepler88d.diffplugin.diff

import com.intellij.ide.projectView.PresentationData
import org.junit.Assert.assertEquals
import org.junit.Test

class DiffStatsUiTest {
    @Test
    fun `keeps presentable text when appending project view stats`() {
        val presentation = PresentationData().apply {
            presentableText = "diff"
        }

        DiffStatsUi.append(presentation, DiffStats(22, 0), DiffStatsSettingsFixture.state())

        assertEquals(listOf("diff", " ", "+22", " ", "-0"), presentation.coloredText.map { it.text })
    }
}

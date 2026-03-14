package com.kepler88d.diffplugin.commit

import com.kepler88d.diffplugin.diff.DiffStatsSelectionCalculatorTest
import com.kepler88d.diffplugin.diff.DiffStatsTreeNodes
import com.kepler88d.diffplugin.fixtures.DirectoryTreeFixture
import com.kepler88d.diffplugin.settings.DiffStatsSettingsState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DiffStatsCommitTreeVisibilityTest {
    @Test
    fun classifiesCommitNodesByDisplayKind() {
        val (root, directoryNode, fileNode, _, _) = createDirectoryTree()

        assertEquals(DiffStatsCommitNodeType.CHANGES, DiffStatsTreeNodes.commitNodeType(root))
        assertEquals(DiffStatsCommitNodeType.DIRECTORY, DiffStatsTreeNodes.commitNodeType(directoryNode))
        assertEquals(DiffStatsCommitNodeType.FILE, DiffStatsTreeNodes.commitNodeType(fileNode))
    }

    @Test
    fun appliesSeparateCommitWindowToggles() {
        val (root, directoryNode, fileNode, _, _) = createDirectoryTree()
        val settings = DiffStatsSettingsState(
            showCommitFiles = false,
            showCommitDirectories = false,
            showCommitChanges = true
        )

        assertTrue(DiffStatsCommitTreeVisibility.shouldShow(root, settings))
        assertFalse(DiffStatsCommitTreeVisibility.shouldShow(directoryNode, settings))
        assertFalse(DiffStatsCommitTreeVisibility.shouldShow(fileNode, settings))
    }

    private fun createDirectoryTree() = DiffStatsSelectionCalculatorTest().run {
        val method = DiffStatsSelectionCalculatorTest::class.java.getDeclaredMethod("createDirectoryTree")
        method.isAccessible = true
        method.invoke(this) as DirectoryTreeFixture
    }
}

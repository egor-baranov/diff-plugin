package com.kepler88d.diffplugin.diff

import com.kepler88d.diffplugin.fixtures.DirectoryTreeFixture
import com.intellij.openapi.vcs.changes.ui.ChangesBrowserNode
import com.intellij.vcsUtil.VcsUtil
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.File

class DiffStatsSelectionCalculatorTest {
    @Test
    fun aggregatesGenericGroupTotalsFromDescendants() {
        val (root, _, _, _, statsByPath) = createDirectoryTree()

        assertEquals(DiffStats(5, 5), DiffStatsSelectionCalculator.aggregate(root, statsByPath))
        assertEquals(true, DiffStatsTreeNodes.supportsInlineStats(root))
    }

    @Test
    fun aggregatesDirectoryTotalsFromDescendants() {
        val (_, directoryNode, fileNode, siblingNode, statsByPath) = createDirectoryTree()

        assertEquals(DiffStats(5, 5), DiffStatsSelectionCalculator.aggregate(directoryNode, statsByPath))
        assertEquals(DiffStats(3, 1), DiffStatsSelectionCalculator.aggregate(fileNode, statsByPath))
        assertEquals(DiffStats(2, 4), DiffStatsSelectionCalculator.aggregate(siblingNode, statsByPath))
    }

    @Test
    fun deduplicatesParentAndChildSelection() {
        val (_, directoryNode, fileNode, _, statsByPath) = createDirectoryTree()

        val total = DiffStatsSelectionCalculator.sumNodes(listOf(directoryNode, fileNode), statsByPath)

        assertEquals(DiffStats(5, 5), total)
    }

    @Test
    fun countsUntrackedFileLinesAsAdditions() {
        val tempFile = kotlin.io.path.createTempFile("diff-stats", ".txt").toFile()
        tempFile.writeText("alpha\nbeta\n")
        val virtualFile = com.intellij.openapi.vfs.LocalFileSystem.getInstance().refreshAndFindFileByIoFile(tempFile)
        val filePath = VcsUtil.getFilePath(tempFile)
        val request = DiffStatsRequest(
            DiffStatsPathKey.fromFilePath(filePath),
            filePath,
            null,
            filePath,
            null,
            virtualFile
        )

        assertEquals(DiffStats(2, 0), DiffStatsFallbackCalculator.calculate(request))
    }

    private fun createDirectoryTree(): DirectoryTreeFixture {
        val root = ChangesBrowserNode.createRoot()
        val directoryPath = VcsUtil.getFilePath(File("/tmp/project/src"), true)
        val firstFilePath = VcsUtil.getFilePath(File("/tmp/project/src/First.kt"), false)
        val secondFilePath = VcsUtil.getFilePath(File("/tmp/project/src/Second.kt"), false)
        val directoryNode = ChangesBrowserNode.createFilePath(directoryPath)
        val firstFileNode = ChangesBrowserNode.createFilePath(firstFilePath)
        val secondFileNode = ChangesBrowserNode.createFilePath(secondFilePath)
        val statsByPath = mapOf(
            DiffStatsPathKey.fromFilePath(firstFilePath) to DiffStats(3, 1),
            DiffStatsPathKey.fromFilePath(secondFilePath) to DiffStats(2, 4)
        )
        root.add(directoryNode)
        directoryNode.add(firstFileNode)
        directoryNode.add(secondFileNode)
        return DirectoryTreeFixture(root, directoryNode, firstFileNode, secondFileNode, statsByPath)
    }
}

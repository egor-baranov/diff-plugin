package com.kepler88d.diffplugin.projectview

import com.kepler88d.diffplugin.diff.DiffStats
import org.junit.Assert.assertEquals
import org.junit.Test

class DiffStatsProjectPathsTest {
    @Test
    fun `sums stats under a single directory`() {
        val stats = DiffStatsProjectPaths.sumUnderRoots(
            listOf("/project/src"),
            mapOf(
                "/project/src/App.kt" to DiffStats(4, 1),
                "/project/src/nested/Util.kt" to DiffStats(2, 0),
                "/project/build.gradle.kts" to DiffStats(1, 1)
            )
        )

        assertEquals(DiffStats(6, 1), stats)
    }

    @Test
    fun `does not double count overlapping roots`() {
        val stats = DiffStatsProjectPaths.sumUnderRoots(
            listOf("/project", "/project/src"),
            mapOf(
                "/project/src/App.kt" to DiffStats(4, 1),
                "/project/README.md" to DiffStats(2, 0)
            )
        )

        assertEquals(DiffStats(6, 1), stats)
    }
}

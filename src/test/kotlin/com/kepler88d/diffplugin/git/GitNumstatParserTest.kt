package com.kepler88d.diffplugin.git

import com.kepler88d.diffplugin.diff.DiffStats
import org.junit.Assert.assertEquals
import org.junit.Test

class GitNumstatParserTest {
    @Test
    fun parsesModifiedEntry() {
        val entries = GitNumstatParser.parse("12\t3\tsrc/App.kt\u0000".toByteArray())

        assertEquals(listOf(GitNumstatEntry("src/App.kt", DiffStats(12, 3))), entries)
    }

    @Test
    fun parsesRenamedEntry() {
        val bytes = "1\t0\t\u0000old.txt\u0000new name.txt\u0000".toByteArray()
        val entries = GitNumstatParser.parse(bytes)

        assertEquals(listOf(GitNumstatEntry("new name.txt", DiffStats(1, 0))), entries)
    }

    @Test
    fun parsesEntryWithSpaces() {
        val entries = GitNumstatParser.parse("5\t4\tfolder name/file name.kt\u0000".toByteArray())

        assertEquals(listOf(GitNumstatEntry("folder name/file name.kt", DiffStats(5, 4))), entries)
    }

    @Test
    fun ignoresBinaryEntry() {
        val entries = GitNumstatParser.parse("-\t-\tbinary.dat\u0000".toByteArray())

        assertEquals(emptyList<GitNumstatEntry>(), entries)
    }

    @Test
    fun parsesMultipleEntries() {
        val output = "1\t0\tfirst.txt\u00002\t1\tsecond.txt\u0000".toByteArray()
        val entries = GitNumstatParser.parse(output)

        assertEquals(
            listOf(
                GitNumstatEntry("first.txt", DiffStats(1, 0)),
                GitNumstatEntry("second.txt", DiffStats(2, 1))
            ),
            entries
        )
    }
}

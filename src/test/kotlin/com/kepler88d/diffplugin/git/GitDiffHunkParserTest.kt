package com.kepler88d.diffplugin.git

import org.junit.Assert.assertEquals
import org.junit.Test

class GitDiffHunkParserTest {
    @Test
    fun `parses modified hunk`() {
        val hunks = GitDiffHunkParser.parse(
            """
            diff --git a/test.txt b/test.txt
            index 1111111..2222222 100644
            --- a/test.txt
            +++ b/test.txt
            @@ -10,2 +10,3 @@
            -old
            -old2
            +new
            +new2
            +new3
            """.trimIndent()
        )

        assertEquals(listOf(GitDiffHunk(9, 3, 2)), hunks)
    }

    @Test
    fun `parses pure deletion hunk`() {
        val hunks = GitDiffHunkParser.parse(
            """
            @@ -4,2 +4,0 @@
            -old
            -old2
            """.trimIndent()
        )

        assertEquals(listOf(GitDiffHunk(3, 0, 2)), hunks)
    }

    @Test
    fun `parses single line counts without explicit comma`() {
        val hunks = GitDiffHunkParser.parse(
            """
            @@ -1 +1 @@
            -old
            +new
            """.trimIndent()
        )

        assertEquals(listOf(GitDiffHunk(0, 1, 1)), hunks)
    }
}

package com.kepler88d.diffplugin.common

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MyMessageBundleTest {
    @Test
    fun `loads bundled message`() {
        assertEquals("Current file diff stats", MyMessageBundle.message("status.bar.diff.stats.tooltip.file"))
    }

    @Test
    fun `returns null for missing message`() {
        assertNull(MyMessageBundle.messageOrNull("missing.key"))
    }
}

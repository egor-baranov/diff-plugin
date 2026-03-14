package com.kepler88d.diffplugin.diff

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.awt.Color

class DiffStatsColorHexTest {
    @Test
    fun normalizesHexValues() {
        assertEquals("A1B2C3", DiffStatsColorHex.normalize("#a1b2c3"))
        assertEquals("FFEEDD", DiffStatsColorHex.normalize("ffeedd"))
    }

    @Test
    fun formatsAndParsesColors() {
        val color = Color(0x12, 0x34, 0x56)

        assertEquals("123456", DiffStatsColorHex.format(color))
        assertEquals(color, DiffStatsColorHex.parse("123456"))
    }

    @Test
    fun rejectsInvalidColors() {
        assertEquals("", DiffStatsColorHex.normalize("xyz"))
        assertNull(DiffStatsColorHex.parse("xyz"))
    }
}

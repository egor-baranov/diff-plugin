package com.kepler88d.diffplugin.diff

import java.awt.Color
import java.util.Locale

internal object DiffStatsColorHex {
    fun normalize(value: String?) = value
        ?.trim()
        ?.removePrefix("#")
        ?.takeIf { it.length == 6 && it.all(::isHexDigit) }
        ?.uppercase(Locale.ROOT)
        .orEmpty()

    fun parse(value: String?) = normalize(value)
        .takeIf { it.isNotEmpty() }
        ?.toInt(16)
        ?.let(::Color)

    fun format(color: Color) = "%06X".format(color.rgb and 0xFFFFFF)

    private fun isHexDigit(char: Char) = char in '0'..'9' || char in 'a'..'f' || char in 'A'..'F'
}

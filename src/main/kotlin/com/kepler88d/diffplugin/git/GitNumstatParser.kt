package com.kepler88d.diffplugin.git

import com.kepler88d.diffplugin.diff.DiffStats
import java.nio.charset.StandardCharsets

internal object GitNumstatParser {
    fun parse(bytes: ByteArray): List<GitNumstatEntry> {
        val cursor = ParserCursor()
        val entries = mutableListOf<GitNumstatEntry>()
        while (cursor.index < bytes.size) {
            val added = readToken(bytes, cursor, '\t'.code.toByte())
            val removed = readToken(bytes, cursor, '\t'.code.toByte())
            val path = if (hasRenameMarker(bytes, cursor)) readRenamePath(bytes, cursor) else readPath(bytes, cursor)
            parseStats(added, removed)?.let { entries += GitNumstatEntry(path, it) }
        }
        return entries
    }

    private fun hasRenameMarker(bytes: ByteArray, cursor: ParserCursor) =
        cursor.index < bytes.size && bytes[cursor.index] == 0.toByte()

    private fun parseStats(added: String, removed: String): DiffStats? {
        if (added == "-" || removed == "-") return null
        return DiffStats(added.toInt(), removed.toInt())
    }

    private fun readRenamePath(bytes: ByteArray, cursor: ParserCursor): String {
        cursor.index++
        readToken(bytes, cursor, 0)
        return readToken(bytes, cursor, 0)
    }

    private fun readPath(bytes: ByteArray, cursor: ParserCursor) = readToken(bytes, cursor, 0)

    private fun readToken(bytes: ByteArray, cursor: ParserCursor, delimiter: Byte): String {
        val start = cursor.index
        while (cursor.index < bytes.size && bytes[cursor.index] != delimiter) {
            cursor.index++
        }
        val token = String(bytes, start, cursor.index - start, StandardCharsets.UTF_8)
        cursor.index++
        return token
    }

    private class ParserCursor {
        var index = 0
    }
}

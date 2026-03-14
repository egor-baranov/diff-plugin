package com.kepler88d.diffplugin.diff

import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vcs.FilePath
import com.intellij.openapi.vfs.VirtualFile

internal object DiffStatsPathKey {
    fun fromFilePath(filePath: FilePath) = FileUtil.toSystemIndependentName(filePath.path)

    fun fromVirtualFile(virtualFile: VirtualFile) = FileUtil.toSystemIndependentName(virtualFile.path)

    fun fromRootRelativePath(root: VirtualFile, relativePath: String): String {
        val absolutePath = FileUtil.toSystemIndependentName("${root.path}/$relativePath")
        return FileUtil.toCanonicalPath(absolutePath)
    }
}

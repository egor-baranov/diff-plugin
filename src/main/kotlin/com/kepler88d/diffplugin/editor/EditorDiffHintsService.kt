package com.kepler88d.diffplugin.editor

import com.kepler88d.diffplugin.git.EditorFileDiffHunksLoader
import com.kepler88d.diffplugin.settings.DiffStatsSettingsService
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.ui.EdtInvocationManager

@Service(Service.Level.PROJECT)
internal class EditorDiffHintsService(
    private val project: Project
) : Disposable {
    private val settingsService = service<DiffStatsSettingsService>()
    private var overlay: EditorDiffOverlay? = null

    suspend fun toggle(editor: Editor, file: VirtualFile) {
        if (overlay?.editor === editor && overlay?.file == file) return clearOnEdt()
        val hunks = EditorFileDiffHunksLoader.load(project, file, editor.document)
        EdtInvocationManager.invokeLaterIfNeeded { show(editor, file, hunks) }
    }

    override fun dispose() {
        clearOnEdt()
    }

    private fun show(editor: Editor, file: VirtualFile, hunks: List<com.kepler88d.diffplugin.git.GitDiffHunk>) {
        clearNow()
        if (editor.isDisposed || hunks.isEmpty()) return
        overlay = EditorDiffOverlayFactory.create(editor, file, hunks, settingsService.currentState)
    }

    private fun clearOnEdt() {
        if (ApplicationManager.getApplication().isDispatchThread) {
            clearNow()
            return
        }
        EdtInvocationManager.invokeLaterIfNeeded { clearNow() }
    }

    private fun clearNow() {
        overlay?.dispose()
        overlay = null
    }
}

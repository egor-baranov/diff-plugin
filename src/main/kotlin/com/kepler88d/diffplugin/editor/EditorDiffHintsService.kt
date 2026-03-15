package com.kepler88d.diffplugin.editor

import com.kepler88d.diffplugin.git.EditorFileDiffHunksLoader
import com.kepler88d.diffplugin.settings.DiffStatsSettingsService
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.event.BulkAwareDocumentListener
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicLong

@Service(Service.Level.PROJECT)
internal class EditorDiffHintsService(
    private val project: Project
) : Disposable {
    private val settingsService = service<DiffStatsSettingsService>()
    private val app = ApplicationManager.getApplication()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val documentListener = object : BulkAwareDocumentListener.Simple {
        override fun beforeDocumentChange(event: DocumentEvent) {
            clearForDocumentMutation(event.document)
        }

        override fun bulkUpdateStarting(document: Document) {
            clearForDocumentMutation(document)
        }
    }
    @Volatile
    private var activeTarget: EditorDiffTarget? = null
    private var overlay: EditorDiffOverlay? = null
    private var refreshJob: Job? = null
    private val refreshGeneration = AtomicLong()

    init {
        EditorFactory.getInstance().eventMulticaster.addDocumentListener(documentListener, this)
    }

    fun toggle(editor: Editor, file: VirtualFile) {
        val target = EditorDiffTarget(editor, file)
        if (activeTarget == target) {
            if (overlay == null) {
                requestRefresh(target, clearExisting = false)
                return
            }
            clearActivePreview()
            return
        }
        activeTarget = target
        requestRefresh(target, clearExisting = true)
    }

    override fun dispose() {
        clearActivePreview()
        scope.cancel()
        clearOnEdt()
    }

    private fun show(editor: Editor, file: VirtualFile, hunks: List<com.kepler88d.diffplugin.git.GitDiffHunk>) {
        clearNow()
        if (editor.isDisposed || hunks.isEmpty()) return
        overlay = EditorDiffOverlayFactory.create(editor, file, hunks, settingsService.currentState)
    }

    private fun clearOnEdt() {
        if (app.isDispatchThread) {
            clearNow()
            return
        }
        app.invokeLater(::clearNow, ModalityState.any())
    }

    private fun clearNow() {
        val currentOverlay = overlay ?: return
        overlay = null
        currentOverlay.dispose()
    }

    private fun clearForDocumentMutation(document: Document) {
        if (!app.isDispatchThread) return
        val previewDocument = activeTarget?.editor?.document ?: overlay?.editor?.document ?: return
        if (previewDocument !== document) return
        activeTarget = null
        refreshGeneration.incrementAndGet()
        refreshJob?.cancel()
        refreshJob = null
        val doomed = overlay
        overlay = null
        val hostEditor = doomed?.editor
        doomed?.dispose()
        if (hostEditor != null && !hostEditor.isDisposed) {
            hostEditor.contentComponent.revalidate()
            hostEditor.contentComponent.repaint()
        }
    }

    private fun clearActivePreview() {
        activeTarget = null
        refreshGeneration.incrementAndGet()
        refreshJob?.cancel()
        refreshJob = null
        clearOnEdt()
    }

    private fun requestRefresh(target: EditorDiffTarget, clearExisting: Boolean) {
        val document = target.editor.document
        val snapshot = EditorDiffDocumentSnapshot(
            text = document.text,
            lineCount = if (document.textLength == 0) 0 else document.lineCount,
            isUnsaved = FileDocumentManager.getInstance().isDocumentUnsaved(document),
            modificationStamp = document.modificationStamp
        )
        val generation = refreshGeneration.incrementAndGet()
        refreshJob?.cancel()
        if (clearExisting) clearOnEdt()
        refreshJob = scope.launch {
            val hunks = EditorFileDiffHunksLoader.load(project, target.file, snapshot)
            app.invokeLater({
                if (refreshGeneration.get() != generation) return@invokeLater
                if (activeTarget != target) return@invokeLater
                if (target.editor.isDisposed) return@invokeLater
                if (target.editor.document.modificationStamp != snapshot.modificationStamp) return@invokeLater
                show(target.editor, target.file, hunks)
            }, ModalityState.any())
        }
    }
}

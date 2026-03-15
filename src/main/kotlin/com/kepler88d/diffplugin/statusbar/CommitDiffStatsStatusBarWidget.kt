package com.kepler88d.diffplugin.statusbar

import com.kepler88d.diffplugin.common.MyMessageBundle
import com.kepler88d.diffplugin.diff.DiffStats
import com.kepler88d.diffplugin.diff.DiffStatsStateService
import com.kepler88d.diffplugin.diff.DiffStatsUi
import com.kepler88d.diffplugin.editor.EditorDiffHintsService
import com.kepler88d.diffplugin.git.EditorFileDiffStatsLoader
import com.kepler88d.diffplugin.settings.DiffStatsSettingsService
import com.intellij.openapi.components.service
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.FileEditorManagerEvent
import com.intellij.openapi.fileEditor.FileEditorManagerListener
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.CustomStatusBarWidget
import com.intellij.ui.ClickListener
import com.intellij.ui.SimpleColoredComponent
import com.intellij.util.ui.EdtInvocationManager
import com.intellij.util.ui.JBUI
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.awt.Cursor
import java.awt.event.MouseEvent
import javax.swing.JComponent

internal class CommitDiffStatsStatusBarWidget(
    private val project: Project,
    scope: CoroutineScope,
    private val stateService: DiffStatsStateService = project.service()
) : CustomStatusBarWidget {
    companion object {
        const val ID = "CommitDiffStatsWidget"
    }

    private val refreshRequests = MutableSharedFlow<Unit>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    private val component = SimpleColoredComponent().apply {
        border = JBUI.Borders.empty(0, 4)
        isOpaque = false
        isVisible = false
    }
    private val connection = project.messageBus.connect()
    private var statusBarState = DiffStatsStatusBarState.EMPTY
    private var settingsState = service<DiffStatsSettingsService>().currentState
    private var editorFileTotal: DiffStats? = null
    private val fileEditorListener: FileEditorManagerListener = object : FileEditorManagerListener {
        override fun fileOpened(source: FileEditorManager, file: com.intellij.openapi.vfs.VirtualFile) = requestRefresh()

        override fun fileClosed(source: FileEditorManager, file: com.intellij.openapi.vfs.VirtualFile) = requestRefresh()

        override fun selectionChanged(event: FileEditorManagerEvent) = requestRefresh()
    }
    private val documentListener = object : DocumentListener {
        override fun documentChanged(event: DocumentEvent) {
            val file = FileDocumentManager.getInstance().getFile(event.document)
            if (file == currentSelectedFile()) requestRefresh()
        }
    }
    private val clickListener = object : ClickListener() {
        override fun onClick(event: MouseEvent, clickCount: Int): Boolean {
            val editor = currentSelectedTextEditor() ?: return false
            val file = FileDocumentManager.getInstance().getFile(editor.document) ?: return false
            if (!component.isVisible) return false
            project.service<EditorDiffHintsService>().toggle(editor, file)
            return true
        }
    }

    init {
        clickListener.installOn(component)
        connection.subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, fileEditorListener)
        EditorFactory.getInstance().eventMulticaster.addDocumentListener(documentListener, this)
        scope.launch {
            stateService.statusBarStateFlow.collectLatest { state ->
                statusBarState = state
                EdtInvocationManager.invokeLaterIfNeeded { updateComponent() }
            }
        }
        scope.launch {
            service<DiffStatsSettingsService>().settingsFlow.collectLatest { settings ->
                settingsState = settings
                EdtInvocationManager.invokeLaterIfNeeded { updateComponent() }
            }
        }
        scope.launch {
            refreshRequests.collectLatest {
                val stats = EditorFileDiffStatsLoader.load(project, currentSelectedFile())
                EdtInvocationManager.invokeLaterIfNeeded {
                    editorFileTotal = stats
                    updateComponent()
                }
            }
        }
        requestRefresh()
    }

    override fun ID() = ID

    override fun getComponent(): JComponent = component

    override fun dispose() {
        connection.disconnect()
    }

    private fun requestRefresh() {
        refreshRequests.tryEmit(Unit)
    }

    private fun updateComponent() {
        if (!settingsState.showStatusBar) {
            DiffStatsUi.update(component, null, settingsState)
            component.toolTipText = null
            component.revalidate()
            component.parent?.revalidate()
            component.repaint()
            component.parent?.repaint()
            return
        }
        val state = statusBarState.copy(
            editorFileTotal = editorFileTotal,
            showEditorFile = !statusBarState.showSelection && editorFileTotal?.hasValue == true
        )
        val stats = state.displayedStats
        DiffStatsUi.update(component, stats, settingsState)
        component.toolTipText = state.tooltipKey?.let { MyMessageBundle.messageOrNull(it) }
        component.cursor = if (stats?.hasValue == true && FileEditorManager.getInstance(project).selectedTextEditor != null) {
            Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        } else {
            Cursor.getDefaultCursor()
        }
        component.revalidate()
        component.parent?.revalidate()
        component.repaint()
        component.parent?.repaint()
    }

    private fun currentSelectedFile() = FileEditorManager.getInstance(project).selectedFiles.firstOrNull()

    private fun currentSelectedTextEditor() = FileEditorManager.getInstance(project).selectedTextEditor
}

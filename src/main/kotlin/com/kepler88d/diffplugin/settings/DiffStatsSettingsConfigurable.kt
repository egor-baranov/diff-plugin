package com.kepler88d.diffplugin.settings

import com.kepler88d.diffplugin.common.MyMessageBundle
import com.kepler88d.diffplugin.diff.DiffStatsColorHex
import com.kepler88d.diffplugin.diff.DiffStatsColors
import com.intellij.openapi.components.service
import com.intellij.openapi.options.SearchableConfigurable
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.ColorPanel
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.dsl.builder.panel
import java.awt.Color
import javax.swing.JComponent

internal class DiffStatsSettingsConfigurable : SearchableConfigurable {
    private val settingsService = service<DiffStatsSettingsService>()
    private val addedColorPanel = ColorPanel()
    private val removedColorPanel = ColorPanel()
    private val showStatusBarCheckBox = JBCheckBox(MyMessageBundle.message("settings.locations.status.bar"))
    private val showCommitFilesCheckBox = JBCheckBox(MyMessageBundle.message("settings.locations.commit.files"))
    private val showCommitDirectoriesCheckBox = JBCheckBox(MyMessageBundle.message("settings.locations.commit.directories"))
    private val showCommitChangesCheckBox = JBCheckBox(MyMessageBundle.message("settings.locations.commit.changes"))
    private val showCommitSummaryCheckBox = JBCheckBox(MyMessageBundle.message("settings.locations.commit.summary"))
    private val showProjectFilesCheckBox = JBCheckBox(MyMessageBundle.message("settings.locations.project.files"))
    private val showProjectDirectoriesCheckBox = JBCheckBox(MyMessageBundle.message("settings.locations.project.directories"))
    private val showProjectGroupsCheckBox = JBCheckBox(MyMessageBundle.message("settings.locations.project.groups"))
    private var component: DialogPanel? = null

    override fun getId() = "com.kepler88d.diffplugin.settings"

    override fun getDisplayName() = MyMessageBundle.message("settings.display.name")

    override fun createComponent(): JComponent {
        if (component == null) component = createPanel()
        reset()
        return component!!
    }

    override fun isModified() = currentUiState() != settingsService.currentState

    override fun apply() {
        settingsService.update(currentUiState())
    }

    override fun reset() {
        val settings = settingsService.currentState
        addedColorPanel.selectedColor = DiffStatsColors.added(settings)
        removedColorPanel.selectedColor = DiffStatsColors.removed(settings)
        showStatusBarCheckBox.isSelected = settings.showStatusBar
        showCommitFilesCheckBox.isSelected = settings.showCommitFiles
        showCommitDirectoriesCheckBox.isSelected = settings.showCommitDirectories
        showCommitChangesCheckBox.isSelected = settings.showCommitChanges
        showCommitSummaryCheckBox.isSelected = settings.showCommitSummary
        showProjectFilesCheckBox.isSelected = settings.showProjectFiles
        showProjectDirectoriesCheckBox.isSelected = settings.showProjectDirectories
        showProjectGroupsCheckBox.isSelected = settings.showProjectGroups
    }

    override fun disposeUIResources() {
        component = null
    }

    private fun createPanel() = panel {
        group(MyMessageBundle.message("settings.colors.group")) {
            row(MyMessageBundle.message("settings.colors.added")) {
                cell(addedColorPanel)
            }
            row(MyMessageBundle.message("settings.colors.removed")) {
                cell(removedColorPanel)
            }
        }
        group(MyMessageBundle.message("settings.locations.group")) {
            row {
                cell(showStatusBarCheckBox)
            }
            group(MyMessageBundle.message("settings.locations.commit.group")) {
                row {
                    cell(showCommitFilesCheckBox)
                }
                row {
                    cell(showCommitDirectoriesCheckBox)
                }
                row {
                    cell(showCommitChangesCheckBox)
                }
                row {
                    cell(showCommitSummaryCheckBox)
                }
            }
            group(MyMessageBundle.message("settings.locations.project.group")) {
                row {
                    cell(showProjectFilesCheckBox)
                }
                row {
                    cell(showProjectDirectoriesCheckBox)
                }
                row {
                    cell(showProjectGroupsCheckBox)
                }
            }
        }
    }

    private fun currentUiState(): DiffStatsSettingsState {
        val defaultAdded = DiffStatsColors.defaultAdded()
        val defaultRemoved = DiffStatsColors.defaultRemoved()
        return DiffStatsSettingsState(
            addedColorHex = selectedColorHex(addedColorPanel, defaultAdded),
            removedColorHex = selectedColorHex(removedColorPanel, defaultRemoved),
            showStatusBar = showStatusBarCheckBox.isSelected,
            showCommitFiles = showCommitFilesCheckBox.isSelected,
            showCommitDirectories = showCommitDirectoriesCheckBox.isSelected,
            showCommitChanges = showCommitChangesCheckBox.isSelected,
            showCommitSummary = showCommitSummaryCheckBox.isSelected,
            showProjectFiles = showProjectFilesCheckBox.isSelected,
            showProjectDirectories = showProjectDirectoriesCheckBox.isSelected,
            showProjectGroups = showProjectGroupsCheckBox.isSelected
        )
    }

    private fun selectedColorHex(panel: ColorPanel, defaultColor: Color): String {
        val selectedColor = panel.selectedColor ?: defaultColor
        val selectedHex = DiffStatsColorHex.format(selectedColor)
        return selectedHex.takeUnless { it == DiffStatsColorHex.format(defaultColor) }.orEmpty()
    }
}

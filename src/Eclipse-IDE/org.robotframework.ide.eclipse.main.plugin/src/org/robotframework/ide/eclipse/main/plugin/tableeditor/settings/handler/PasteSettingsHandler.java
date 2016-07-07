/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.settings.handler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.nebula.widgets.nattable.coordinate.PositionCoordinate;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.ui.ISources;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSetting;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSetting.SettingsGroup;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSettingsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.CreateFreshGeneralSettingCommand;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.InsertSettingCommand;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.SetKeywordCallArgumentCommand;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.SetKeywordCallCommentCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorSources;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotFormEditor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.dnd.RedClipboard;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.settings.GeneralSettingsModel;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.settings.handler.PasteSettingsHandler.E4PasteSettingsHandler;
import org.robotframework.red.commands.DIParameterizedHandler;
import org.robotframework.red.viewers.Selections;

import com.google.common.base.Optional;

public class PasteSettingsHandler extends DIParameterizedHandler<E4PasteSettingsHandler> {

    public PasteSettingsHandler() {
        super(E4PasteSettingsHandler.class);
    }

    public static class E4PasteSettingsHandler {

        @Execute
        public void pasteKeywords(@Named(ISources.ACTIVE_EDITOR_NAME) final RobotFormEditor editor,
                @Named(RobotEditorSources.SUITE_FILE_MODEL) final RobotSuiteFile fileModel,
                final RobotEditorCommandsStack commandsStack,
                @Named(Selections.SELECTION) final IStructuredSelection selection, final RedClipboard clipboard) {

            final RobotKeywordCall[] probablySettings = clipboard.getKeywordCalls();
            if (probablySettings != null) {
                insertSettings(editor, fileModel, commandsStack, selection, probablySettings);
            }
        }

        private void insertSettings(final RobotFormEditor editor, final RobotSuiteFile fileModel,
                final RobotEditorCommandsStack commandsStack, final IStructuredSelection selection,
                final RobotKeywordCall[] settings) {
            
            final Optional<RobotSetting> firstSelected = Selections.getOptionalFirstElement(selection,
                    RobotSetting.class);

            if (isNoGroupSetting(settings)) {
                RobotSetting selectedSetting = null;
                if (firstSelected.isPresent()) {
                    selectedSetting = firstSelected.get();
                } else {
                    final List<RobotElement> newGeneralSettings = createNewGeneralSettingsIfNotPresentInSection(
                            fileModel, editor.getSelectionLayerAccessor().getSelectionLayer(), commandsStack);
                    if (!newGeneralSettings.isEmpty()) {
                        selectedSetting = (RobotSetting) newGeneralSettings.get(0);
                    }
                }
                if (selectedSetting != null) {
                    insertArgumentsInNoGroupSetting(commandsStack, selectedSetting, settings);
                }
            } else {
                final RobotSettingsSection section = firstSelected.isPresent() ? firstSelected.get().getParent()
                        : fileModel.findSection(RobotSettingsSection.class).orNull();
                if (section != null) {
                    commandsStack.execute(new InsertSettingCommand(section, firstSelected, settings));
                }
            }
        }

        private void insertArgumentsInNoGroupSetting(final RobotEditorCommandsStack commandsStack,
                final RobotSetting firstSelectedSetting, final RobotKeywordCall[] settingsFromClipboard) {
            if (settingsFromClipboard.length > 0) {
                final List<String> arguments = settingsFromClipboard[0].getArguments();
                for (int i = 0; i < arguments.size(); i++) {
                    commandsStack.execute(new SetKeywordCallArgumentCommand(firstSelectedSetting, i, arguments.get(i)));
                }
                final String comment = settingsFromClipboard[0].getComment();
                if (comment != null && !comment.isEmpty()) {
                    commandsStack.execute(new SetKeywordCallCommentCommand(firstSelectedSetting, comment));
                }
            }
        }

        private boolean isNoGroupSetting(final RobotKeywordCall[] settings) {
            return settings.length > 0 && settings[0] instanceof RobotSetting
                    && ((RobotSetting) settings[0]).getGroup() == SettingsGroup.NO_GROUP;
        }

        static List<RobotElement> createNewGeneralSettingsIfNotPresentInSection(final RobotSuiteFile fileModel,
                final SelectionLayer selectionLayer, final RobotEditorCommandsStack commandsStack) {

            final List<RobotElement> newSettings = new ArrayList<>();
            final Optional<RobotSettingsSection> section = fileModel.findSection(RobotSettingsSection.class);
            if (section.isPresent()) {
                final PositionCoordinate[] selectedCellPositions = selectionLayer.getSelectedCellPositions();
                final Map<String, RobotElement> settingsMappingBeforeAddition = GeneralSettingsModel
                        .fillSettingsMapping(section.get());
                final String[] generalSettingsNames = settingsMappingBeforeAddition.keySet().toArray(new String[0]);
                final List<Integer> createdRows = new ArrayList<>();
                for (final PositionCoordinate position : selectedCellPositions) {
                    final int selectedRowNumber = position.getRowPosition();
                    if (!createdRows.contains(selectedRowNumber) && selectedRowNumber >= 0
                            && selectedRowNumber < generalSettingsNames.length
                            && settingsMappingBeforeAddition.get(generalSettingsNames[selectedRowNumber]) == null) {
                        commandsStack.execute(new CreateFreshGeneralSettingCommand(section.get(),
                                generalSettingsNames[selectedRowNumber], new ArrayList<String>()));
                        final Map<String, RobotElement> settingsMappingAfterAddition = GeneralSettingsModel
                                .fillSettingsMapping(section.get());
                        final RobotElement newGeneralSetting = settingsMappingAfterAddition
                                .get(generalSettingsNames[selectedRowNumber]);
                        if (newGeneralSetting != null) {
                            newSettings.add(newGeneralSetting);
                        }
                    }
                    createdRows.add(selectedRowNumber);
                }
            }
            return newSettings;
        }
    }
}

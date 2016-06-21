/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.settings.handler;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.nebula.widgets.nattable.coordinate.PositionCoordinate;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.ui.ISources;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSetting;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSetting.SettingsGroup;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSettingsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.SetKeywordCallArgumentCommand;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.SetKeywordCallCommentCommand;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.SetKeywordCallNameCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorSources;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotFormEditor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.dnd.KeywordCallsTransfer;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.dnd.PositionCoordinateTransfer;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.dnd.PositionCoordinateTransfer.PositionCoordinateSerializer;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.handler.TableHandlersSupport;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.settings.handler.PasteInSettingsTableHandler.E4PasteInSettingsTableHandler;
import org.robotframework.red.commands.DIParameterizedHandler;
import org.robotframework.red.viewers.Selections;

public class PasteInSettingsTableHandler extends DIParameterizedHandler<E4PasteInSettingsTableHandler> {

    public PasteInSettingsTableHandler() {
        super(E4PasteInSettingsTableHandler.class);
    }

    public static class E4PasteInSettingsTableHandler {

        @Inject
        private RobotEditorCommandsStack commandsStack;

        @Inject
        @Named(RobotEditorSources.SUITE_FILE_MODEL)
        private RobotSuiteFile fileModel;

        @Execute
        public Object paste(@Named(ISources.ACTIVE_EDITOR_NAME) final RobotFormEditor editor,
                @Named(Selections.SELECTION) final IStructuredSelection selection, final Clipboard clipboard) {

            final List<EditorCommand> pasteCommands = new ArrayList<>();

            final List<RobotSetting> selectedSettings = Selections.getElements(selection, RobotSetting.class);
            if (!selectedSettings.isEmpty() && KeywordCallsTransfer.hasSettings(clipboard)
                    && PositionCoordinateTransfer.hasPositionsCoordinates(clipboard)) {

                RobotKeywordCall[] settingsFromClipboard = getSettingsFromClipboard(clipboard);
                PositionCoordinateSerializer[] cellPositionsFromClipboard = TableHandlersSupport.getPositionsCoordinatesFromClipboard(clipboard);

                if (settingsFromClipboard != null && settingsFromClipboard.length > 0
                        && cellPositionsFromClipboard != null && cellPositionsFromClipboard.length > 0) {
                    final SelectionLayer selectionLayer = editor.getSelectionLayerAccessor().getSelectionLayer();
                    final int columnCount = selectionLayer.getColumnCount();
                    int settingFromClipboardIndex = 0;
                    int currentClipboardRowIndex = cellPositionsFromClipboard[0].getRowPosition();
                    for (RobotSetting selectedSetting : selectedSettings) {
                        RobotSetting settingFromClipboard = (RobotSetting) settingsFromClipboard[settingFromClipboardIndex];
                        if (settingFromClipboardIndex+1 < settingsFromClipboard.length) {
                            settingFromClipboardIndex++;
                        }
                        final List<Integer> selectedSettingColumnsIndexes = findColumnsToPaste(
                                selectedSetting.getParent(), selectedSetting, selectionLayer);
                        final List<Integer> clipboardSettingColumnsIndexes = findCurrentClipboardSettingColumnsIndexes(
                                currentClipboardRowIndex, cellPositionsFromClipboard);
                        currentClipboardRowIndex = calculateNextClipboardRowIndex(currentClipboardRowIndex,
                                cellPositionsFromClipboard);
                        if (!clipboardSettingColumnsIndexes.isEmpty()) {
                            for (int i = 0; i < selectedSettingColumnsIndexes.size(); i++) {
                                int clipboardSettingColumnIndex = clipboardSettingColumnsIndexes.get(0);
                                if (i > 0 && i < clipboardSettingColumnsIndexes.size()) {
                                    clipboardSettingColumnIndex = clipboardSettingColumnsIndexes.get(i);
                                }
                                final String valueToPaste = getValueToPaste(settingFromClipboard,
                                        clipboardSettingColumnIndex, columnCount);
                                collectPasteCommandsForSelectedSettings(selectedSetting,
                                        selectedSettingColumnsIndexes.get(i), columnCount, valueToPaste, pasteCommands);
                            }
                        }
                    }
                }
            }

            for (EditorCommand command : pasteCommands) {
                commandsStack.execute(command);
            }

            return null;
        }

        private RobotKeywordCall[] getSettingsFromClipboard(final Clipboard clipboard) {
            final Object probablySettings = clipboard.getContents(KeywordCallsTransfer.getInstance());
            return probablySettings != null && probablySettings instanceof RobotKeywordCall[]
                    ? (RobotKeywordCall[]) probablySettings : null;
        }

        private void collectPasteCommandsForSelectedSettings(final RobotSetting selectedSetting,
                final int selectedSettingColumnIndex, final int columnCount, final String valueToPaste,
                final List<EditorCommand> pasteCommands) {
            
            if (selectedSettingColumnIndex == columnCount - 1) {
                pasteCommands.add(new SetKeywordCallCommentCommand(selectedSetting, valueToPaste));
            } else {
                if (selectedSetting.getGroup() == SettingsGroup.METADATA) {
                    pasteCommands.add(new SetKeywordCallArgumentCommand(selectedSetting, selectedSettingColumnIndex,
                            valueToPaste));
                } else if (selectedSetting.getGroup() == SettingsGroup.NO_GROUP) {
                    if (selectedSettingColumnIndex > 0) {
                        pasteCommands.add(new SetKeywordCallArgumentCommand(selectedSetting,
                                selectedSettingColumnIndex - 1, valueToPaste));
                    }
                } else {
                    if (selectedSettingColumnIndex == 0) {
                        pasteCommands.add(new SetKeywordCallNameCommand(selectedSetting, valueToPaste));
                    } else {
                        pasteCommands.add(new SetKeywordCallArgumentCommand(selectedSetting,
                                selectedSettingColumnIndex - 1, valueToPaste));
                    }
                }
            }
        }

        private String getValueToPaste(final RobotSetting settingFromClipboard, final int clipboardSettingColumnIndex,
                final int columnCount) {
            String valueToPaste = "";

            List<String> arguments = settingFromClipboard.getArguments();
            if (settingFromClipboard.getGroup() == SettingsGroup.METADATA) {
                if (!arguments.isEmpty()) {
                    if (clipboardSettingColumnIndex == 0) {
                        valueToPaste = arguments.get(0);
                    } else if (clipboardSettingColumnIndex == 1 && arguments.size() > 0) {
                        valueToPaste = arguments.get(1);

                    } else if (clipboardSettingColumnIndex == 2) {
                        valueToPaste = settingFromClipboard.getComment();
                    }
                }
            } else {
                if (clipboardSettingColumnIndex == 0) {
                    valueToPaste = settingFromClipboard.getName();
                } else {
                    int argIndex = clipboardSettingColumnIndex - 1;
                    if (argIndex < arguments.size()) {
                        valueToPaste = arguments.get(argIndex);
                    } else if (clipboardSettingColumnIndex == columnCount - 1) {
                        valueToPaste = settingFromClipboard.getComment();
                    }
                }
            }
            return valueToPaste;
        }

        public List<Integer> findColumnsToPaste(final RobotSettingsSection section, final RobotSetting selectedSetting,
                final SelectionLayer selectionLayer) {
            final int settingTableIndex = TableHandlersSupport.findTableIndexOfSelectedSetting(section,
                    selectedSetting);
            final PositionCoordinate[] selectedCellPositions = selectionLayer.getSelectedCellPositions();
            final List<Integer> columns = new ArrayList<>();
            for (int i = 0; i < selectedCellPositions.length; i++) {
                if (selectedCellPositions[i].rowPosition == settingTableIndex) {
                    columns.add(selectedCellPositions[i].columnPosition);
                }
            }
            return columns;
        }
        
        private List<Integer> findCurrentClipboardSettingColumnsIndexes(final int currentRowIndex,
                final PositionCoordinateSerializer[] positionsCoordinates) {

            final List<Integer> clipboardSettingColumnsIndexes = new ArrayList<>();
            for (int i = 0; i < positionsCoordinates.length; i++) {
                if (positionsCoordinates[i].getRowPosition() == currentRowIndex) {
                    clipboardSettingColumnsIndexes.add(positionsCoordinates[i].getColumnPosition());
                }
            }
            return clipboardSettingColumnsIndexes;
        }

        private int calculateNextClipboardRowIndex(final int currentRowIndex,
                final PositionCoordinateSerializer[] positionsCoordinates) {

            int nextRowIndex = currentRowIndex;
            for (int i = 0; i < positionsCoordinates.length; i++) {
                if (positionsCoordinates[i].getRowPosition() > currentRowIndex) {
                    return positionsCoordinates[i].getRowPosition();
                }
            }
            return nextRowIndex;
        }

    }
}

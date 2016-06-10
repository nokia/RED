/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.settings.handler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.nebula.widgets.nattable.coordinate.PositionCoordinate;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.ui.ISources;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSetting;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSetting.SettingsGroup;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSettingsSection;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.DeleteSettingKeywordCallCommand;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.SetKeywordCallArgumentCommand;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.SetKeywordCallCommentCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotFormEditor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.SelectionLayerAccessor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.settings.GeneralSettingsModel;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.settings.handler.DeleteInSettingsTableHandler.E4DeleteInSettingsTableHandler;
import org.robotframework.red.commands.DIParameterizedHandler;
import org.robotframework.red.viewers.Selections;

public class DeleteInSettingsTableHandler extends DIParameterizedHandler<E4DeleteInSettingsTableHandler> {

    public DeleteInSettingsTableHandler() {
        super(E4DeleteInSettingsTableHandler.class);
    }

    public static class E4DeleteInSettingsTableHandler {

        @Execute
        public Object delete(final RobotEditorCommandsStack commandsStack,
                @Named(ISources.ACTIVE_EDITOR_NAME) final RobotFormEditor editor,
                @Named(Selections.SELECTION) final IStructuredSelection selection) {
            final SelectionLayerAccessor selectionLayerAccessor = editor.getSelectionLayerAccessor();
            final SelectionLayer selectionLayer = selectionLayerAccessor.getSelectionLayer();

            final List<RobotSetting> settings = Selections.getElements(selection, RobotSetting.class);

            if (!settings.isEmpty()) {

                final RobotSettingsSection section = settings.get(0).getParent();
                // final DeleteSettingKeywordCallCommand settingsDeleteCommand =
                // createSettingsDeleteCommand(section,
                // settings, selectionLayer);
                final Collection<EditorCommand> detailsDeletingCommands = createCommandsForDetailsRemoval(section,
                        settings, selectionLayer);

                for (final EditorCommand command : detailsDeletingCommands) {
                    commandsStack.execute(command);
                }
                // should deleting whole setting be done in delete whole row command ?
                // commandsStack.execute(settingsDeleteCommand);
            }
            return null;
        }

        private DeleteSettingKeywordCallCommand createSettingsDeleteCommand(final RobotSettingsSection section,
                final List<RobotSetting> settings, final SelectionLayer selectionLayer) {

            final int[] fullySelectedRows = selectionLayer.getFullySelectedRowPositions();
            final List<Integer> rows = new ArrayList<>();
            for (final int row : fullySelectedRows) {
                rows.add(row);
            }

            final List<RobotSetting> settingsToRemove = new ArrayList<>();

            if (!rows.isEmpty()) {
                for (final RobotSetting selectedSetting : settings) {

                    if (selectedSetting.getGroup() != SettingsGroup.NO_GROUP) {
                        int tableIndex = -1;
                        if (selectedSetting.getGroup() == SettingsGroup.METADATA) {
                            tableIndex = section.getMetadataSettings().indexOf(selectedSetting);
                        } else {
                            tableIndex = section.getImportSettings().indexOf(selectedSetting);
                        }
                        if (rows.contains(tableIndex)) {
                            settingsToRemove.add(selectedSetting);
                        }
                    }
                }
            }

            return new DeleteSettingKeywordCallCommand(settingsToRemove);
        }

        private Collection<EditorCommand> createCommandsForDetailsRemoval(final RobotSettingsSection section,
                final List<RobotSetting> settings, final SelectionLayer selectionLayer) {
            
            final List<EditorCommand> commands = new ArrayList<>();
            final PositionCoordinate[] selectedCellPositions = selectionLayer.getSelectedCellPositions();
            if (selectedCellPositions.length == 0) {
                return commands;
            }
            final int settingsTableColumnCount = selectionLayer.getColumnCount();

            for (final RobotSetting selectedSetting : settings) {
                int tableIndexOfSelectedSetting = findTableIndexOfSelectedSetting(section, selectedSetting);
                for (int i = 0; i < selectedCellPositions.length; i++) {
                    final PositionCoordinate selectedCell = selectedCellPositions[i];
                    if (tableIndexOfSelectedSetting == selectedCell.rowPosition) {
                        if (selectedCell.columnPosition < settingsTableColumnCount - 1) {
                            if (selectedSetting.getGroup() == SettingsGroup.METADATA) {
                                commands.add(new SetKeywordCallArgumentCommand(selectedSetting,
                                        selectedCell.columnPosition, ""));
                            } else if (selectedCell.columnPosition > 0) {
                                commands.add(new SetKeywordCallArgumentCommand(selectedSetting,
                                        selectedCell.columnPosition - 1, ""));
                            }
                        } else if (selectedCell.columnPosition == settingsTableColumnCount - 1) {
                            commands.add(new SetKeywordCallCommentCommand(selectedSetting, ""));
                        }
                    }
                }
            }

            return commands;
        }

        public int findTableIndexOfSelectedSetting(final RobotSettingsSection section,
                final RobotSetting selectedSetting) {
            if (selectedSetting.getGroup() == SettingsGroup.METADATA) {
                return section.getMetadataSettings().indexOf(selectedSetting);
            } else if (selectedSetting.getGroup() == SettingsGroup.NO_GROUP) {
                final Iterator<RobotElement> generalSettingsIterator = GeneralSettingsModel.fillSettingsMapping(section)
                        .values()
                        .iterator();
                int i = 0;
                while (generalSettingsIterator.hasNext()) {
                    if (selectedSetting.equals(generalSettingsIterator.next())) {
                        return i;
                    }
                    i++;
                }
            }
            return section.getImportSettings().indexOf(selectedSetting);
        }
    }
}

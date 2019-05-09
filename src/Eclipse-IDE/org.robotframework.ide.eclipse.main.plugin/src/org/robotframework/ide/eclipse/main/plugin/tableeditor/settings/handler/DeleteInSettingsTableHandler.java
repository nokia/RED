/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.settings.handler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.nebula.widgets.nattable.coordinate.PositionCoordinate;
import org.eclipse.ui.ISources;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSetting;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSetting.SettingsGroup;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.EmptyCommand;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.settings.SetSettingCommentCommand;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.settings.SetSettingArgumentCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotFormEditor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.SelectionLayerAccessor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.settings.handler.DeleteInSettingsTableHandler.E4DeleteInSettingsTableHandler;
import org.robotframework.red.commands.DIParameterizedHandler;
import org.robotframework.red.viewers.Selections;

public class DeleteInSettingsTableHandler extends DIParameterizedHandler<E4DeleteInSettingsTableHandler> {

    public DeleteInSettingsTableHandler() {
        super(E4DeleteInSettingsTableHandler.class);
    }

    public static class E4DeleteInSettingsTableHandler {

        @Execute
        public void delete(@Named(Selections.SELECTION) final IStructuredSelection selection,
                @Named(ISources.ACTIVE_EDITOR_NAME) final RobotFormEditor editor,
                final RobotEditorCommandsStack commandsStack) {

            final List<RobotSetting> settings = Selections.getElements(selection, RobotSetting.class);
            if (!settings.isEmpty()) {
                final List<EditorCommand> detailsDeletingCommands = createCommandsForDetailsRemoval(settings,
                        editor.getSelectionLayerAccessor());
                Collections.reverse(detailsDeletingCommands); // deleting must be started from the
                                                              // biggest column index

                final EditorCommand parentCommand = new EmptyCommand();
                for (final EditorCommand command : detailsDeletingCommands) {
                    command.setParent(parentCommand);
                    commandsStack.execute(command);
                }
            }
        }

        private List<EditorCommand> createCommandsForDetailsRemoval(final List<RobotSetting> settings,
                final SelectionLayerAccessor selectionLayerAccessor) {

            final List<EditorCommand> commands = new ArrayList<>();
            final PositionCoordinate[] selectedCellPositions = selectionLayerAccessor.getSelectedPositions();
            if (selectedCellPositions.length == 0) {
                return commands;
            }
            final int tableColumnCount = selectionLayerAccessor.getColumnCount();

            int selectedElemRow = 0;
            for (final RobotSetting element : settings) {
                selectedElemRow = selectionLayerAccessor.findNextSelectedElementRowIndex(selectedElemRow);

                final List<Integer> selectedColumnsIndexes = selectionLayerAccessor
                        .findSelectedColumnsIndexesByRowIndex(selectedElemRow);
                for (int i = 0; i < selectedColumnsIndexes.size(); i++) {
                    final EditorCommand command = getCommandForSelectedElement(element, selectedColumnsIndexes.get(i),
                            tableColumnCount);
                    if (command != null) {
                        commands.add(command);
                    }
                }
            }
            return commands;
        }

        private EditorCommand getCommandForSelectedElement(final RobotSetting selectedSetting, final int columnIndex,
                final int tableColumnCount) {
            if (columnIndex < tableColumnCount - 1) {
                if (selectedSetting.getGroup() == SettingsGroup.METADATA) {
                    return new SetSettingArgumentCommand(selectedSetting, columnIndex, null);
                } else if (columnIndex > 0) {
                    return new SetSettingArgumentCommand(selectedSetting, columnIndex - 1, null);
                }
            } else if (columnIndex == tableColumnCount - 1) {
                return new SetSettingCommentCommand(selectedSetting, null);
            }
            return null;
        }
    }
}

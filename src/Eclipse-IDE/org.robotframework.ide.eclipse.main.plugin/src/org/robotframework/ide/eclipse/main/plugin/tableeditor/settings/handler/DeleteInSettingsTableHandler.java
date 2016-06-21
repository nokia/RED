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
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.ui.ISources;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSetting;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSetting.SettingsGroup;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSettingsSection;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.SetKeywordCallArgumentCommand;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.SetKeywordCallCommentCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotFormEditor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.handler.TableHandlersSupport;
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

            final List<RobotSetting> settings = Selections.getElements(selection, RobotSetting.class);
            if (!settings.isEmpty()) {
                final List<EditorCommand> detailsDeletingCommands = createCommandsForDetailsRemoval(
                        settings.get(0).getParent(), settings, editor.getSelectionLayerAccessor().getSelectionLayer());
                Collections.reverse(detailsDeletingCommands); // deleting must be started from the biggest column index

                for (final EditorCommand command : detailsDeletingCommands) {
                    commandsStack.execute(command);
                }
            }
            
            return null;
        }

        private List<EditorCommand> createCommandsForDetailsRemoval(final RobotSettingsSection section,
                final List<RobotSetting> settings, final SelectionLayer selectionLayer) {

            final List<EditorCommand> commands = new ArrayList<>();
            final PositionCoordinate[] selectedCellPositions = selectionLayer.getSelectedCellPositions();
            if (selectedCellPositions.length == 0) {
                return commands;
            }
            final int settingsTableColumnCount = selectionLayer.getColumnCount();

            for (final RobotSetting selectedSetting : settings) {
                int tableIndexOfSelectedSetting = TableHandlersSupport.findTableIndexOfSelectedSetting(section,
                        selectedSetting);
                for (int i = 0; i < selectedCellPositions.length; i++) {
                    final PositionCoordinate selectedCell = selectedCellPositions[i];
                    if (tableIndexOfSelectedSetting == selectedCell.rowPosition) {
                        if (selectedCell.columnPosition < settingsTableColumnCount - 1) {
                            if (selectedSetting.getGroup() == SettingsGroup.METADATA) {
                                commands.add(new SetKeywordCallArgumentCommand(selectedSetting,
                                        selectedCell.columnPosition, null));
                            } else if (selectedCell.columnPosition > 0) {
                                commands.add(new SetKeywordCallArgumentCommand(selectedSetting,
                                        selectedCell.columnPosition - 1, null));
                            }
                        } else if (selectedCell.columnPosition == settingsTableColumnCount - 1) {
                            commands.add(new SetKeywordCallCommentCommand(selectedSetting, null));
                        }
                    }
                }
            }

            return commands;
        }
    }
}

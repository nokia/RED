/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.variables.handler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.nebula.widgets.nattable.coordinate.PositionCoordinate;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.ui.ISources;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariable;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariablesSection;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.variables.CleanVariableValueCommand;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.variables.SetVariableCommentCommand;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.variables.SetVariableNameCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotFormEditor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.SelectionLayerAccessor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.variables.handler.DeleteInVariableTableHandler.E4DeleteInVariableTableHandler;
import org.robotframework.red.commands.DIParameterizedHandler;
import org.robotframework.red.viewers.Selections;

public class DeleteInVariableTableHandler extends DIParameterizedHandler<E4DeleteInVariableTableHandler> {

    public DeleteInVariableTableHandler() {
        super(E4DeleteInVariableTableHandler.class);
    }

    public static class E4DeleteInVariableTableHandler {

        @Execute
        public void delete(final RobotEditorCommandsStack commandsStack,
                @Named(ISources.ACTIVE_EDITOR_NAME) final RobotFormEditor editor,
                @Named(Selections.SELECTION) final IStructuredSelection selection) {
            final SelectionLayerAccessor selectionLayerAccessor = editor.getSelectionLayerAccessor();
            final SelectionLayer selectionLayer = selectionLayerAccessor.getSelectionLayer();

            final RobotVariablesSection section = getSection(selection);
            final Collection<EditorCommand> detailsDeletingCommands = createCommandsForDetailsRemoval(section,
                    selectionLayer);

            for (final EditorCommand command : detailsDeletingCommands) {
                commandsStack.execute(command);
            }
        }

        private RobotVariablesSection getSection(final IStructuredSelection selection) {
            final List<RobotVariable> selectedVariables = Selections.getElements(selection, RobotVariable.class);
            final RobotVariablesSection section = selectedVariables.get(0).getParent();
            return section;
        }


        private Collection<EditorCommand> createCommandsForDetailsRemoval(final RobotVariablesSection section,
                final SelectionLayer selectionLayer) {
            final List<RobotVariable> variables = section.getChildren();

            // TODO : decide on behavior of fully selected rows; uncomment if change is needed
            // final List<RobotVariable> varsToRemove = new ArrayList<>();
            final List<EditorCommand> commands = new ArrayList<>();
            for (final PositionCoordinate cellPosition : selectionLayer.getSelectedCellPositions()) {

                final RobotVariable variable = variables.get(cellPosition.rowPosition);

                /*
                 * if (selectionLayer.isRowPositionFullySelected(cellPosition.rowPosition)) {
                 * varsToRemove.add(variable);
                 * } else
                 */if (cellPosition.columnPosition == 0) {
                    commands.add(new SetVariableNameCommand(variable, ""));
                } else if (cellPosition.columnPosition == 1) {
                    commands.add(new CleanVariableValueCommand(variable));
                } else if (cellPosition.columnPosition == 2) {
                    commands.add(new SetVariableCommentCommand(variable, ""));
                }
            }
            // if (!varsToRemove.isEmpty()) {
                // commands.add(new RemoveVariableCommand(varsToRemove));
            // }
            return commands;
        }
    }
}

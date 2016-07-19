/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.variables.handler;

import java.util.List;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.nebula.widgets.nattable.coordinate.PositionCoordinate;
import org.eclipse.ui.ISources;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariable;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotFormEditor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.dnd.PositionCoordinateTransfer.PositionCoordinateSerializer;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.dnd.RedClipboard;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.handler.TableHandlersSupport;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.variables.handler.CopyInVariableTableHandler.E4CopyInVariableTableHandler;
import org.robotframework.red.commands.DIParameterizedHandler;
import org.robotframework.red.viewers.Selections;

public class CopyInVariableTableHandler extends DIParameterizedHandler<E4CopyInVariableTableHandler> {

    public CopyInVariableTableHandler() {
        super(E4CopyInVariableTableHandler.class);
    }

    public static class E4CopyInVariableTableHandler {

        @Execute
        public boolean copy(@Named(ISources.ACTIVE_EDITOR_NAME) final RobotFormEditor editor,
                @Named(Selections.SELECTION) final IStructuredSelection selection, final RedClipboard clipboard) {

            final List<RobotVariable> variables = Selections.getElements(selection, RobotVariable.class);
            final PositionCoordinate[] selectedCellPositions = editor.getSelectionLayerAccessor()
                    .getSelectionLayer()
                    .getSelectedCellPositions();
            if (selectedCellPositions.length > 0 && !variables.isEmpty()) {
                final PositionCoordinateSerializer[] positionsCopy = TableHandlersSupport
                        .createSerializablePositionsCoordinates(selectedCellPositions);
                final RobotVariable[] variablesCopy = variables.toArray(new RobotVariable[0]);

                clipboard.insertContent(positionsCopy, variablesCopy);

                return true;
            }
            return false;
        }
    }
}

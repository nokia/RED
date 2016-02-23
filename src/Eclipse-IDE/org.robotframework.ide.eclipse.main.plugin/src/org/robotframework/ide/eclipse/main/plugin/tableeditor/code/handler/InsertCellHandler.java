/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.code.handler;

import java.util.List;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.ui.ISources;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.FocusedViewerAccessor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotFormEditor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.code.handler.InsertCellHandler.E4InsertCellHandler;
import org.robotframework.red.commands.DIParameterizedHandler;
import org.robotframework.red.viewers.Selections;
import org.robotframework.red.viewers.Viewers;

import com.google.common.base.Optional;

public class InsertCellHandler extends DIParameterizedHandler<E4InsertCellHandler> {

    public InsertCellHandler() {
        super(E4InsertCellHandler.class);
    }

    public static class E4InsertCellHandler {

        @Execute
        public Object insertCell(final RobotEditorCommandsStack commandsStack,
                @Named(ISources.ACTIVE_EDITOR_NAME) final RobotFormEditor editor,
                @Named(Selections.SELECTION) final IStructuredSelection selection) {

            final FocusedViewerAccessor viewerAccessor = editor.getFocusedViewerAccessor();
            final ColumnViewer viewer = viewerAccessor.getViewer();
            final ViewerCell focusedCell = viewerAccessor.getFocusedCell();
            final int index = Viewers.createOrderIndexToPositionIndex(viewer, focusedCell.getColumnIndex());

            viewerAccessor.getColumnsManager().addColumn(viewerAccessor.getViewer());

            final Optional<RobotElement> element = Selections.getOptionalFirstElement(selection, RobotElement.class);
            if (!element.isPresent()) {
                return null;
            }

            final List<? extends EditorCommand> commands = new CodeAttributesCommandsProvider()
                    .provideInsertAttributeCommands(element.get(), index);

            for (final EditorCommand command : commands) {
                commandsStack.execute(command);
            }
            return null;
        }
    }
}

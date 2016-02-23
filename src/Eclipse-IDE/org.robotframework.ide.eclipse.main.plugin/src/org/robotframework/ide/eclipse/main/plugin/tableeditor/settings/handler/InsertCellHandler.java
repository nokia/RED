/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.settings.handler;

import javax.inject.Named;

import org.eclipse.core.commands.IHandler;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.ui.ISources;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSetting;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSetting.SettingsGroup;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.InsertKeywordCallArgumentCommand;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.SetKeywordCallNameCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.FocusedViewerAccessor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotFormEditor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.settings.handler.InsertCellHandler.E4InsertCellHandler;
import org.robotframework.red.commands.DIParameterizedHandler;
import org.robotframework.red.viewers.Selections;
import org.robotframework.red.viewers.Viewers;

import com.google.common.base.Optional;

public class InsertCellHandler extends DIParameterizedHandler<E4InsertCellHandler> implements IHandler {

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

            final Optional<RobotSetting> element = Selections.getOptionalFirstElement(selection, RobotSetting.class);
            if (!element.isPresent()) {
                return null;
            }

            final RobotSetting setting = element.get();
            if (index == 0 && setting.getGroup() == SettingsGroup.NO_GROUP) {
                commandsStack.execute(new InsertKeywordCallArgumentCommand(setting, 0, ""));
            } else if (index == 0) {
                final String currentName = setting.getName();
                commandsStack.execute(new SetKeywordCallNameCommand(setting, ""));
                commandsStack.execute(new InsertKeywordCallArgumentCommand(setting, 0, currentName));
            } else {
                commandsStack.execute(new InsertKeywordCallArgumentCommand(setting, index - 1, ""));
            }            
            return null;
        }
    }
}

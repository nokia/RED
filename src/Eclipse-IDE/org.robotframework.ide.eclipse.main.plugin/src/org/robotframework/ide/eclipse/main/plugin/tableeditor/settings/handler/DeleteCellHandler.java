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
import org.robotframework.ide.eclipse.main.plugin.model.cmd.RemoveKeywordCallArgumentCommand;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.SetKeywordCallNameCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.FocusedViewerAccessor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotFormEditor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.settings.handler.DeleteCellHandler.E4DeleteCellHandler;
import org.robotframework.red.commands.DIParameterizedHandler;
import org.robotframework.red.viewers.Selections;
import org.robotframework.red.viewers.Viewers;

import com.google.common.base.Optional;

public class DeleteCellHandler extends DIParameterizedHandler<E4DeleteCellHandler> implements IHandler {

    public DeleteCellHandler() {
        super(E4DeleteCellHandler.class);
    }

    public static class E4DeleteCellHandler {

        @Execute
        public Object deleteCell(final RobotEditorCommandsStack commandsStack,
                @Named(ISources.ACTIVE_EDITOR_NAME) final RobotFormEditor editor,
                @Named(Selections.SELECTION) final IStructuredSelection selection) {

            final FocusedViewerAccessor viewerAccessor = editor.getFocusedViewerAccessor();
            final ColumnViewer viewer = viewerAccessor.getViewer();
            final ViewerCell focusedCell = viewerAccessor.getFocusedCell();
            final int index = Viewers.createOrderIndexToPositionIndex(viewer, focusedCell.getColumnIndex());

            viewerAccessor.getColumnsManager().removeColumn(viewer);

            final Optional<RobotSetting> element = Selections.getOptionalFirstElement(selection, RobotSetting.class);
            if (!element.isPresent()) {
                return null;
            }

            final RobotSetting setting = element.get();
            if (index == 0 && setting.getGroup() == SettingsGroup.NO_GROUP) {
                commandsStack.execute(new RemoveKeywordCallArgumentCommand(setting, 0));
            } else if (index == 0) {
                final String name = setting.getArguments().isEmpty() ? "" : setting.getArguments().get(0);
                commandsStack.execute(new SetKeywordCallNameCommand(setting, name));
                commandsStack.execute(new RemoveKeywordCallArgumentCommand(setting, 0));
            } else {
                commandsStack.execute(new RemoveKeywordCallArgumentCommand(setting, index - 1));
            }
            return null;
        }
    }
}

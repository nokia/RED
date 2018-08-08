/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.tasks.handler;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.ISources;
import org.robotframework.ide.eclipse.main.plugin.model.RobotTask;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotFormEditor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.code.handler.E4CopyInCodeHoldersTableHandler;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.dnd.RedClipboard;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.tasks.handler.CopyInTasksTableHandler.E4CopyInTasksTableHandler;
import org.robotframework.red.commands.DIParameterizedHandler;
import org.robotframework.red.viewers.Selections;

public class CopyInTasksTableHandler extends DIParameterizedHandler<E4CopyInTasksTableHandler> {

    public CopyInTasksTableHandler() {
        super(E4CopyInTasksTableHandler.class);
    }

    public static class E4CopyInTasksTableHandler extends E4CopyInCodeHoldersTableHandler {

        @Execute
        public boolean copyContent(final @Named(ISources.ACTIVE_EDITOR_NAME) RobotFormEditor editor,
                @Named(Selections.SELECTION) final IStructuredSelection selection, final RedClipboard clipboard) {

            return copyContent(editor.getSelectionLayerAccessor(), selection, clipboard, RobotTask.class);
        }
    }
}

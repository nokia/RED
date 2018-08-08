/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.tasks.handler;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.code.handler.DeleteCodeHoldersHandler.E4DeleteHoldersHandler;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.dnd.RedClipboard;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.tasks.handler.CopyTasksHandler.E4CopyTasksHandler;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.tasks.handler.CutTasksHandler.E4CutTasksHandler;
import org.robotframework.red.commands.DIParameterizedHandler;
import org.robotframework.red.viewers.Selections;

public class CutTasksHandler extends DIParameterizedHandler<E4CutTasksHandler> {

    public CutTasksHandler() {
        super(E4CutTasksHandler.class);
    }

    public static class E4CutTasksHandler {

        @Execute
        public void cutTasks(@Named(Selections.SELECTION) final IStructuredSelection selection,
                final RedClipboard clipboard, final RobotEditorCommandsStack commandsStack) {
            
            final boolean copiedToClipboard = new E4CopyTasksHandler().copyTasks(selection, clipboard);
            if (copiedToClipboard) {
                new E4DeleteHoldersHandler().deleteHoldersAndCalls(commandsStack, selection);
            }
        }
    }
}

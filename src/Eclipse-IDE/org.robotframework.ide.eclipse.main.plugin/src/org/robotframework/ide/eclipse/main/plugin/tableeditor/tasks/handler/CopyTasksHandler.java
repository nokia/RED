/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.tasks.handler;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotTask;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.code.handler.E4CopyCodeHoldersHandler;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.dnd.RedClipboard;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.tasks.handler.CopyTasksHandler.E4CopyTasksHandler;
import org.robotframework.red.commands.DIParameterizedHandler;
import org.robotframework.red.viewers.Selections;

public class CopyTasksHandler extends DIParameterizedHandler<E4CopyTasksHandler> {

    public CopyTasksHandler() {
        super(E4CopyTasksHandler.class);
    }

    public static class E4CopyTasksHandler extends E4CopyCodeHoldersHandler {

        @Execute
        public boolean copyTasks(@Named(Selections.SELECTION) final IStructuredSelection selection,
                final RedClipboard clipboard) {

            return copyCodeHolders(selection, clipboard, RobotTask.class);
        }
    }
}

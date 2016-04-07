/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.editor.handlers;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfigEvents;
import org.robotframework.ide.eclipse.main.plugin.project.editor.RedProjectEditorInput;
import org.robotframework.ide.eclipse.main.plugin.project.editor.handlers.DeleteClassPathHandler.E4DeleteClassPathHandler;
import org.robotframework.red.commands.DIParameterizedHandler;
import org.robotframework.red.viewers.Selections;


public class DeleteClassPathHandler extends DIParameterizedHandler<E4DeleteClassPathHandler> {

    public DeleteClassPathHandler() {
        super(E4DeleteClassPathHandler.class);
    }

    public static class E4DeleteClassPathHandler extends E4DeleteSearchPathHandler {

        @Execute
        public void deleteSearchPaths(@Named(value = Selections.SELECTION) final IStructuredSelection selection,
                final RedProjectEditorInput input, final IEventBroker eventBroker) {
            super.deleteSearchPaths(input.getProjectConfiguration().getClassPath(), selection, eventBroker,
                    RobotProjectConfigEvents.ROBOT_CONFIG_CLASSPATH_STRUCTURE_CHANGED);
        }
    }
}

/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.editor.handlers;

import java.util.List;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig.ReferencedVariableFile;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfigEvents;
import org.robotframework.ide.eclipse.main.plugin.project.editor.RedProjectEditorInput;
import org.robotframework.ide.eclipse.main.plugin.project.editor.handlers.DeleteReferencedVariableFileHandler.E4DeleteVariableFileHandler;
import org.robotframework.red.commands.DIParameterizedHandler;
import org.robotframework.red.viewers.Selections;

public class DeleteReferencedVariableFileHandler extends DIParameterizedHandler<E4DeleteVariableFileHandler> {

    public DeleteReferencedVariableFileHandler() {
        super(E4DeleteVariableFileHandler.class);
    }

    public static class E4DeleteVariableFileHandler {

        @Execute
        public Object deleteFile(@Named(Selections.SELECTION) final IStructuredSelection selection,
                final RedProjectEditorInput input, final IEventBroker eventBroker) {
            final List<ReferencedVariableFile> varFiles = Selections.getElements(selection,
                    ReferencedVariableFile.class);
            input.getProjectConfiguration().removeReferencedVariableFiles(varFiles);

            eventBroker.send(RobotProjectConfigEvents.ROBOT_CONFIG_VAR_FILE_STRUCTURE_CHANGED,
                    input.getProjectConfiguration().getReferencedVariableFiles());

            return null;
        }
    }
}

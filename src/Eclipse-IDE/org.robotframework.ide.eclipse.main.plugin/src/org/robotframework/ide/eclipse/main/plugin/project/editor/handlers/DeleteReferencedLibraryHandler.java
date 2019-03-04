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
import org.rf.ide.core.project.RobotProjectConfig.ReferencedLibrary;
import org.robotframework.ide.eclipse.main.plugin.project.RedProjectConfigEventData;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfigEvents;
import org.robotframework.ide.eclipse.main.plugin.project.editor.RedProjectEditorInput;
import org.robotframework.ide.eclipse.main.plugin.project.editor.handlers.DeleteReferencedLibraryHandler.E4DeleteReferencedLibraryHandler;
import org.robotframework.red.commands.DIParameterizedHandler;
import org.robotframework.red.viewers.Selections;


/**
 * @author Michal Anglart
 *
 */
public class DeleteReferencedLibraryHandler extends DIParameterizedHandler<E4DeleteReferencedLibraryHandler> {

    public DeleteReferencedLibraryHandler() {
        super(E4DeleteReferencedLibraryHandler.class);
    }

    public static class E4DeleteReferencedLibraryHandler {

        @Execute
        public void deleteReferencedLibraries(@Named(Selections.SELECTION) final IStructuredSelection selection,
                final RedProjectEditorInput input, final IEventBroker eventBroker) {
            final List<ReferencedLibrary> libraries = Selections.getElements(selection, ReferencedLibrary.class);
            final boolean removed = input.getProjectConfiguration().removeReferencedLibraries(libraries);
            if (removed) {
                eventBroker.send(RobotProjectConfigEvents.ROBOT_CONFIG_LIBRARIES_STRUCTURE_CHANGED,
                        new RedProjectConfigEventData<>(input.getFile(), libraries));
                input.getRobotProject().unregisterWatchingOnReferencedLibraries(libraries);
            }
        }
    }
}

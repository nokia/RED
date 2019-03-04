/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.editor.handlers;

import java.util.List;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.rf.ide.core.project.RobotProjectConfig.RemoteLocation;
import org.robotframework.ide.eclipse.main.plugin.project.RedProjectConfigEventData;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfigEvents;
import org.robotframework.ide.eclipse.main.plugin.project.editor.RedProjectEditorInput;
import org.robotframework.ide.eclipse.main.plugin.project.editor.handlers.DeleteRemoteLocationHandler.E4DeleteRemoteLocationHandler;
import org.robotframework.red.commands.DIParameterizedHandler;
import org.robotframework.red.viewers.Selections;


public class DeleteRemoteLocationHandler extends DIParameterizedHandler<E4DeleteRemoteLocationHandler> {

    public DeleteRemoteLocationHandler() {
        super(E4DeleteRemoteLocationHandler.class);
    }

    public static class E4DeleteRemoteLocationHandler {

        @Execute
        public void deleteRemoteLocations(@Named(Selections.SELECTION) final IStructuredSelection selection,
                final RedProjectEditorInput input, final IEventBroker eventBroker) {
            final List<RemoteLocation> locations = Selections.getElements(selection, RemoteLocation.class);
            final boolean removed = input.getProjectConfiguration().removeRemoteLocations(locations);
            if (removed) {
                eventBroker.send(RobotProjectConfigEvents.ROBOT_CONFIG_REMOTE_STRUCTURE_CHANGED,
                        new RedProjectConfigEventData<>(input.getFile(), locations));
            }
        }
    }
}

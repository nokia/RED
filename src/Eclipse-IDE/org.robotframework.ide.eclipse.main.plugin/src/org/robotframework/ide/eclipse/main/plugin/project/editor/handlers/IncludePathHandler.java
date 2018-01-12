/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.editor.handlers;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Named;

import org.eclipse.core.runtime.IPath;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.robotframework.ide.eclipse.main.plugin.project.RedProjectConfigEventData;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfigEvents;
import org.robotframework.ide.eclipse.main.plugin.project.editor.RedProjectEditorInput;
import org.robotframework.ide.eclipse.main.plugin.project.editor.handlers.IncludePathHandler.E4IncludePathHandler;
import org.robotframework.ide.eclipse.main.plugin.project.editor.validation.ProjectTreeElement;
import org.robotframework.red.commands.DIParameterizedHandler;
import org.robotframework.red.viewers.Selections;

public class IncludePathHandler extends DIParameterizedHandler<E4IncludePathHandler> {

    public IncludePathHandler() {
        super(E4IncludePathHandler.class);
    }

    public static class E4IncludePathHandler {

        @Execute
        public void include(@Named(Selections.SELECTION) final IStructuredSelection selection,
                final RedProjectEditorInput input, final IEventBroker eventBroker) {

            final List<ProjectTreeElement> locationsToInclude = Selections.getElements(selection,
                    ProjectTreeElement.class);

            for (final ProjectTreeElement locationToInclude : locationsToInclude) {
                final IPath toRemove = locationToInclude.getPath();
                input.getProjectConfiguration().removeExcludedPath(toRemove.toPortableString());
            }

            final List<IPath> includedPaths = locationsToInclude.stream().map(ProjectTreeElement::getPath).collect(
                    Collectors.toList());
            final RedProjectConfigEventData<Collection<IPath>> eventData = new RedProjectConfigEventData<>(
                    input.getRobotProject().getConfigurationFile(), includedPaths);
            eventBroker.send(RobotProjectConfigEvents.ROBOT_CONFIG_VALIDATION_EXCLUSIONS_STRUCTURE_CHANGED, eventData);
            eventBroker.send(RobotProjectConfigEvents.ROBOT_CONFIG_VALIDATION_EXCLUSIONS_STRUCTURE_CHANGED,
                    locationsToInclude);
        }
    }

}

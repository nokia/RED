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
import org.robotframework.ide.eclipse.main.plugin.project.editor.handlers.ExcludePathHandler.E4ExcludePathHandler;
import org.robotframework.ide.eclipse.main.plugin.project.editor.validation.ProjectTreeElement;
import org.robotframework.red.commands.DIParameterizedHandler;
import org.robotframework.red.viewers.Selections;

public class ExcludePathHandler extends DIParameterizedHandler<E4ExcludePathHandler> {

    public ExcludePathHandler() {
        super(E4ExcludePathHandler.class);
    }

    public static class E4ExcludePathHandler {

        @Execute
        public void exclude(@Named(Selections.SELECTION) final IStructuredSelection selection,
                final RedProjectEditorInput input, final IEventBroker eventBroker) {

            final List<ProjectTreeElement> locationsToExclude = Selections.getElements(selection,
                    ProjectTreeElement.class);

            for (final ProjectTreeElement locationToExclude : locationsToExclude) {
                final IPath toRemove = locationToExclude.getPath();
                input.getProjectConfiguration().addExcludedPath(toRemove.toPortableString());
            }

            final List<IPath> excludedPaths = locationsToExclude.stream().map(ProjectTreeElement::getPath).collect(
                    Collectors.toList());
            final RedProjectConfigEventData<Collection<IPath>> eventData = new RedProjectConfigEventData<>(
                    input.getRobotProject().getConfigurationFile(), excludedPaths);
            eventBroker.send(RobotProjectConfigEvents.ROBOT_CONFIG_VALIDATION_EXCLUSIONS_STRUCTURE_CHANGED, eventData);
        }
    }
}

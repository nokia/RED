/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.navigator.handlers;

import java.util.List;

import javax.inject.Named;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.rf.ide.core.project.RobotProjectConfig;
import org.robotframework.ide.eclipse.main.plugin.navigator.handlers.ExcludePathForValidationHandler.E4ExcludePathForValidationHandler;
import org.robotframework.red.commands.DIParameterizedHandler;
import org.robotframework.red.viewers.Selections;

public class ExcludePathForValidationHandler extends DIParameterizedHandler<E4ExcludePathForValidationHandler> {

    public ExcludePathForValidationHandler() {
        super(E4ExcludePathForValidationHandler.class);
    }

    public static class E4ExcludePathForValidationHandler extends ChangeExclusionHandler {

        @Execute
        public void changeExclusion(final IEventBroker eventBroker,
                final @Named(Selections.SELECTION) IStructuredSelection selection) {
            final List<IResource> selectedResources = Selections.getAdaptableElements(selection, IResource.class);
            final boolean wasChanged = changeExclusion(eventBroker, selectedResources);
            if (wasChanged) {
                removeMarkers(selectedResources);
            }
        }

        @Override
        protected boolean changeExclusion(final RobotProjectConfig config, final IPath pathToChange) {
            return config.addExcludedPath(pathToChange.toPortableString());
        }

        private void removeMarkers(final List<IResource> selectedResources) {
            for (final IResource resource : selectedResources) {
                try {
                    resource.deleteMarkers(null, true, IResource.DEPTH_INFINITE);
                } catch (final CoreException e) {
                    throw new IllegalStateException(
                            "Unable to remove problems from " + resource.getFullPath().toOSString());
                }
            }
        }
    }
}

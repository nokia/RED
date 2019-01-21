/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.navigator.handlers;

import java.util.List;

import javax.inject.Named;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.rf.ide.core.project.RobotProjectConfig;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.navigator.handlers.IncludePathForValidationHandler.E4IncludePathForValidationHandler;
import org.robotframework.red.commands.DIParameterizedHandler;
import org.robotframework.red.viewers.Selections;

public class IncludePathForValidationHandler extends DIParameterizedHandler<E4IncludePathForValidationHandler> {

    public IncludePathForValidationHandler() {
        super(E4IncludePathForValidationHandler.class);
    }

    public static class E4IncludePathForValidationHandler extends ChangeExclusionHandler {

        private static final long REVALIDATE_JOB_DELAY = 2000;

        @Execute
        public void changeExclusion(final IEventBroker eventBroker,
                final @Named(Selections.SELECTION) IStructuredSelection selection) {
            final List<IResource> selectedResources = Selections.getAdaptableElements(selection, IResource.class);
            changeExclusion(eventBroker, selectedResources);
            scheduleRevalidation(selectedResources);
        }

        @Override
        protected void changeExclusion(final RobotProjectConfig config, final IPath pathToChange) {
            config.removeExcludedPath(pathToChange.toPortableString());
        }

        private void scheduleRevalidation(final List<IResource> selectedResources) {
            if (!RedPlugin.getDefault().getPreferences().isValidationTurnedOff()) {
                RevalidateSelectionHandler.revalidate(selectedResources, REVALIDATE_JOB_DELAY);
            }
        }
    }
}

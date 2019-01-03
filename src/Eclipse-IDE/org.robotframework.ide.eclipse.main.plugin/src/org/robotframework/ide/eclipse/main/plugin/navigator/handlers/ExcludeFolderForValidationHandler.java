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
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.statushandlers.StatusManager;
import org.rf.ide.core.project.RobotProjectConfig;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.navigator.handlers.ExcludeFolderForValidationHandler.E4ExcludeFolderForValidationHandler;
import org.robotframework.red.commands.DIParameterizedHandler;
import org.robotframework.red.viewers.Selections;

public class ExcludeFolderForValidationHandler extends DIParameterizedHandler<E4ExcludeFolderForValidationHandler> {

    public ExcludeFolderForValidationHandler() {
        super(E4ExcludeFolderForValidationHandler.class);
    }

    public static class E4ExcludeFolderForValidationHandler extends ChangeExclusionHandler {

        @Execute
        public void changeExclusion(final IEventBroker eventBroker,
                final @Named(Selections.SELECTION) IStructuredSelection selection) {
            final List<IResource> selectedResources = Selections.getAdaptableElements(selection, IResource.class);
            try {
                changeExclusion(eventBroker, selectedResources);
                removeMarkers(selectedResources);

            } catch (final UnsupportedOperationException e) {
                final Status status = new Status(IStatus.ERROR, RedPlugin.PLUGIN_ID,
                        "Cannot exclude selected resource in project configuration. The file 'red.xml' is missing or corrupted");
                StatusManager.getManager().handle(status, StatusManager.SHOW);
            }
        }

        @Override
        protected void changeExclusion(final RobotProjectConfig config, final IPath pathToChange) {
            config.addExcludedPath(pathToChange.toPortableString());
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

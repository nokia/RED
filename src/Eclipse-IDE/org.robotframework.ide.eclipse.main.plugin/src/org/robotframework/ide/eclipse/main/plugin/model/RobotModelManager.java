/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.ui.PlatformUI;
import org.rf.ide.core.watcher.RedFileWatcher;

public final class RobotModelManager {

    private static class ModelSynchronizer implements IResourceChangeListener {

        @Override
        public void resourceChanged(final IResourceChangeEvent event) {
            List<RobotElementChange> changes = null;
            if (event.getType() == IResourceChangeEvent.PRE_CLOSE) {
                changes = InstanceHolder.INSTANCE.getModel().removeProject((IProject) event.getResource());

                notifyAboutChanges(changes);
            } else if (event.getType() == IResourceChangeEvent.POST_CHANGE) {
                changes = InstanceHolder.INSTANCE.getModel().synchronizeChanges(event.getDelta());

                notifyAboutChanges(changes);
            } else if (event.getType() == IResourceChangeEvent.PRE_DELETE) {
                RedFileWatcher.getInstance().closeWatchService();
            }
        }

        private void notifyAboutChanges(final List<RobotElementChange> changes) {
            final IEventBroker eventBroker = PlatformUI.getWorkbench().getService(IEventBroker.class);

            for (final RobotElementChange change : changes) {
                eventBroker.post(RobotModelEvents.EXTERNAL_MODEL_CHANGE, change);
            }
        }
    }

    private static class InstanceHolder {
        private static final RobotModelManager INSTANCE = new RobotModelManager();
    }

    private RobotModel model = new RobotModel();
    private final IResourceChangeListener resourceListener;

    private RobotModelManager() {
        if (PlatformUI.isWorkbenchRunning()) {
            resourceListener = new ModelSynchronizer();
            ResourcesPlugin.getWorkspace().addResourceChangeListener(resourceListener);
        } else {
            resourceListener = null;
        }
    }

    public static RobotModelManager getInstance() {
        return InstanceHolder.INSTANCE;
    }

    public RobotProject createProject(final IProject project) {
        return model.createRobotProject(project);
    }

    public RobotSuiteFile createSuiteFile(final IFile file) {
        return model.createSuiteFile(file);
    }

    public RobotModel getModel() {
        return model;
    }

    public void dispose() {
        if (PlatformUI.isWorkbenchRunning()) {
            ResourcesPlugin.getWorkspace().removeResourceChangeListener(resourceListener);
        }
        model = new RobotModel();
    }

}

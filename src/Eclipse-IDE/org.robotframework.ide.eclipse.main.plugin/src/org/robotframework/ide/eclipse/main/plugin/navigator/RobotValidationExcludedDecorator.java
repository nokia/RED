/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.navigator;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.rf.ide.core.project.RobotProjectConfig;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.red.graphics.ColorsManager;

public class RobotValidationExcludedDecorator implements ILightweightLabelDecorator {

    public static final String ID = "org.robotframework.red.decorator.resource.validationExcluded";

    @Override
    public void addListener(final ILabelProviderListener listener) {
        // nothing to do here
    }

    @Override
    public void dispose() {
        // nothing to do here
    }

    @Override
    public boolean isLabelProperty(final Object element, final String property) {
        return false;
    }

    @Override
    public void removeListener(final ILabelProviderListener listener) {
        // nothing to do here
    }

    private void removeMarkers(IResource resource) {
        try {
            resource.deleteMarkers(null, true, IResource.DEPTH_INFINITE);
        } catch (CoreException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void decorate(final Object element, final IDecoration decoration) {
        final IResource resource = RedPlugin.getAdapter(element, IResource.class);
        if (resource != null && (resource.getType() == IResource.FILE || resource.getType() == IResource.FOLDER)) {
            final RobotProject robotProject = RedPlugin.getModelManager()
                    .getModel()
                    .createRobotProject(resource.getProject());
            RobotProjectConfig config = robotProject.getOpenedProjectConfig();
            if (config == null) {
                config = robotProject.getRobotProjectConfig();
            }

            if (config != null
                    && config.isExcludedFromValidation(resource.getProjectRelativePath().toPortableString())) {
                decoration.addSuffix(" [excluded]");
                decoration.setForegroundColor(ColorsManager.getColor(200, 200, 200));
                removeMarkers(resource);
            }
        }
    }

}

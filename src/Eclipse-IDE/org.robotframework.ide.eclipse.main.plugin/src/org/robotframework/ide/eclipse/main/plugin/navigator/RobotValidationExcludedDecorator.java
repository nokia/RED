/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.navigator;

import org.eclipse.core.resources.IFolder;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig;
import org.robotframework.red.graphics.ColorsManager;


public class RobotValidationExcludedDecorator implements ILightweightLabelDecorator {

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

    @Override
    public void decorate(final Object element, final IDecoration decoration) {
        if (element instanceof IFolder) {
            final IFolder folder = (IFolder) element;
            final RobotProject robotProject = RedPlugin.getModelManager()
                    .getModel()
                    .createRobotProject(folder.getProject());
            final RobotProjectConfig config = robotProject.getRobotProjectConfig();

            if (config != null && config.isExcludedFromValidation(folder.getProjectRelativePath())) {
                decoration.addPrefix("[excluded] ");
                decoration.setForegroundColor(ColorsManager.getColor(200, 200, 200));
            }
        }
    }

}

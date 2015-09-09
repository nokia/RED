/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see licence.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.navigator;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.project.RobotSuiteFileDescriber;


public class RobotFilesDecorator implements ILightweightLabelDecorator {

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
        if (element instanceof IFile && RobotSuiteFileDescriber.isResourceFile((IFile) element)) {
            decoration.addOverlay(RedImages.getRobotResourceDecoratorImage());
        } else if (element instanceof IFile && RobotSuiteFileDescriber.isInitializationFile((IFile) element)) {
            decoration.addOverlay(RedImages.getRobotInitFileDecoratorImage());
        }
    }

}

/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.propertytester;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;


public class RedXmlForNavigatorPropertyTester extends PropertyTester {

    @VisibleForTesting static final String IS_EXCLUDED = "isExcluded";
    @VisibleForTesting static final String IS_INCLUDED = "isIncluded";
    @VisibleForTesting static final String IS_INTERNAL_FOLDER = "isInternalFolder";

    @Override
    public boolean test(final Object receiver, final String property, final Object[] args, final Object expectedValue) {
        Preconditions.checkArgument(receiver instanceof IResource,
                "Property tester is unable to test properties of " + receiver.getClass().getName()
                + ". It should be used with " + IResource.class.getName());

        if (expectedValue instanceof Boolean) {
            return testProperty((IResource) receiver, property, ((Boolean) expectedValue).booleanValue());
        }
        return false;
    }

    private boolean testProperty(final IResource projectElement, final String property,
            final boolean expected) {
        if (IS_INTERNAL_FOLDER.equals(property)) {
            return projectElement instanceof IFolder == expected;
        } else if (IS_INCLUDED.equals(property)) {
            return !isExcluded(projectElement) == expected;
        } else if (IS_EXCLUDED.equals(property)) {
            return isExcluded(projectElement) == expected;
        }
        return false;
    }

    private boolean isExcluded(final IResource projectElement) {
        final RobotProject robotProject = RedPlugin.getModelManager()
                .getModel()
                .createRobotProject(projectElement.getProject());
        RobotProjectConfig config = robotProject.getOpenedProjectConfig();
        if (config == null) {
            config = robotProject.getRobotProjectConfig();
        }
        return config.isExcludedFromValidation(projectElement.getProjectRelativePath());
    }

}

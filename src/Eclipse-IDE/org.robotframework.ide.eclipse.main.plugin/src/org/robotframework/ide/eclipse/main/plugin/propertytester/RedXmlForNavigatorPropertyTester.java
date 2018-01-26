/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.propertytester;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.rf.ide.core.project.RobotProjectConfig;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.project.ExcludedResources;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;

public class RedXmlForNavigatorPropertyTester extends PropertyTester {

    @VisibleForTesting
    static final String IS_EXCLUDED = "isExcluded";

    @VisibleForTesting
    static final String IS_INTERNAL_FOLDER = "isInternalFolder";

    @VisibleForTesting
    static final String IS_FILE = "isFile";

    @VisibleForTesting
    static final String PARENT_EXCLUDED = "isExcludedViaInheritance";

    @VisibleForTesting
    static final String IS_PROJECT = "isProject";

    @VisibleForTesting
    static final String IS_HIDDEN = "isHidden";

    @Override
    public boolean test(final Object receiver, final String property, final Object[] args, final Object expectedValue) {
        Preconditions.checkArgument(receiver instanceof IResource, "Property tester is unable to test properties of "
                + receiver.getClass().getName() + ". It should be used with " + IResource.class.getName());

        if (expectedValue instanceof Boolean) {
            return testProperty((IResource) receiver, property, ((Boolean) expectedValue).booleanValue());
        }
        return false;
    }

    private boolean testProperty(final IResource projectElement, final String property, final boolean expected) {
        if (IS_INTERNAL_FOLDER.equals(property)) {
            return projectElement instanceof IFolder == expected;
        } else if (IS_EXCLUDED.equals(property)) {
            final RobotProjectConfig config = getConfig(projectElement);
            return isExcluded(projectElement, config) == expected;
        } else if (IS_FILE.equals(property)) {
            return projectElement instanceof IFile == expected;
        } else if (PARENT_EXCLUDED.equals(property)) {
            return isExcludedViaInheritance(projectElement) == expected;
        } else if (IS_PROJECT.equals(property)) {
            return projectElement instanceof IProject == expected;
        } else if (IS_HIDDEN.equals(property)) {
            return ExcludedResources.isHiddenInEclipse(projectElement) == expected;
        } else {
            return false;
        }
    }

    private boolean isExcluded(final IResource projectElement, final RobotProjectConfig config) {
        if (config != null) {
            return config.isExcludedFromValidation(projectElement.getProjectRelativePath().toPortableString());
        }
        return false;
    }

    private RobotProjectConfig getConfig(final IResource projectElement) {
        final RobotProject robotProject = RedPlugin.getModelManager().getModel().createRobotProject(
                projectElement.getProject());
        RobotProjectConfig config = robotProject.getOpenedProjectConfig();
        if (config == null) {
            config = robotProject.getRobotProjectConfig();
        }
        return config;
    }

    private boolean isExcludedViaInheritance(final IResource projectElement) {
        IResource resource = projectElement;
        IPath path = resource.getProjectRelativePath();
        final RobotProjectConfig config = getConfig(resource);
        while (path.segmentCount() > 0) {
            resource = resource.getParent();
            if (isExcluded(resource, config)) {
                return true;
            }
            path = resource.getProjectRelativePath();
        }
        return false;
    }

}

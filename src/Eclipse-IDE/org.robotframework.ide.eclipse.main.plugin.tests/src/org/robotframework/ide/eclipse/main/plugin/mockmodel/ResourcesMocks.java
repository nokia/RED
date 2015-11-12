/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.mockmodel;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectNature;

/**
 * @author Michal Anglart
 *
 */
public class ResourcesMocks {

    public static IFile prepareSuiteInitMockFile() {
        final IProject project = prepareRobotMockProject();

        final IFile file = mock(IFile.class);
        when(file.exists()).thenReturn(true);
        when(file.getName()).thenReturn("__init__.robot");
        when(file.getProject()).thenReturn(project);
        return file;
    }

    public static IFile prepareSuiteMockFile() {
        return null;

    }

    public static IFile prepareResourceMockFile() {
        return null;
    }

    public static IProject prepareRobotMockProject() {
        try {
            final IProject project = mock(IProject.class);
            when(project.hasNature(RobotProjectNature.ROBOT_NATURE)).thenReturn(true);
            return project;
        } catch (final CoreException e) {
            throw new IllegalStateException("Unable to create mock project");
        }
    }

}

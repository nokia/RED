/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project;

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.rf.ide.core.project.RobotProjectConfig;
import org.rf.ide.core.project.RobotProjectConfigReader;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;

public class RedEclipseProjectConfigReader extends RobotProjectConfigReader {

    public RobotProjectConfig readConfiguration(final RobotProject robotProject) {
        return readConfiguration(robotProject.getProject());
    }

    public RobotProjectConfig readConfiguration(final IProject project) {
        return readConfiguration(project.getFile(RobotProjectConfig.FILENAME));
    }

    public RobotProjectConfig readConfiguration(final IFile file) {
        if (file == null || !file.exists()) {
            throw new CannotReadProjectConfigurationException(
                    "Project configuration file '" + file.getName() + "' does not exist");
        }
        try (InputStream stream = file.getContents()) {
            return readConfiguration(stream);
        } catch (final IOException | CoreException e) {
            throw new CannotReadProjectConfigurationException("Project configuration file '" + file.getName()
                    + "' does not exist");
        }
    }

    public RobotProjectConfigWithLines readConfigurationWithLines(final IFile file) {
        try (InputStream stream = file.getContents()) {
            return readConfigurationWithLines(stream);
        } catch (final IOException | CoreException e) {
            throw new CannotReadProjectConfigurationException("Project configuration file '" + file.getName()
                    + "' does not exist");
        }
    }
}

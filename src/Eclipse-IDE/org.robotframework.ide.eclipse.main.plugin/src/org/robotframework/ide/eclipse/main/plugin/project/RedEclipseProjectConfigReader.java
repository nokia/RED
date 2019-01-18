/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project;

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.rf.ide.core.project.RobotProjectConfig;
import org.rf.ide.core.project.RobotProjectConfigReader;

public class RedEclipseProjectConfigReader extends RobotProjectConfigReader {

    public RobotProjectConfig readConfiguration(final IFile file) {
        if (file == null || !file.exists()) {
            throw new CannotReadProjectConfigurationException(
                    "Project configuration file '" + RobotProjectConfig.FILENAME + "' does not exist");
        }
        try (InputStream contents = file.getContents()) {
            return readConfiguration(contents);
        } catch (final IOException | CoreException e) {
            throw new CannotReadProjectConfigurationException(
                    "Project configuration file '" + RobotProjectConfig.FILENAME + "' cannot be read", e);
        }
    }

    public RobotProjectConfigWithLines readConfigurationWithLines(final IFile file) {
        if (file == null || !file.exists()) {
            throw new CannotReadProjectConfigurationException(
                    "Project configuration file '" + RobotProjectConfig.FILENAME + "' does not exist");
        }
        try (InputStream contents = file.getContents()) {
            return readConfigurationWithLines(contents);
        } catch (final IOException | CoreException e) {
            throw new CannotReadProjectConfigurationException(
                    "Project configuration file '" + RobotProjectConfig.FILENAME + "' cannot be read", e);
        }
    }
}

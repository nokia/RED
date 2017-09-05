/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.rf.ide.core.project.RobotProjectConfig;
import org.rf.ide.core.project.RobotProjectConfigWriter;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;


public class RedEclipseProjectConfigWriter extends RobotProjectConfigWriter {

    public void writeConfiguration(final RobotProjectConfig configuration, final RobotProject robotProject) {
        writeConfiguration(configuration, robotProject.getProject());
    }

    public void writeConfiguration(final RobotProjectConfig configuration, final IProject project) {
        try {
            final StringWriter writer = new StringWriter(512);
            writeConfiguration(configuration, writer);

            final IFile configFile = project.getFile(RobotProjectConfig.FILENAME);
            final InputStream source = new ByteArrayInputStream(writer.toString().getBytes(StandardCharsets.UTF_8));
            if (!configFile.exists()) {
                configFile.create(source, true, null);
            } else {
                configFile.setContents(source, true, true, null);
            }
        } catch (final CoreException e) {
            throw new CannotWriteProjectConfigurationException(
                    "Unable to write configuration file for '" + project.getName() + "' project", e);
        }
    }
}

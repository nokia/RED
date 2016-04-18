/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;

public class RobotProjectConfigWriter {

    public void writeConfiguration(final RobotProjectConfig configuration, final RobotProject project) {
        writeConfiguration(configuration, project.getProject());
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
            throw new CannotWriteProjectConfigurationException("Unable to write configuration file for '"
                    + project.getName() + "' project", e);
        }
    }

    private void writeConfiguration(final RobotProjectConfig configuration, final Writer writer) {
        try {
            final JAXBContext jaxbContext = JAXBContext.newInstance(RobotProjectConfig.class);
            final Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            marshaller.marshal(configuration, writer);
        } catch (final JAXBException e) {
            throw new CannotWriteProjectConfigurationException("Unable to write project configuration file");
        }
    }

    public static class CannotWriteProjectConfigurationException extends RuntimeException {
        public CannotWriteProjectConfigurationException(final String message) {
            super(message);
        }

        public CannotWriteProjectConfigurationException(final String message, final Throwable cause) {
            super(message, cause);
        }
    }
}

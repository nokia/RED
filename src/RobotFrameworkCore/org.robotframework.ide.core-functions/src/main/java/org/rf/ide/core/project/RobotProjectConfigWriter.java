/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.project;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

public class RobotProjectConfigWriter {

    public void writeConfiguration(final RobotProjectConfig configuration, final File projectDir) {
        try {
            final StringWriter writer = new StringWriter(512);
            writeConfiguration(configuration, writer);
            final InputStream source = new ByteArrayInputStream(writer.toString().getBytes(StandardCharsets.UTF_8));

            final File configFile = new File(projectDir, RobotProjectConfig.FILENAME);
            Files.copy(source, configFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (final IOException e) {
            throw new CannotWriteProjectConfigurationException(
                    "Unable to write configuration file for '" + projectDir.getName() + "' project", e);
        }
    }

    public void writeConfiguration(final RobotProjectConfig configuration, final Writer writer) {
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

        private static final long serialVersionUID = 1L;

        public CannotWriteProjectConfigurationException(final String message) {
            super(message);
        }

        public CannotWriteProjectConfigurationException(final String message, final Throwable cause) {
            super(message, cause);
        }
    }
}

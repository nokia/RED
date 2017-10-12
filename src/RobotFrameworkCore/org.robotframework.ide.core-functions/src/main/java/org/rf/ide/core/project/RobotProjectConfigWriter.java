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
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import com.google.common.collect.ImmutableMap;
import com.google.common.io.CharStreams;

public class RobotProjectConfigWriter {

    public void writeConfiguration(final RobotProjectConfig configuration, final File projectDir) {
        try {
            final InputStream source = writeConfiguration(configuration);

            final File configFile = new File(projectDir, RobotProjectConfig.FILENAME);
            Files.copy(source, configFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (final IOException e) {
            throw new CannotWriteProjectConfigurationException(
                    "Unable to write configuration file for '" + projectDir.getName() + "' project", e);
        }
    }

    public InputStream writeConfiguration(final RobotProjectConfig configuration) {
        final StringWriter writer = new StringWriter(512);
        writeConfiguration(configuration, writer, ImmutableMap.of(Marshaller.JAXB_FORMATTED_OUTPUT, true));
        return new ByteArrayInputStream(writer.toString().getBytes(StandardCharsets.UTF_8));
    }

    public String writeFragment(final Object modelPart) {
        try {
            final StringWriter writer = new StringWriter(512);
            writeConfiguration(modelPart, writer,
                    ImmutableMap.of(Marshaller.JAXB_FORMATTED_OUTPUT, true, Marshaller.JAXB_FRAGMENT, true));
            final ByteArrayInputStream stream = new ByteArrayInputStream(
                    writer.toString().getBytes(StandardCharsets.UTF_8));
            return CharStreams.toString(new InputStreamReader(stream, StandardCharsets.UTF_8));
        } catch (final IOException e) {
            throw new CannotWriteProjectConfigurationException(
                    "Unable to write fragment of configuration for element of type '"
                            + modelPart.getClass().getSimpleName() + "'",
                    e);
        }
    }

    private void writeConfiguration(final Object modelObject, final Writer writer,
            final Map<String, Object> marshallerProperties) {
        try {
            final JAXBContext jaxbContext = JAXBContext.newInstance(modelObject.getClass());
            final Marshaller marshaller = jaxbContext.createMarshaller();
            for (final Entry<String, Object> property : marshallerProperties.entrySet()) {
                marshaller.setProperty(property.getKey(), property.getValue());
            }
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            marshaller.marshal(modelObject, writer);
        } catch (final JAXBException e) {
            if (e.getLinkedException() != null) {
                throw new CannotWriteProjectConfigurationException(e.getLinkedException().getMessage(), e);
            } else {
                throw new CannotWriteProjectConfigurationException(e.getMessage(), e);
            }
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

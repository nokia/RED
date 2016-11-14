/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.project;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.Unmarshaller.Listener;
import javax.xml.stream.Location;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.rf.ide.core.validation.ProblemPosition;
import org.xml.sax.SAXParseException;

public class RobotProjectConfigReader {

    public RobotProjectConfig readConfiguration(final File file) {
        if (file == null || !file.isFile() || !file.exists()) {
            throw new CannotReadProjectConfigurationException(
                    "Project configuration file '" + file.getName() + "' does not exist");
        }
        try (InputStream stream = new FileInputStream(file)) {
            return readConfiguration(stream);
        } catch (final IOException e) {
            throw new CannotReadProjectConfigurationException(
                    "Project configuration file '" + file.getName() + "' does not exist");
        }
    }

    public RobotProjectConfig readConfiguration(final InputStream contents) {
        return readConfiguration(new InputStreamReader(contents));
    }

    public RobotProjectConfigWithLines readConfigurationWithLines(final File file) {
        if (file == null || !file.isFile() || !file.exists()) {
            throw new CannotReadProjectConfigurationException(
                    "Project configuration file '" + file.getName() + "' does not exist");
        }
        try (InputStream stream = new FileInputStream(file)) {
            return readConfigurationWithLines(stream);
        } catch (final IOException e) {
            throw new CannotReadProjectConfigurationException(
                    "Project configuration file '" + file.getName() + "' does not exist");
        }
    }

    public RobotProjectConfigWithLines readConfigurationWithLines(final InputStream contents) {
        return readConfigurationWithLines(new InputStreamReader(contents));
    }

    protected final RobotProjectConfig readConfiguration(final Reader reader) {
        try {
            final JAXBContext jaxbContext = JAXBContext.newInstance(RobotProjectConfig.class);
            return (RobotProjectConfig) jaxbContext.createUnmarshaller().unmarshal(reader);

        } catch (final JAXBException e) {
            if (e.getLinkedException() != null) {
                throw new CannotReadProjectConfigurationException(e.getLinkedException().getMessage(), e);
            } else {
                throw new CannotReadProjectConfigurationException(e.getMessage(), e);
            }
        }
    }

    protected final RobotProjectConfigWithLines readConfigurationWithLines(final Reader reader) {
        try {
            final XMLInputFactory xmlFactory = XMLInputFactory.newFactory();
            final JAXBContext jaxbContext = JAXBContext.newInstance(RobotProjectConfig.class);
            final Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            final XMLStreamReader xmlReader = xmlFactory.createXMLStreamReader(reader);
            final LocationListener listener = new LocationListener(xmlReader);
            unmarshaller.setListener(listener);

            final RobotProjectConfig config = (RobotProjectConfig) unmarshaller.unmarshal(xmlReader);
            return new RobotProjectConfigWithLines(config, listener.locations);

        } catch (final JAXBException e) {
            if (e.getLinkedException() != null) {
                throw new CannotReadProjectConfigurationException(e.getLinkedException().getMessage(), e);
            } else {
                throw new CannotReadProjectConfigurationException(e.getMessage(), e);
            }
        } catch (final XMLStreamException e) {
            throw new CannotReadProjectConfigurationException(e.getMessage(), e);
        }
    }
    
    public static class RobotProjectConfigWithLines {

        private final Map<Object, ProblemPosition> locations;
        private final RobotProjectConfig config;

        public RobotProjectConfigWithLines(final RobotProjectConfig config,
                final Map<Object, ProblemPosition> locations) {
            this.config = config;
            this.locations = locations;
        }

        public RobotProjectConfig getConfigurationModel() {
            return config;
        }

        public Map<Object, ProblemPosition> getLinesMapping() {
            return locations;
        }
    }

    private static class LocationListener extends Listener {
        private final XMLStreamReader streamReader;

        private final Deque<Location> locationsStack = new LinkedList<>();
        private final Map<Object, ProblemPosition> locations = new HashMap<>();;

        private LocationListener(final XMLStreamReader streamReader) {
            this.streamReader = streamReader;
        }

        @Override
        public void beforeUnmarshal(final Object target, final Object parent) {
            locationsStack.addFirst(streamReader.getLocation());
        }

        @Override
        public void afterUnmarshal(final Object target, final Object parent) {
            final Location beginLocation = locationsStack.removeFirst();
            locations.put(target, new ProblemPosition(beginLocation.getLineNumber()));
        }
    }

    public static class CannotReadProjectConfigurationException extends RuntimeException {

        private static final long serialVersionUID = 1L;

        public CannotReadProjectConfigurationException(final String message) {
            super(message);
        }

        public CannotReadProjectConfigurationException(final String message, final Throwable cause) {
            super(message, cause);
        }

        public int getLineNumber() {
            final JAXBException jaxbException = (JAXBException) getCause();
            if (jaxbException.getLinkedException() instanceof SAXParseException) {
                final SAXParseException saxException = (SAXParseException) jaxbException.getLinkedException();
                return saxException.getLineNumber();
            }
            return 1;
        }
    }
}

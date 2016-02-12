/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project;

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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.project.build.ProblemPosition;
import org.xml.sax.SAXParseException;

public class RobotProjectConfigReader {

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

    private RobotProjectConfig readConfiguration(final InputStream contents) {
        return readConfiguration(new InputStreamReader(contents));
    }

    public RobotProjectConfigWithLines readConfigurationWithLines(final IFile file) {
        try (InputStream stream = file.getContents()) {
            return readConfigurationWithLines(stream);
        } catch (final IOException | CoreException e) {
            throw new CannotReadProjectConfigurationException("Project configuration file '" + file.getName()
                    + "' does not exist");
        }
    }

    public RobotProjectConfigWithLines readConfigurationWithLines(final InputStream contents) {
        return readConfigurationWithLines(new InputStreamReader(contents));
    }

    private RobotProjectConfig readConfiguration(final Reader reader) {
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

    private RobotProjectConfigWithLines readConfigurationWithLines(final Reader reader) {
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
        private CannotReadProjectConfigurationException(final String message) {
            super(message);
        }

        private CannotReadProjectConfigurationException(final String message, final Throwable cause) {
            super(message, cause);
        }

        public int getLineNumber() {
            final JAXBException jaxbException = asJaxbException();
            if (jaxbException.getLinkedException() instanceof SAXParseException) {
                final SAXParseException saxException = (SAXParseException) jaxbException.getLinkedException();
                return saxException.getLineNumber();
            }
            return 1;
        }

        private JAXBException asJaxbException() {
            return (JAXBException) getCause();
        }
    }
}

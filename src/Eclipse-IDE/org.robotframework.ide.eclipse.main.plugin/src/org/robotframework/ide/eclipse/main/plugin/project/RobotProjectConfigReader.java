/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

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
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.xml.sax.SAXParseException;

import com.google.common.base.Charsets;

public class RobotProjectConfigReader {

    public RobotProjectConfig readConfiguration(final RobotProject robotProject) {
        return readConfiguration(robotProject.getProject());
    }

    public RobotProjectConfig readConfiguration(final IProject project) {
        return readConfiguration(project.getFile(RobotProjectConfig.FILENAME));
    }

    public RobotProjectConfig readConfiguration(final IFile file) {
        try {
            if (file == null || file.getLocation() == null) {
                throw new CannotReadProjectConfigurationException(
                        "Project configuration file '" + file.getName() + "' does not exist");
            }
            return readConfiguration(new FileReader(file.getLocation().toFile()));
        } catch (final FileNotFoundException e) {
            throw new CannotReadProjectConfigurationException("Project configuration file '" + file.getName()
                    + "' does not exist");
        }
    }

    public RobotProjectConfig readConfiguration(final InputStream contents) {
        return readConfiguration(new InputStreamReader(contents));
    }

    public RobotProjectConfigWithLines readConfigurationWithLines(final IFile file) {
        try {
            final InputStreamReader reader = new InputStreamReader(new FileInputStream(file.getLocation().toFile()),
                    Charsets.UTF_8);
            return readConfigurationWithLines(reader);
        } catch (final FileNotFoundException e) {
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

        private final Map<Object, Location> locations;
        private final RobotProjectConfig config;

        public RobotProjectConfigWithLines(final RobotProjectConfig config, final Map<Object, Location> locations) {
            this.config = config;
            this.locations = locations;
        }

        public RobotProjectConfig getConfigurationModel() {
            return config;
        }

        public Map<Object, Location> getLinesMapping() {
            return locations;
        }
    }

    private static class LocationListener extends Listener {
        private final XMLStreamReader streamReader;
        private final Stack<Location> locationsStack = new Stack<Location>();
        private final Map<Object, Location> locations = new HashMap<>();;

        private LocationListener(final XMLStreamReader streamReader) {
            this.streamReader = streamReader;
        }

        @Override
        public void beforeUnmarshal(final Object target, final Object parent) {
            locationsStack.push(streamReader.getLocation());
        }

        @Override
        public void afterUnmarshal(final Object target, final Object parent) {
            final Location beginLocation = locationsStack.pop();
            // final Location endLocation = streamReader.getLocation();
            locations.put(target, beginLocation);
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

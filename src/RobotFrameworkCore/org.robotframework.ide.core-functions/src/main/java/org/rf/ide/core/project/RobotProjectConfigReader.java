/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.project;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.TreeSet;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.Unmarshaller.Listener;
import javax.xml.stream.Location;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.rf.ide.core.project.RobotProjectConfig.ReferencedLibrary;
import org.rf.ide.core.project.RobotProjectConfig.ReferencedLibraryArgumentsVariant;
import org.rf.ide.core.testdata.model.FilePosition;
import org.rf.ide.core.testdata.model.FileRegion;
import org.xml.sax.SAXParseException;

import com.google.common.io.CharStreams;

public class RobotProjectConfigReader {

    public RobotProjectConfig readConfiguration(final InputStream contents) {
        try (Reader reader = new InputStreamReader(contents, StandardCharsets.UTF_8)) {
            return readConfiguration(reader);
        } catch (final IOException e) {
            throw new CannotReadProjectConfigurationException(
                    "Project configuration file '" + RobotProjectConfig.FILENAME + "' cannot be read", e);
        }
    }

    public RobotProjectConfigWithLines readConfigurationWithLines(final InputStream contents) {
        try (Reader reader = new InputStreamReader(contents, StandardCharsets.UTF_8)) {
            return readConfigurationWithLines(reader);
        } catch (final IOException e) {
            throw new CannotReadProjectConfigurationException(
                    "Project configuration file '" + RobotProjectConfig.FILENAME + "' cannot be read", e);
        }
    }

    private final RobotProjectConfig readConfiguration(final Reader reader) {
        try {
            final JAXBContext jaxbContext = JAXBContext.newInstance(RobotProjectConfig.class);
            final Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            unmarshaller.setListener(new SimpleListener());
            return (RobotProjectConfig) unmarshaller.unmarshal(reader);

        } catch (final JAXBException e) {
            if (e.getLinkedException() != null) {
                throw new CannotReadProjectConfigurationException(e.getLinkedException().getMessage(), e);
            } else {
                throw new CannotReadProjectConfigurationException(e.getMessage(), e);
            }
        }
    }

    private final RobotProjectConfigWithLines readConfigurationWithLines(final Reader reader) {
        try {
            final String content = CharStreams.toString(reader);

            final XMLInputFactory xmlFactory = XMLInputFactory.newFactory();
            final JAXBContext jaxbContext = JAXBContext.newInstance(RobotProjectConfig.class);
            final Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            final TreeSet<FileRegion> elementRegions = extractElementRegions(content, xmlFactory);

            final XMLStreamReader xmlReader = xmlFactory.createXMLStreamReader(new StringReader(content));
            final LocationListener locationListener = new LocationListener(xmlReader, elementRegions);
            final CompoundListener listener = new CompoundListener(new SimpleListener(), locationListener);
            unmarshaller.setListener(listener);

            final RobotProjectConfig config = (RobotProjectConfig) unmarshaller.unmarshal(xmlReader);
            return new RobotProjectConfigWithLines(config, elementRegions, locationListener.locations);

        } catch (final JAXBException e) {
            if (e.getLinkedException() != null) {
                throw new CannotReadProjectConfigurationException(e.getLinkedException().getMessage(), e);
            } else {
                throw new CannotReadProjectConfigurationException(e.getMessage(), e);
            }
        } catch (final XMLStreamException | IOException e) {
            throw new CannotReadProjectConfigurationException(e.getMessage(), e);
        }
    }

    private TreeSet<FileRegion> extractElementRegions(final String content, final XMLInputFactory xmlFactory)
            throws XMLStreamException {
        final XMLStreamReader xmlReader = xmlFactory.createXMLStreamReader(new StringReader(content));

        final TreeSet<FileRegion> regions = new TreeSet<>(byStart());
        final Deque<FilePosition> positionsStack = new LinkedList<>();
        Location previous = null;
        while (xmlReader.hasNext()) {
            final int result = xmlReader.next();
            if (result == XMLStreamConstants.START_ELEMENT) {

                final FilePosition position;
                if (previous != null) {
                    position = new FilePosition(previous.getLineNumber(), previous.getColumnNumber() - 1);
                } else {
                    // first element, so positioning from current location
                    final Location location = xmlReader.getLocation();
                    final int tagLength = xmlReader.getLocalName().length() + 2;
                    position = new FilePosition(location.getLineNumber(), location.getColumnNumber() - tagLength);
                }
                positionsStack.push(position);

            } else if (result == XMLStreamConstants.END_ELEMENT) {

                final Location location = xmlReader.getLocation();
                final FilePosition position = new FilePosition(location.getLineNumber(),
                        location.getColumnNumber() - 1);

                regions.add(new FileRegion(positionsStack.pop(), position));
            }
            previous = xmlReader.getLocation();
        }
        return regions;
    }

    private static Comparator<FileRegion> byStart() {
        return (r1, r2) -> {
            final int line1 = r1.getStart().getLine();
            final int line2 = r2.getStart().getLine();
            if (line1 == line2) {
                return Integer.compare(r1.getStart().getColumn(), r2.getStart().getColumn());
            } else {
                return Integer.compare(line1, line2);
            }
        };
    }

    public static class RobotProjectConfigWithLines {

        private final RobotProjectConfig config;

        private final Map<Object, FilePosition> locations;

        private final TreeSet<FileRegion> elementRegions;

        public RobotProjectConfigWithLines(final RobotProjectConfig config, final TreeSet<FileRegion> elementRegions,
                final Map<Object, FilePosition> locations) {
            this.config = config;
            this.elementRegions = elementRegions;
            this.locations = locations;
        }

        public RobotProjectConfig getConfigurationModel() {
            return config;
        }

        public int getLineFor(final Object configElement) {
            return Optional.ofNullable(locations.get(configElement)).map(FilePosition::getLine).orElse(-1);
        }

        public FileRegion getRegionFor(final Object configElement) {
            final FilePosition elementStartPosition = locations.get(configElement);
            if (elementStartPosition != null) {
                for (final FileRegion region : elementRegions) {
                    if (region.getStart().equals(elementStartPosition)) {
                        return region;
                    }
                }
            }
            return null;
        }
    }

    private static class CompoundListener extends Listener {

        private final Listener[] listeners;

        public CompoundListener(final Listener... listeners) {
            this.listeners = listeners;
        }

        @Override
        public void beforeUnmarshal(final Object target, final Object parent) {
            for (final Listener listener : listeners) {
                listener.beforeUnmarshal(target, parent);
            }
        }

        @Override
        public void afterUnmarshal(final Object target, final Object parent) {
            for (final Listener listener : listeners) {
                listener.afterUnmarshal(target, parent);
            }
        }
    }

    private static class SimpleListener extends Listener {

        @Override
        public void afterUnmarshal(final Object target, final Object parent) {
            if (target instanceof ReferencedLibraryArgumentsVariant && parent instanceof ReferencedLibrary) {
                ((ReferencedLibraryArgumentsVariant) target).setParent((ReferencedLibrary) parent);
            }
        }
    }

    private static class LocationListener extends Listener {

        private final XMLStreamReader streamReader;

        private final TreeSet<FileRegion> elementRegions;

        private final Deque<Location> locationsStack = new LinkedList<>();

        private final Map<Object, FilePosition> locations = new HashMap<>();

        private LocationListener(final XMLStreamReader streamReader, final TreeSet<FileRegion> elementRegions) {
            this.streamReader = streamReader;
            this.elementRegions = elementRegions;
        }

        @Override
        public void beforeUnmarshal(final Object target, final Object parent) {
            locationsStack.addFirst(streamReader.getLocation());
        }

        @Override
        public void afterUnmarshal(final Object target, final Object parent) {
            final Location beginLocation = locationsStack.removeFirst();
            final FilePosition loc = new FilePosition(beginLocation.getLineNumber(), beginLocation.getColumnNumber());

            final FileRegion nearestTagStart = elementRegions.floor(new FileRegion(loc, null));

            locations.put(target, nearestTagStart.getStart().copy());
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
            if (getCause() instanceof JAXBException) {
                final JAXBException jaxbException = (JAXBException) getCause();
                if (jaxbException.getLinkedException() instanceof SAXParseException) {
                    final SAXParseException saxException = (SAXParseException) jaxbException.getLinkedException();
                    return saxException.getLineNumber();
                }
            }
            return 1;
        }
    }
}

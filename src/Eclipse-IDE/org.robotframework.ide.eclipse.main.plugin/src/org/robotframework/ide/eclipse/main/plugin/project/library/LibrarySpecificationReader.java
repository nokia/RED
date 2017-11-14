/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.library;

import java.io.File;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.eclipse.core.resources.IFile;
import org.rf.ide.core.project.RobotProjectConfig.ReferencedLibrary;
import org.rf.ide.core.project.RobotProjectConfig.RemoteLocation;

public class LibrarySpecificationReader {

    static LibrarySpecification readSpecification(final IFile libraryFile) {
        try {
            final JAXBContext jaxbContext = JAXBContext.newInstance(LibrarySpecification.class);
            final Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            final LibrarySpecification specification = (LibrarySpecification) jaxbUnmarshaller.unmarshal(new File(libraryFile.getLocationURI()));
            specification.propagateFormat();
            specification.setSourceFile(libraryFile);

            return specification;

        } catch (final JAXBException e) {
            throw new CannotReadLibrarySpecificationException("Unable to read library specification file", e);
        }
    }

    public static LibrarySpecification readStandardLibrarySpecification(final IFile libraryFile,
            final String libraryName) {
        return readSpecification(libraryFile);
    }

    public static LibrarySpecification readRemoteSpecification(final IFile libraryFile,
            final RemoteLocation remoteLocation) {
        final LibrarySpecification spec = readSpecification(libraryFile);
        spec.setRemoteLocation(remoteLocation);
        spec.setSecondaryKey(remoteLocation.getUri());
        return spec;
    }

    public static LibrarySpecification readReferencedSpecification(final IFile libraryFile,
            final ReferencedLibrary library) {
        final LibrarySpecification spec = readSpecification(libraryFile);
        spec.setReferenced(library);
        spec.setSecondaryKey(library.getPath());
        return spec;
    }

    @SuppressWarnings("serial")
    public static class CannotReadLibrarySpecificationException extends RuntimeException {

        public CannotReadLibrarySpecificationException(final String message, final Throwable cause) {
            super(message, cause);
        }
    }
}

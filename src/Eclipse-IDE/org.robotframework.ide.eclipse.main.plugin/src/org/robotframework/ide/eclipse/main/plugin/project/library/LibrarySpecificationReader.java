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

import org.robotframework.ide.eclipse.main.plugin.model.LibraryDescriptor;
import org.robotframework.ide.eclipse.main.plugin.model.LibspecsFolder;

public class LibrarySpecificationReader {

    public static LibrarySpecification readLibrarySpecification(final LibspecsFolder libspecsFolder,
            final LibraryDescriptor descriptor) {

        final String fileName = descriptor.generateLibspecFileName();
        final File xmlFile = new File(libspecsFolder.getXmlSpecFile(fileName).getLocationURI());
        final LibrarySpecification spec = readSpecification(xmlFile);
        spec.setDescriptor(descriptor);
        return spec;
    }

    public static LibrarySpecification readSpecification(final File xmlLibspecFile) {
        try {
            final JAXBContext jaxbContext = JAXBContext.newInstance(LibrarySpecification.class);
            final Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            final LibrarySpecification specification = (LibrarySpecification) jaxbUnmarshaller
                    .unmarshal(xmlLibspecFile);
            specification.propagateFormat();

            return specification;

        } catch (final JAXBException e) {
            throw new CannotReadLibrarySpecificationException("Unable to read library specification file", e);
        }
    }

    public static class CannotReadLibrarySpecificationException extends RuntimeException {

        private static final long serialVersionUID = 1L;

        public CannotReadLibrarySpecificationException(final String message, final Throwable cause) {
            super(message, cause);
        }
    }
}

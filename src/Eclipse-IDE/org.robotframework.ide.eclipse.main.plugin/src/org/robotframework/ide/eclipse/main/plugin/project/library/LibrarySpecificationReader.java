package org.robotframework.ide.eclipse.main.plugin.project.library;

import java.io.File;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.eclipse.core.resources.IFile;

public class LibrarySpecificationReader {

    public static LibrarySpecification readSpecification(final IFile libraryFile) {
        try {
            final JAXBContext jaxbContext = JAXBContext.newInstance(LibrarySpecification.class);
            final Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            final LibrarySpecification specification = (LibrarySpecification) jaxbUnmarshaller.unmarshal(new File(libraryFile.getLocationURI()));
            specification.propagateFormat();
            return specification;

        } catch (final JAXBException e) {
            throw new CannotReadlibrarySpecificationException("Unable to read library specification file", e);
        }
    }

    public static class CannotReadlibrarySpecificationException extends RuntimeException {

        public CannotReadlibrarySpecificationException(final String message, final Throwable cause) {
            super(message, cause);
        }
    }
}

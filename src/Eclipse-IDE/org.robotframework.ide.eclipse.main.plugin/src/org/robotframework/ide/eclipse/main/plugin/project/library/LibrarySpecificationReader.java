package org.robotframework.ide.eclipse.main.plugin.project.library;

import java.io.File;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.eclipse.core.resources.IFile;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig.RemoteLocation;

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

    public static LibrarySpecification readRemoteSpecification(final IFile libraryFile,
            final RemoteLocation remoteLocation) {
        final LibrarySpecification spec = readSpecification(libraryFile);
        spec.setRemote();
        spec.setAdditionalInformation(remoteLocation.getUri());
        return spec;
    }

    public static class CannotReadlibrarySpecificationException extends RuntimeException {

        public CannotReadlibrarySpecificationException(final String message, final Throwable cause) {
            super(message, cause);
        }
    }
}

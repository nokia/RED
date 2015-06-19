package org.robotframework.ide.eclipse.main.plugin.project;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.robotframework.ide.eclipse.main.plugin.RobotProject;
import org.xml.sax.SAXParseException;

public class RobotProjectConfigReader {

    public RobotProjectConfig readConfiguration(final RobotProject robotProject) {
        return readConfiguration(robotProject.getProject());
    }

    public RobotProjectConfig readConfiguration(final IProject project) {
        return readConfiguration(project.getFile(RobotProjectConfig.FILENAME));
    }

    public RobotProjectConfig readConfiguration(final IFile file) {
        try {
            return readConfiguration(new FileReader(file.getLocation().toFile()));
        } catch (final FileNotFoundException e) {
            throw new CannotReadProjectConfigurationException("Project configuration file '" + file.getName()
                    + "' does not exist");
        }
    }

    public RobotProjectConfig readConfiguration(final InputStream contents) {
        return readConfiguration(new InputStreamReader(contents));
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

    public static class CannotReadProjectConfigurationException extends RuntimeException {
        private CannotReadProjectConfigurationException(final String message) {
            super(message);
        }

        private CannotReadProjectConfigurationException(final String message, final JAXBException cause) {
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

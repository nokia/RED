package org.robotframework.ide.eclipse.main.plugin.project.build;

import java.io.IOException;
import java.net.Socket;
import java.util.Map;

import javax.xml.stream.Location;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig.RemoteLocation;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfigReader;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfigReader.CannotReadProjectConfigurationException;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfigReader.RobotProjectConfigWithLines;

public class RobotProjectConfigFileValidator {

    private final IFile configFile;

    public RobotProjectConfigFileValidator(final IFile configFile) {
        this.configFile = configFile;
    }

    void validate(final IProgressMonitor monitor) throws CoreException {
        RobotProjectConfigWithLines config = null;
        try {
            config = new RobotProjectConfigReader().readConfigurationWithLines(configFile);
        } catch (final CannotReadProjectConfigurationException e) {
            System.err.println(e);
        }
        final RobotProjectConfig model = config.getConfigurationModel();
        for (final RemoteLocation location : model.getRemoteLocations()) {
            validateRemoteLocation(location, configFile, config.getLinesMapping(), monitor);
        }
    }

    private void validateRemoteLocation(final RemoteLocation location, final IFile configFile,
            final Map<Object, Location> linesMapping, final IProgressMonitor monitor) throws CoreException {
        String address = location.getPath();
        final String httpPrefix = "http://";
        if (address.startsWith(httpPrefix)) {
            address = address.substring(httpPrefix.length());
        }
        try (Socket s = new Socket(address, location.getPort())) {
            // that's fine
        } catch (final IOException ex) {
            final IMarker marker = configFile.createMarker(RobotProblem.TYPE_ID);

            marker.setAttribute(IMarker.MESSAGE,
                    "Unreachable remote server " + address + " on port " + location.getPort());
            marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
            final Integer lineNumber = linesMapping.get(location).getLineNumber();
            marker.setAttribute(IMarker.LOCATION, "line " + lineNumber);
            marker.setAttribute(IMarker.LINE_NUMBER, lineNumber);
        }
    }
}

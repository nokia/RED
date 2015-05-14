package org.robotframework.ide.eclipse.main.plugin.project;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.robotframework.ide.core.executor.RobotRuntimeEnvironment;

public class RobotProjectMetadata {

    private final File pythonDirectory;
    
    private final String version;

    private final List<String> stdLibrariesNames;

    RobotProjectMetadata(final File pythonDirectory, final String version, final List<String> stdLibraries) {
        this.pythonDirectory = pythonDirectory;
        this.version = version;
        this.stdLibrariesNames = stdLibraries;
    }

    public static RobotProjectMetadata createEmpty() {
        return new RobotProjectMetadata(null, null, new ArrayList<String>());
    }

    public static RobotProjectMetadata create(final File pythonDirectory) {
        final RobotRuntimeEnvironment environment = RobotRuntimeEnvironment.create(pythonDirectory);

        final List<String> stdLibraries = environment.getStandardLibrariesNames();
        final String version = environment.getVersion();
        return new RobotProjectMetadata(pythonDirectory, version, stdLibraries);
    }

    public static RobotProjectMetadata create(final String version, final List<String> stdLibs) {
        return new RobotProjectMetadata(null, version, stdLibs);
    }

    public File getPythonLocation() {
        return pythonDirectory;
    }

    public String getVersion() {
        return version;
    }

    public List<String> getStdLibrariesNames() {
        return stdLibrariesNames;
    }
}

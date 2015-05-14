package org.robotframework.ide.eclipse.main.plugin.project;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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

    public static RobotProjectMetadata create(final File file, final String version, final List<String> stdLibs) {
        return new RobotProjectMetadata(file, version, stdLibs);
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

    @Override
    public int hashCode() {
        return Objects.hash(pythonDirectory, version, stdLibrariesNames);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final RobotProjectMetadata other = (RobotProjectMetadata) obj;
        return Objects.equals(pythonDirectory, other.pythonDirectory) && Objects.equals(version, other.version)
                && Objects.equals(stdLibrariesNames, other.stdLibrariesNames);
    }
}

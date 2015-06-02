package org.robotframework.ide.eclipse.main.plugin.project;

import java.io.File;
import java.util.Objects;

public class RobotProjectConfiguration {

    private static final String CURRENT_VERSION = "1.0";

    private final String version;

    private final File pythonDirectory;

    private RobotProjectConfiguration(final String version, final File pythonDirectory) {
        this.version = version;
        this.pythonDirectory = pythonDirectory;
    }

    public static RobotProjectConfiguration create() {
        return create(null);
    }

    public static RobotProjectConfiguration create(final File pythonDirectory) {
        return create(CURRENT_VERSION, pythonDirectory);
    }

    public static RobotProjectConfiguration create(final String version, final File pythonDirectory) {
        return new RobotProjectConfiguration(version, pythonDirectory);
    }

    public String getVersion() {
        return version;
    }

    public File getPythonLocation() {
        return pythonDirectory;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pythonDirectory, version);
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
        final RobotProjectConfiguration other = (RobotProjectConfiguration) obj;
        return Objects.equals(pythonDirectory, other.pythonDirectory) && Objects.equals(version, other.version);
    }
}

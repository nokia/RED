package org.robotframework.ide.eclipse.main.plugin.project;

import static com.google.common.collect.Lists.newArrayList;

import java.io.File;
import java.util.List;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.eclipse.core.runtime.IPath;

@XmlRootElement(name = "projectConfiguration")
public class RobotProjectConfig {

    public static final String FILENAME = "red.xml";

    private static final String CURRENT_VERSION = "1.0";

    private String version;

    private ExecutionEnvironment executionEnvironment;

    private List<String> librarySpecifications;

    public static RobotProjectConfig create() {
        final RobotProjectConfig configuration = new RobotProjectConfig();
        configuration.setVersion(CURRENT_VERSION);
        return configuration;
    }

    public static RobotProjectConfig create(final File pythonDirectory) {
        final RobotProjectConfig configuration = new RobotProjectConfig();
        configuration.setVersion(CURRENT_VERSION);
        configuration.executionEnvironment.setPath(pythonDirectory.getAbsolutePath());
        return configuration;
    }

    @XmlElement(name = "configVersion", required = true)
    public void setVersion(final String version) {
        this.version = version;
    }

    public String getVersion() {
        return version;
    }

    @XmlElement(name = "robotExecEnvironment", required = false)
    public void setExecutionEnvironment(final ExecutionEnvironment executionEnvironment) {
        this.executionEnvironment = executionEnvironment;
    }

    public ExecutionEnvironment getExecutionEnvironment() {
        return executionEnvironment;
    }

    @XmlElement(name = "referencedLibspec")
    public void setLibrarySpecifications(final List<String> librarySpecifications) {
        this.librarySpecifications = librarySpecifications;
    }

    public List<String> getLibrarySpecifications() {
        return librarySpecifications;
    }

    public void addReferencedLibrarySpecification(final IPath projectRelativePath) {
        if (librarySpecifications == null) {
            librarySpecifications = newArrayList();
        }
        librarySpecifications.add(projectRelativePath.toPortableString());
    }

    public File getPythonLocation() {
        return executionEnvironment == null ? null : new File(executionEnvironment.path);
    }

    public void setPythonLocation(final File location) {
        if (location == null) {
            executionEnvironment = null;
            return;
        }
        if (executionEnvironment == null) {
            executionEnvironment = new ExecutionEnvironment();
        }
        executionEnvironment.path = location.getAbsolutePath();
    }

    public boolean hasReferencedLibraries() {
        return librarySpecifications != null && !librarySpecifications.isEmpty();
    }

    @Override
    public int hashCode() {
        return Objects.hash(version, executionEnvironment.path);
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
        final RobotProjectConfig other = (RobotProjectConfig) obj;
        return Objects.equals(executionEnvironment.path, other.executionEnvironment.path)
                && Objects.equals(version, other.version);
    }

    @XmlRootElement(namespace = "org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig")
    public static class ExecutionEnvironment {

        private String path;

        @XmlAttribute
        public void setPath(final String path) {
            this.path = path;
        }

        public String getPath() {
            return path;
        }
    }
}

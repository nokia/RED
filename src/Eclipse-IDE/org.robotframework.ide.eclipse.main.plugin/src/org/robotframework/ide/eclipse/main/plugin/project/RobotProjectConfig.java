package org.robotframework.ide.eclipse.main.plugin.project;

import static com.google.common.collect.Lists.newArrayList;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.eclipse.core.runtime.IPath;

@XmlRootElement(name = "projectConfiguration")
@XmlType(propOrder = { "version", "executionEnvironment", "libraries" })
@XmlAccessorType(XmlAccessType.FIELD)
public class RobotProjectConfig {

    public static final String FILENAME = "red.xml";

    private static final String CURRENT_VERSION = "1.0";

    @XmlElement(name = "configVersion", required = true)
    private String version;

    @XmlElement(name = "robotExecEnvironment", required = false)
    private ExecutionEnvironment executionEnvironment;

    @XmlElement(name = "referencedLibrary", required = false)
    private List<ReferencedLibrary> libraries = new ArrayList<>();

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

    public void setVersion(final String version) {
        this.version = version;
    }

    public String getVersion() {
        return version;
    }

    public void setExecutionEnvironment(final ExecutionEnvironment executionEnvironment) {
        this.executionEnvironment = executionEnvironment;
    }

    public ExecutionEnvironment getExecutionEnvironment() {
        return executionEnvironment;
    }

    public void setLibraries(final List<ReferencedLibrary> libraries) {
        this.libraries = libraries;
    }

    public List<ReferencedLibrary> getLibraries() {
        return libraries;
    }

    public void addReferencedLibrarySpecification(final IPath workspaceRelativePath) {
        if (libraries == null) {
            libraries = newArrayList();
        }
        final ReferencedLibrary referencedLibrary = new ReferencedLibrary();
        referencedLibrary.setType(LibraryType.VIRTUAL.toString());
        referencedLibrary.setPath(workspaceRelativePath.toPortableString());

        libraries.add(referencedLibrary);
    }

    public void addReferencedLibraryInJava(final String name, final IPath systemAbsolutePath) {
        if (libraries == null) {
            libraries = newArrayList();
        }
        final ReferencedLibrary referencedLibrary = new ReferencedLibrary();
        referencedLibrary.setType(LibraryType.JAVA.toString());
        referencedLibrary.setName(name);
        referencedLibrary.setPath(systemAbsolutePath.toPortableString());

        libraries.add(referencedLibrary);
    }

    public void removeLibraries(final List<ReferencedLibrary> selectedLibs) {
        libraries.removeAll(selectedLibs);
    }

    public boolean usesPreferences() {
        return executionEnvironment == null;
    }

    public File providePythonLocation() {
        return executionEnvironment == null ? null : new File(executionEnvironment.path);
    }

    public void assignPythonLocation(final File location) {
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
        return libraries != null && !libraries.isEmpty();
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

    @XmlRootElement(name = "robotExecEnvironment")
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class ExecutionEnvironment {

        @XmlAttribute
        private String path;

        public void setPath(final String path) {
            this.path = path;
        }

        public String getPath() {
            return path;
        }
    }

    @XmlRootElement(name = "referencedLibrary")
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class ReferencedLibrary {

        @XmlAttribute
        private String type;

        @XmlAttribute
        private String name;

        @XmlAttribute
        private String path;

        public void setType(final String type) {
            this.type = type;
        }

        public String getType() {
            return type;
        }

        public void setName(final String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public void setPath(final String path) {
            this.path = path;
        }

        public String getPath() {
            return path;
        }

        public LibraryType provideType() {
            return LibraryType.valueOf(type);
        }
    }

    public enum LibraryType {
        VIRTUAL, PYTHON, JAVA
    }
}

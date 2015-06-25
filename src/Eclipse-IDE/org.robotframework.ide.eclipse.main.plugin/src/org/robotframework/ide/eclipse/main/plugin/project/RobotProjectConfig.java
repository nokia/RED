package org.robotframework.ide.eclipse.main.plugin.project;

import static com.google.common.collect.Lists.newArrayList;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

@XmlRootElement(name = "projectConfiguration")
@XmlType(propOrder = { "version", "executionEnvironment", "libraries" })
public class RobotProjectConfig {

    public static final String FILENAME = "red.xml";

    private static final String CURRENT_VERSION = "1.0";

    private String version;

    private ExecutionEnvironment executionEnvironment;

    private List<ReferencedLibrary> libraries;

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

    @XmlElement(name = "referencedLibrary")
    public void setLibraries(final List<ReferencedLibrary> libraries) {
        this.libraries = libraries;
    }

    public List<ReferencedLibrary> getLibraries() {
        return libraries == null ? new ArrayList<ReferencedLibrary>() : libraries;
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

    @XmlRootElement(namespace = "org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig")
    public static class ReferencedLibrary {

        private LibraryType type;

        private String name;

        private IPath path;


        @XmlAttribute
        public void setType(final String type) {
            this.type = LibraryType.valueOf(type);
        }

        public String getType() {
            return type.toString();
        }

        @XmlAttribute
        public void setName(final String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        @XmlAttribute
        public void setPath(final String path) {
            this.path = new Path(path);
        }

        public String getPath() {
            return path.toPortableString();
        }

        public LibraryType provideType() {
            return type;
        }
    }

    public enum LibraryType {
        VIRTUAL, PYTHON, JAVA
    }
}

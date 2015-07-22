package org.robotframework.ide.eclipse.main.plugin.project;

import static com.google.common.collect.Lists.newArrayList;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
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
import org.eclipse.jface.resource.ImageDescriptor;
import org.robotframework.ide.eclipse.main.plugin.RobotImages;

@XmlRootElement(name = "projectConfiguration")
@XmlType(propOrder = { "version", "executionEnvironment", "libraries", "remoteLocations" })
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

    @XmlElement(name = "remoteLocations", required = false)
    private List<RemoteLocation> remoteLocations = new ArrayList<>();

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

    public void setRemoteLocations(final List<RemoteLocation> remoteLocations) {
        this.remoteLocations = remoteLocations;
    }

    public List<RemoteLocation> getRemoteLocations() {
        return remoteLocations;
    }

    public void addReferencedLibrarySpecification(final IPath workspaceRelativePath) {
        addReferencedLibrary(LibraryType.VIRTUAL.toString(), null, workspaceRelativePath);
    }

    public void addReferencedLibraryInPython(final String name, final IPath systemAbsolutePath) {
        addReferencedLibrary(LibraryType.PYTHON.toString(), name, systemAbsolutePath);
    }

    public void addReferencedLibraryInJava(final String name, final IPath systemAbsolutePath) {
        addReferencedLibrary(LibraryType.JAVA.toString(), name, systemAbsolutePath);
    }
    
    private void addReferencedLibrary(final String type, final String name, final IPath path) {
        final ReferencedLibrary referencedLibrary = new ReferencedLibrary();
        referencedLibrary.setType(type);
        if (name != null) {
            referencedLibrary.setName(name);
        }
        referencedLibrary.setPath(path.toPortableString());
        
        if (libraries == null) {
            libraries = newArrayList();
        }
        if (!libraries.contains(referencedLibrary)) {
            libraries.add(referencedLibrary);
        }
    }

    public void addRemoteLocation(final RemoteLocation remoteLocation) {
        if (remoteLocations == null) {
            remoteLocations = newArrayList();
        }
        remoteLocations.add(remoteLocation);
    }

    public void removeLibraries(final List<ReferencedLibrary> selectedLibs) {
        libraries.removeAll(selectedLibs);
    }

    public void removeRemoteLocations(final List<RemoteLocation> locations) {
        remoteLocations.removeAll(locations);
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

    public boolean hasRemoteLibraries() {
        return remoteLocations != null && !remoteLocations.isEmpty();
    }

    @Override
    public int hashCode() {
        return Objects.hash(version, executionEnvironment == null ? null : executionEnvironment.path);
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

        public ImageDescriptor getImage() {
            switch (provideType()) {
                case JAVA:
                    return RobotImages.getJavaLibraryImage();
                default:
                    return RobotImages.getLibraryImage();
            }
        }
        
        @Override
        public boolean equals(final Object obj) {
            if (obj == null) {
                return false;
            } else if (obj.getClass() == getClass()) {
                final ReferencedLibrary other = (ReferencedLibrary) obj;
                return Objects.equals(type, other.type) && Objects.equals(name, other.name) && Objects.equals(path, other.path);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hash(type, name, path);
        }
    }

    public static enum LibraryType {
        VIRTUAL, PYTHON, JAVA
    }

    @XmlRootElement(name = "remoteLocation")
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class RemoteLocation {

        @XmlAttribute(required = true)
        private URI uri;

        public void setUriAddress(final URI uri) {
            this.uri = uri;
        }

        public URI getUriAddress() {
            return uri;
        }

        public void setUri(final String path) {
            try {
                this.uri = new URI(path);
            } catch (final URISyntaxException e) {
                throw new IllegalStateException("Invalid URI '" + path + "'", e);
            }
        }

        public String getUri() {
            return uri.toString();
        }

        @Override
        public boolean equals(final Object obj) {
            if (obj instanceof RemoteLocation) {
                final RemoteLocation that = (RemoteLocation) obj;
                return Objects.equals(uri, that.uri);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return uri.hashCode();
        }

        public String createLibspecFileName() {
            return "Remote_" + pathWithoutSpecialCharacters(getUri());
        }

        private static String pathWithoutSpecialCharacters(final String path) {
            return path.replaceAll("[^A-Za-z0-9]", "_");
        }
    }
}

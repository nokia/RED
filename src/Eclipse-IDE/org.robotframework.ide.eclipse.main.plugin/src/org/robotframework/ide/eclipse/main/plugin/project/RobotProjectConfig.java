/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newLinkedHashMap;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.resource.ImageDescriptor;
import org.robotframework.ide.eclipse.main.plugin.RedImages;

@XmlRootElement(name = "projectConfiguration")
@XmlType(propOrder = { "version", "executionEnvironment", "variableMappings", "libraries", "remoteLocations",
        "referencedVariableFiles", "excludedPath" })
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
    
    @XmlElement(name = "variableFiles", required = false)
    private List<ReferencedVariableFile> referencedVariableFiles = new ArrayList<>();

    @XmlElement(name = "variable", required = false)
    private List<VariableMapping> variableMappings = new ArrayList<>();

    @XmlElementWrapper(name = "excludedForValidation", required = false)
    private List<ExcludedFolderPath> excludedPath = new ArrayList<>();

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

    public void setVariableMappings(final List<VariableMapping> variableMappings) {
        this.variableMappings = variableMappings;
    }

    public List<VariableMapping> getVariableMappings() {
        return variableMappings;
    }

    public void setExcludedPath(final List<ExcludedFolderPath> excludedPaths) {
        this.excludedPath = excludedPaths;
    }

    public List<ExcludedFolderPath> getExcludedPath() {
        return excludedPath;
    }

    public void addExcludedPath(final IPath path) {
        if (excludedPath == null) {
            excludedPath = new ArrayList<>();
        }
        final ExcludedFolderPath excludedFolderPath = new ExcludedFolderPath();
        excludedFolderPath.path = path.toPortableString();
        if (!excludedPath.contains(excludedFolderPath)) {
            excludedPath.add(excludedFolderPath);
        }
    }

    public void removeExcludedPath(final IPath path) {
        if (excludedPath == null) {
            return;
        }
        ExcludedFolderPath foundToRemove = null;
        for (final ExcludedFolderPath excludedPath : excludedPath) {
            if (excludedPath.path.equals(path.toPortableString())) {
                foundToRemove = excludedPath;
            }
        }
        if (foundToRemove != null) {
            excludedPath.remove(foundToRemove);
        }
    }

    public boolean isExcludedFromValidation(final IPath path) {
        return getExcludedPath(path) != null;
    }

    public ExcludedFolderPath getExcludedPath(final IPath path) {
        if (excludedPath == null) {
            return null;
        }
        for (final ExcludedFolderPath excludedPath : excludedPath) {
            if (excludedPath.asPath().equals(path)) {
                return excludedPath;
            }
        }
        return null;
    }

    public void addReferencedLibraryInPython(final String name, final IPath path) {
        final ReferencedLibrary referencedLibrary = new ReferencedLibrary();
        referencedLibrary.setType(LibraryType.PYTHON.toString());
        referencedLibrary.setName(name);
        referencedLibrary.setPath(path.toPortableString());
        addReferencedLibrary(referencedLibrary);
    }

    public boolean addReferencedLibrary(final ReferencedLibrary referencedLibrary) {
        if (libraries == null) {
            libraries = newArrayList();
        }
        if (!libraries.contains(referencedLibrary)) {
            libraries.add(referencedLibrary);
            return true;
        }
        return false;
    }

    public void removeLibraries(final List<ReferencedLibrary> selectedLibs) {
        libraries.removeAll(selectedLibs);
    }

    public void addRemoteLocation(final RemoteLocation remoteLocation) {
        if (remoteLocations == null) {
            remoteLocations = newArrayList();
        }
        remoteLocations.add(remoteLocation);
    }


    public void removeRemoteLocations(final List<RemoteLocation> locations) {
        remoteLocations.removeAll(locations);
    }

    public void addVariableMapping(final VariableMapping mapping) {
        if (variableMappings == null) {
            variableMappings = newArrayList();
        }
        variableMappings.add(mapping);
    }

    public void removeVariableMappings(final List<VariableMapping> mappings) {
        variableMappings.removeAll(mappings);
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
    
    public List<ReferencedVariableFile> getReferencedVariableFiles() {
        return referencedVariableFiles;
    }

    public void addReferencedVariableFile(final ReferencedVariableFile variableFile) {
        if (referencedVariableFiles == null) {
            referencedVariableFiles = newArrayList();
        }
        if (!referencedVariableFiles.contains(variableFile)) {
            referencedVariableFiles.add(variableFile);
        }
    }
    
    public void removeReferencedVariableFiles(final List<ReferencedVariableFile> selectedFiles) {
        referencedVariableFiles.removeAll(selectedFiles);
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

        public IPath getFilepath() {
            if (provideType() == LibraryType.PYTHON) {
                return new Path(path + "/" + name.replaceAll("\\.", "/"));
            } else {
                return new Path(path);
            }
        }

        public LibraryType provideType() {
            return LibraryType.valueOf(type);
        }

        public ImageDescriptor getImage() {
            switch (provideType()) {
                case JAVA:
                    return RedImages.getJavaLibraryImage();
                case PYTHON:
                    return RedImages.getPythonLibraryImage();
                case VIRTUAL:
                    return RedImages.getVirtualLibraryImage();
                default:
                    return RedImages.getLibraryImage();
            }
        }
        
        @Override
        public boolean equals(final Object obj) {
            if (obj == null) {
                return false;
            } else if (obj.getClass() == getClass()) {
                final ReferencedLibrary other = (ReferencedLibrary) obj;
                return Objects.equals(type, other.type) && Objects.equals(name, other.name);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hash(type, name);
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
    
    @XmlRootElement(name = "referencedVariableFile")
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class ReferencedVariableFile {

        @XmlAttribute
        private String path;

        @XmlElement
        private List<String> arguments;

        @XmlTransient
        private Map<String, Object> variables;

        public void setPath(final String path) {
            this.path = path;
        }

        public String getPath() {
            return path;
        }

        public void setArguments(final List<String> arguments) {
            this.arguments = arguments;
        }

        public List<String> getArguments() {
            return arguments == null ? new ArrayList<String>() : arguments;
        }

        @XmlTransient
        public void setVariables(final Map<String, Object> variables) {
            this.variables = variables;
        }

        public Map<String, Object> getVariables() {
            return variables;
        }

        public Map<String, Object> getVariablesWithProperPrefixes() {
            if (variables == null) {
                return null;
            }
            final Map<String, Object> varsWithTypes = newLinkedHashMap();
            for (final Entry<String, Object> var : variables.entrySet()) {
                String name = var.getKey();
                if (name.matches("^[$@&]\\{.+\\}$")) {
                    name = name.substring(2, name.length() - 1);
                }

                String newName;
                if (var.getValue() instanceof Map<?, ?>) {
                    newName = "&{" + name + "}";
                } else if (var.getValue() instanceof List<?> || var.getValue() instanceof Object[]) {
                    newName = "@{" + name + "}";
                } else {
                    newName = "${" + name + "}";
                }
                varsWithTypes.put(newName, toListIfNeeded(var.getValue()));
            }
            return varsWithTypes;
        }

        private Object toListIfNeeded(final Object object) {
            return object instanceof Object[] ? newArrayList((Object[]) object) : object;
        }
        
        @Override
        public boolean equals(final Object obj) {
            if (obj == null) {
                return false;
            } else if (obj.getClass() == getClass()) {
                final ReferencedVariableFile other = (ReferencedVariableFile) obj;
                return Objects.equals(path, other.path) && Objects.equals(arguments, other.arguments);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hash(path, arguments);
        }
    }

    @XmlRootElement(name = "variable")
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class VariableMapping {

        @XmlAttribute(required = true)
        private String name;

        @XmlAttribute(required = true)
        private String value;

        public void setName(final String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public void setValue(final String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        @Override
        public boolean equals(final Object obj) {
            if (obj instanceof VariableMapping) {
                final VariableMapping that = (VariableMapping) obj;
                return Objects.equals(this.name, that.name) && Objects.equals(this.value, that.value);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, value);
        }
    }

    @XmlRootElement(name = "excludedPath")
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class ExcludedFolderPath {

        @XmlAttribute(required = true)
        private String path;

        public void setPath(final String path) {
            this.path = path;
        }

        public String getPath() {
            return path;
        }

        public IPath asPath() {
            return Path.fromPortableString(path);
        }

        @Override
        public boolean equals(final Object obj) {
            if (obj instanceof ExcludedFolderPath) {
                final ExcludedFolderPath that = (ExcludedFolderPath) obj;
                return Objects.equals(this.path, that.path);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hash(path);
        }
    }
}

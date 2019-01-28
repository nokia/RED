/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.project;

import static com.google.common.collect.Lists.newArrayList;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
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
import javax.xml.bind.annotation.XmlValue;

import org.rf.ide.core.environment.SuiteExecutor;

@XmlRootElement(name = "projectConfiguration")
@XmlType(propOrder = { "version", "executionEnvironment", "pathsRelativityPoint", "variableMappings", "libraries",
        "pythonPaths", "classPaths", "remoteLocations", "referencedVariableFiles", "excludedPaths",
        "isValidatedFileSizeCheckingEnabled", "validatedFileMaxSize", "isReferencedLibrariesAutoReloadEnabled",
        "isReferencedLibrariesAutoDiscoveringEnabled", "isLibrariesAutoDiscoveringSummaryWindowEnabled" })
@XmlAccessorType(XmlAccessType.FIELD)
public class RobotProjectConfig {

    public static final String FILENAME = "red.xml";

    public static final String CURRENT_VERSION = "2";

    private static final String VALIDATED_FILE_DEFAULT_MAX_SIZE_IN_KB = "1024";

    @XmlElement(name = "configVersion", required = true)
    private ConfigVersion version;

    @XmlElement(name = "robotExecEnvironment", required = false)
    private ExecutionEnvironment executionEnvironment;

    @XmlElement(name = "relativeTo", required = true)
    private RelativityPoint pathsRelativityPoint = new RelativityPoint();

    @XmlElement(name = "referencedLibrary", required = false)
    private List<ReferencedLibrary> libraries = new ArrayList<>();

    @XmlElement(name = "remoteLocations", required = false)
    private List<RemoteLocation> remoteLocations = new ArrayList<>();

    @XmlElementWrapper(name = "pythonpath", required = false)
    @XmlElement(name = "path", type = SearchPath.class)
    private List<SearchPath> pythonPaths = new ArrayList<>();

    @XmlElementWrapper(name = "classpath", required = false)
    @XmlElement(name = "path", type = SearchPath.class)
    private List<SearchPath> classPaths = new ArrayList<>();

    @XmlElement(name = "variableFiles", required = false)
    private List<ReferencedVariableFile> referencedVariableFiles = new ArrayList<>();

    @XmlElement(name = "variable", required = false)
    private List<VariableMapping> variableMappings = new ArrayList<>();

    @XmlElementWrapper(name = "excludedForValidation", required = false)
    @XmlElement(name = "excludedPath", type = ExcludedPath.class)
    private List<ExcludedPath> excludedPaths = new ArrayList<>();

    @XmlElement(name = "isValidatedFileSizeCheckingEnabled", required = false)
    private boolean isValidatedFileSizeCheckingEnabled = true;

    @XmlElement(name = "validatedFileMaxSize", required = false)
    private String validatedFileMaxSize = VALIDATED_FILE_DEFAULT_MAX_SIZE_IN_KB;

    @XmlElement(name = "isReferencedLibrariesAutoReloadEnabled", required = false)
    private boolean isReferencedLibrariesAutoReloadEnabled = true;

    @XmlElement(name = "isReferencedLibrariesAutoDiscoveringEnabled", required = false)
    private boolean isReferencedLibrariesAutoDiscoveringEnabled = true;

    @XmlElement(name = "isLibrariesAutoDiscoveringSummaryWindowEnabled", required = false)
    private boolean isLibrariesAutoDiscoveringSummaryWindowEnabled = false;

    public static RobotProjectConfig create() {
        final RobotProjectConfig configuration = new RobotProjectConfig();
        configuration.setVersion(CURRENT_VERSION);
        return configuration;
    }

    public boolean isNullConfig() {
        return false;
    }

    public void setVersion(final String version) {
        this.version = ConfigVersion.create(version);
    }

    public ConfigVersion getVersion() {
        return version;
    }

    public void setExecutionEnvironment(final ExecutionEnvironment executionEnvironment) {
        this.executionEnvironment = executionEnvironment;
    }

    public ExecutionEnvironment getExecutionEnvironment() {
        return executionEnvironment;
    }

    public void setRelativityPoint(final RelativityPoint relativityPoint) {
        this.pathsRelativityPoint = relativityPoint;
    }

    public RelativityPoint getRelativityPoint() {
        return pathsRelativityPoint;
    }

    public void setReferencedLibraries(final List<ReferencedLibrary> libraries) {
        this.libraries = libraries;
    }

    public List<ReferencedLibrary> getReferencedLibraries() {
        return libraries;
    }

    public boolean addReferencedLibrary(final ReferencedLibrary library) {
        if (!libraries.contains(library)) {
            libraries.add(library);
            return true;
        }
        return false;
    }

    public boolean removeReferencedLibraries(final List<ReferencedLibrary> libraries) {
        return this.libraries.removeAll(libraries);
    }

    public void setRemoteLocations(final List<RemoteLocation> remoteLocations) {
        this.remoteLocations = remoteLocations;
    }

    public List<RemoteLocation> getRemoteLocations() {
        return remoteLocations;
    }

    public boolean addRemoteLocation(final RemoteLocation remoteLocation) {
        if (!remoteLocations.contains(remoteLocation)) {
            remoteLocations.add(remoteLocation);
            return true;
        }
        return false;
    }

    public boolean removeRemoteLocations(final List<RemoteLocation> remoteLocations) {
        return this.remoteLocations.removeAll(remoteLocations);
    }

    public void setPythonPaths(final List<SearchPath> pythonPaths) {
        this.pythonPaths = pythonPaths;
    }

    public List<SearchPath> getPythonPaths() {
        return pythonPaths;
    }

    public boolean addPythonPath(final SearchPath path) {
        if (!pythonPaths.contains(path)) {
            pythonPaths.add(path);
            return true;
        }
        return false;
    }

    public boolean removePythonPaths(final List<SearchPath> paths) {
        return pythonPaths.removeAll(paths);
    }

    public void setClassPaths(final List<SearchPath> classPaths) {
        this.classPaths = classPaths;
    }

    public List<SearchPath> getClassPaths() {
        return classPaths;
    }

    public boolean addClassPath(final SearchPath path) {
        if (!classPaths.contains(path)) {
            classPaths.add(path);
            return true;
        }
        return false;
    }

    public boolean removeClassPaths(final List<SearchPath> paths) {
        return classPaths.removeAll(paths);
    }

    public void setReferencedVariableFiles(final List<ReferencedVariableFile> referencedVariableFiles) {
        this.referencedVariableFiles = referencedVariableFiles;
    }

    public List<ReferencedVariableFile> getReferencedVariableFiles() {
        return referencedVariableFiles;
    }

    public boolean addReferencedVariableFile(final ReferencedVariableFile variableFile) {
        if (!referencedVariableFiles.contains(variableFile)) {
            referencedVariableFiles.add(variableFile);
            return true;
        }
        return false;
    }

    public boolean removeReferencedVariableFiles(final List<ReferencedVariableFile> referencedVariableFiles) {
        return this.referencedVariableFiles.removeAll(referencedVariableFiles);
    }

    public void setVariableMappings(final List<VariableMapping> variableMappings) {
        this.variableMappings = variableMappings;
    }

    public List<VariableMapping> getVariableMappings() {
        return variableMappings;
    }

    public boolean addVariableMapping(final VariableMapping mapping) {
        if (!variableMappings.contains(mapping)) {
            variableMappings.add(mapping);
            return true;
        }
        return false;
    }

    public boolean removeVariableMappings(final List<VariableMapping> variableMappings) {
        return this.variableMappings.removeAll(variableMappings);
    }

    public void setExcludedPaths(final List<ExcludedPath> excludedPaths) {
        this.excludedPaths = excludedPaths;
    }

    public List<ExcludedPath> getExcludedPaths() {
        return excludedPaths;
    }

    public boolean addExcludedPath(final String path) {
        final ExcludedPath toAdd = ExcludedPath.create(path);
        if (!excludedPaths.contains(toAdd)) {
            excludedPaths.add(toAdd);
            return true;
        }
        return false;
    }

    public boolean removeExcludedPath(final String path) {
        final ExcludedPath toRemove = getExcludedPath(path);
        if (toRemove != null) {
            return excludedPaths.remove(toRemove);
        }
        return false;
    }

    public boolean isExcludedPath(final String path) {
        return getExcludedPath(path) != null;
    }

    public ExcludedPath getExcludedPath(final String path) {
        for (final ExcludedPath excludedPath : excludedPaths) {
            if (excludedPath.getPath().equals(path)) {
                return excludedPath;
            }
        }
        return null;
    }

    public void setIsValidatedFileSizeCheckingEnabled(final boolean isFileSizeCheckingEnabled) {
        this.isValidatedFileSizeCheckingEnabled = isFileSizeCheckingEnabled;
    }

    public boolean isValidatedFileSizeCheckingEnabled() {
        return this.isValidatedFileSizeCheckingEnabled;
    }

    public void setValidatedFileMaxSize(final String validatedFileMaxSize) {
        this.validatedFileMaxSize = validatedFileMaxSize;
    }

    public String getValidatedFileMaxSize() {
        return this.validatedFileMaxSize;
    }

    public static long getValidatedFileDefaultMaxSize() {
        return Long.parseLong(VALIDATED_FILE_DEFAULT_MAX_SIZE_IN_KB);
    }

    public void setIsReferencedLibrariesAutoReloadEnabled(final boolean isReferencedLibrariesAutoReloadEnabled) {
        this.isReferencedLibrariesAutoReloadEnabled = isReferencedLibrariesAutoReloadEnabled;
    }

    public boolean isReferencedLibrariesAutoReloadEnabled() {
        return this.isReferencedLibrariesAutoReloadEnabled;
    }

    public boolean isReferencedLibrariesAutoDiscoveringEnabled() {
        return isReferencedLibrariesAutoDiscoveringEnabled;
    }

    public void setReferencedLibrariesAutoDiscoveringEnabled(final boolean isReferencedLibrariesAutoDiscoveringEnabled) {
        this.isReferencedLibrariesAutoDiscoveringEnabled = isReferencedLibrariesAutoDiscoveringEnabled;
    }

    public boolean isLibrariesAutoDiscoveringSummaryWindowEnabled() {
        return isLibrariesAutoDiscoveringSummaryWindowEnabled;
    }

    public void setLibrariesAutoDiscoveringSummaryWindowEnabled(final boolean isLibrariesAutoDiscoveringSummaryWindowEnabled) {
        this.isLibrariesAutoDiscoveringSummaryWindowEnabled = isLibrariesAutoDiscoveringSummaryWindowEnabled;
    }

    public boolean usesPreferences() {
        return executionEnvironment == null;
    }

    public String providePythonLocation() {
        return executionEnvironment == null ? null : executionEnvironment.getPath();
    }

    public SuiteExecutor providePythonInterpreter() {
        return executionEnvironment == null ? null : executionEnvironment.getInterpreter();
    }

    public void assignPythonLocation(final String path, final SuiteExecutor executor) {
        if (path == null) {
            executionEnvironment = null;
            return;
        }
        if (executionEnvironment == null) {
            executionEnvironment = ExecutionEnvironment.create(path, executor);
        }
        if (!executionEnvironment.getPath().equals(path)) {
            executionEnvironment.setPath(path);
        }
        if (executionEnvironment.getInterpreter() != executor) {
            executionEnvironment.setInterpreter(executor);
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(version == null ? null : version.getVersion(),
                executionEnvironment == null ? null : executionEnvironment.path);
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
                && Objects.equals(version.getVersion(), other.version.getVersion());
    }

    @XmlRootElement(name = "configVersion")
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class ConfigVersion {

        public static ConfigVersion create(final String version) {
            final ConfigVersion configVersion = new ConfigVersion();
            configVersion.setVersion(version);
            return configVersion;
        }

        // workaround xs:simpleType -> xs:complexType, which is required in this case
        @XmlAttribute(required = false)
        private String foo;

        @XmlValue
        private String version;

        public void setVersion(final String version) {
            this.version = version;
        }

        public String getVersion() {
            return version;
        }
    }

    @XmlRootElement(name = "robotExecEnvironment")
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class ExecutionEnvironment {

        public static ExecutionEnvironment create(final String path, final SuiteExecutor executor) {
            final ExecutionEnvironment environment = new ExecutionEnvironment();
            environment.setPath(path);
            environment.setInterpreter(executor);
            return environment;
        }

        @XmlAttribute
        private String path;

        @XmlAttribute
        private String interpreter;

        public void setPath(final String path) {
            this.path = path;
        }

        public String getPath() {
            return path;
        }

        public void setInterpreter(final SuiteExecutor executor) {
            this.interpreter = executor == null ? null : executor.name();
        }

        public SuiteExecutor getInterpreter() {
            return interpreter == null ? null : SuiteExecutor.valueOf(interpreter);
        }
    }

    @XmlRootElement(name = "referencedLibrary")
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class ReferencedLibrary {

        public static ReferencedLibrary create(final LibraryType type, final String name, final String path) {
            final ReferencedLibrary library = new ReferencedLibrary();
            library.setType(type.toString());
            library.setName(name);
            library.setPath(path);
            return library;
        }

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

        @Override
        public String toString() {
            return "ReferencedLibrary [type=" + type + ", name=" + name + ", path=" + path + "]";
        }
    }

    public static enum LibraryType {
        VIRTUAL, PYTHON, JAVA
    }

    @XmlRootElement(name = "remoteLocation")
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class RemoteLocation {

        public static final String DEFAULT_ADDRESS = "http://127.0.0.1:8270/RPC2";

        public static final RemoteLocation DEFAULT_LOCATION = RemoteLocation.create(DEFAULT_ADDRESS);

        public static RemoteLocation create(final String path) {
            final RemoteLocation location = new RemoteLocation();
            location.setUri(path);
            return location;
        }

        public static RemoteLocation create(final URI path) {
            final RemoteLocation location = new RemoteLocation();
            location.setUriAddress(path);
            return location;
        }

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
                throw new IllegalArgumentException("Invalid URI '" + path + "'", e);
            }
        }

        public String getUri() {
            return uri.toString();
        }

        public String getRemoteName() {
            return "Remote " + getUri();
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
    }

    @XmlRootElement(name = "referencedVariableFile")
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class ReferencedVariableFile {

        public static ReferencedVariableFile create(final String path, final String... arguments) {
            final ReferencedVariableFile file = new ReferencedVariableFile();
            file.setPath(path);
            file.setArguments(newArrayList(arguments));
            return file;
        }

        @XmlAttribute
        private String path;

        @XmlElement
        private List<String> arguments = new ArrayList<>();;

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
            return arguments;
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
            final Map<String, Object> varsWithTypes = new LinkedHashMap<>();
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

        public static VariableMapping create(final String name, final String value) {
            final VariableMapping mapping = new VariableMapping();
            mapping.setName(name);
            mapping.setValue(value);
            return mapping;
        }

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
    public static class ExcludedPath {

        public static ExcludedPath create(final String path) {
            final ExcludedPath excludedPath = new ExcludedPath();
            excludedPath.setPath(path);
            return excludedPath;
        }

        @XmlAttribute(required = true)
        private String path;

        public void setPath(final String path) {
            this.path = path;
        }

        public String getPath() {
            return path;
        }

        @Override
        public boolean equals(final Object obj) {
            if (obj instanceof ExcludedPath) {
                final ExcludedPath that = (ExcludedPath) obj;
                return Objects.equals(this.path, that.path);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hash(path);
        }
    }

    @XmlRootElement(name = "path")
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class SearchPath {

        public static SearchPath create(final String path) {
            return create(path, false);
        }

        public static SearchPath create(final String path, final boolean isSystem) {
            final SearchPath searchPath = new SearchPath();
            searchPath.setLocation(path);
            searchPath.setSystem(isSystem);
            return searchPath;
        }

        @XmlAttribute(required = true)
        private String location;

        @XmlTransient
        private boolean isSystem = false;

        public void setLocation(final String path) {
            this.location = path;
        }

        public String getLocation() {
            return location;
        }

        @XmlTransient
        public void setSystem(final boolean isSystem) {
            this.isSystem = isSystem;
        }

        public boolean isSystem() {
            return isSystem;
        }

        @Override
        public boolean equals(final Object obj) {
            if (obj instanceof SearchPath) {
                final SearchPath that = (SearchPath) obj;
                return Objects.equals(this.location, that.location);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hash(location);
        }
    }

    @XmlRootElement(name = "relativeTo")
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class RelativityPoint {

        public static RelativityPoint create(final RelativeTo relativeTo) {
            final RelativityPoint point = new RelativityPoint();
            point.setRelativeTo(relativeTo);
            return point;
        }

        @XmlValue
        private RelativeTo relativeTo;

        public RelativityPoint() {
            this.relativeTo = RelativeTo.WORKSPACE;
        }

        public RelativeTo getRelativeTo() {
            return relativeTo;
        }

        public void setRelativeTo(final RelativeTo relativeTo) {
            this.relativeTo = relativeTo;
        }

        @Override
        public boolean equals(final Object obj) {
            if (obj instanceof RelativityPoint) {
                final RelativityPoint that = (RelativityPoint) obj;
                return Objects.equals(this.relativeTo, that.relativeTo);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hash(relativeTo);
        }
    }

    public enum RelativeTo {
        WORKSPACE,
        PROJECT
    }
}

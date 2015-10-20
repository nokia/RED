/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model;

import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Maps.newLinkedHashMap;
import static com.google.common.collect.Sets.newHashSet;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.robotframework.ide.core.executor.RobotRuntimeEnvironment;
import org.robotframework.ide.core.testData.RobotParser;
import org.robotframework.ide.core.testData.model.RobotProjectHolder;
import org.robotframework.ide.eclipse.main.plugin.PathsConverter;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig.LibraryType;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig.ReferencedLibrary;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig.ReferencedVariableFile;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig.RemoteLocation;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfigReader;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfigReader.CannotReadProjectConfigurationException;
import org.robotframework.ide.eclipse.main.plugin.project.library.LibrarySpecification;
import org.robotframework.ide.eclipse.main.plugin.project.library.LibrarySpecificationReader;
import org.robotframework.ide.eclipse.main.plugin.project.library.LibrarySpecificationReader.CannotReadlibrarySpecificationException;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Predicates;

public class RobotProject extends RobotContainer {

    private RobotProjectHolder projectHolder;
    
    private List<LibrarySpecification> stdLibsSpecs;
    private List<LibrarySpecification> refLibsSpecs;
    private List<ReferencedVariableFile> referencedVariableFiles;
    
    private RobotProjectConfig configuration;

    RobotProject(final IProject project) {
        super(null, project);
    }
    
    public synchronized RobotProjectHolder getRobotProjectHolder() {
        if (projectHolder == null) {
            projectHolder = new RobotProjectHolder(getRuntimeEnvironment());
        }
        return projectHolder;
    }

    public RobotParser getEagerRobotParser() {
        return RobotParser.createEager(getRobotProjectHolder());
    }
    
    public RobotParser getRobotParser() {
        return RobotParser.create(getRobotProjectHolder());
    }

    public IProject getProject() {
        return (IProject) container;
    }

    public String getVersion() {
        readProjectConfigurationIfNeeded();
        final RobotRuntimeEnvironment env = getRuntimeEnvironment();
        return env == null ? "???" : env.getVersion();
    }

    public synchronized boolean hasStandardLibraries() {
        readProjectConfigurationIfNeeded();
        if (stdLibsSpecs != null && !stdLibsSpecs.isEmpty()) {
            return true;
        }
        return configuration != null;
    }

    public synchronized List<LibrarySpecification> getStandardLibraries() {
        if (stdLibsSpecs != null) {
            return stdLibsSpecs;
        }
        readProjectConfigurationIfNeeded();
        final RobotRuntimeEnvironment env = getRuntimeEnvironment();
        if (env == null || configuration == null) {
            return newArrayList();
        }

        stdLibsSpecs = newArrayList(filter(transform(configuration.getRemoteLocations(), remoteLibToSpec(getProject())),
                Predicates.<LibrarySpecification> notNull()));
        stdLibsSpecs.addAll(newArrayList(filter(transform(env.getStandardLibrariesNames(), stdLibToSpec(getProject())),
                Predicates.<LibrarySpecification> notNull())));
        return stdLibsSpecs;
    }

    public synchronized boolean hasReferencedLibraries() {
        readProjectConfigurationIfNeeded();
        if (refLibsSpecs != null && !refLibsSpecs.isEmpty()) {
            return true;
        }
        return configuration != null && configuration.hasReferencedLibraries();
    }

    public synchronized List<LibrarySpecification> getReferencedLibraries() {
        if (refLibsSpecs != null) {
            return refLibsSpecs;
        }
        readProjectConfigurationIfNeeded();
        if (configuration == null) {
            return newArrayList();
        }

        refLibsSpecs = newArrayList(filter(transform(configuration.getLibraries(), libToSpec(getProject())),
                Predicates.<LibrarySpecification> notNull()));
        return refLibsSpecs;
    }

    public Map<String, LibrarySpecification> getLibrariesMapping() {
        final Map<String, LibrarySpecification> mapping = newHashMap();
        for (final LibrarySpecification specification : getStandardLibraries()) {
            mapping.put(specification.getName(), specification);
        }
        for (final LibrarySpecification specification : getReferencedLibraries()) {
            mapping.put(specification.getName(), specification);
        }
        return mapping;
    }

    public Map<String, LibrarySpecification> getStandardLibrariesMapping() {
        final Map<String, LibrarySpecification> mapping = newHashMap();
        for (final LibrarySpecification specification : getStandardLibraries()) {
            final String libName = specification.getName();
            if ("Remote".equals(libName)) {
                mapping.put(libName + " " + specification.getAdditionalInformation(), specification);
            } else {
                mapping.put(libName, specification);
            }
        }
        return mapping;
    }

    public synchronized Map<String, LibrarySpecification> getStandardLibrariesMappingWithNulls() {
        final Map<String, LibrarySpecification> mapping = newHashMap();
        readProjectConfigurationIfNeeded();
        final RobotRuntimeEnvironment env = getRuntimeEnvironment();
        if (env == null || configuration == null) {
            return mapping;
        }
        
        final Map<String, LibrarySpecification> specs = getStandardLibrariesMapping();
        
        for (final String libName : env.getStandardLibrariesNames()) {
            if ("Remote".equals(libName)) {
                final String name = libName + " " + specs.get(libName).getAdditionalInformation();
                mapping.put(name, specs.get(name));
            } else {
                mapping.put(libName, specs.get(libName));
            }
        }
        for (final RemoteLocation remoteLocation : configuration.getRemoteLocations()) {
            final String name = "Remote " + remoteLocation.getUri();
            mapping.put(name, specs.get(name));
        }
        return mapping;
    }

    public synchronized Map<ReferencedLibrary, LibrarySpecification> getReferencedLibrariesMapping() {
        readProjectConfigurationIfNeeded();
        if (configuration == null) {
            return newHashMap();
        }

        final Map<ReferencedLibrary, LibrarySpecification> spcs = newHashMap();
        for (final ReferencedLibrary library : configuration.getLibraries()) {
            final LibrarySpecification spec = libToSpec(getProject()).apply(library);
            if (spec != null) {
                spcs.put(library, spec);
            }
        }
        return spcs;
    }

    public Map<ReferencedLibrary, LibrarySpecification> getReferencedLibrariesMappingWithNulls() {
        readProjectConfigurationIfNeeded();
        if (configuration == null) {
            return newHashMap();
        }

        final Map<ReferencedLibrary, LibrarySpecification> spcs = newLinkedHashMap();
        for (final ReferencedLibrary library : configuration.getLibraries()) {
            final LibrarySpecification spec = libToSpec(getProject()).apply(library);
            spcs.put(library, spec);
        }
        return spcs;
    }

    private static Function<String, LibrarySpecification> stdLibToSpec(final IProject project) {
        return new Function<String, LibrarySpecification>() {

            @Override
            public LibrarySpecification apply(final String libraryName) {
                try {
                    final IFile file = LibspecsFolder.get(project).getSpecFile(libraryName);
                    return LibrarySpecificationReader.readSpecification(file);
                } catch (final CannotReadlibrarySpecificationException e) {
                    return null;
                }
            }
        };
    }

    private static Function<RemoteLocation, LibrarySpecification> remoteLibToSpec(final IProject project) {
        return new Function<RemoteLocation, LibrarySpecification>() {

            @Override
            public LibrarySpecification apply(final RemoteLocation remoteLocation) {
                try {
                    final IFile file = LibspecsFolder.get(project).getSpecFile(remoteLocation.createLibspecFileName());
                    return LibrarySpecificationReader.readRemoteSpecification(file, remoteLocation);
                } catch (final CannotReadlibrarySpecificationException e) {
                    return null;
                }
            }
        };
    }

    private static Function<ReferencedLibrary, LibrarySpecification> libToSpec(final IProject project) {
        return new Function<ReferencedLibrary, LibrarySpecification>() {

            @Override
            public LibrarySpecification apply(final ReferencedLibrary lib) {
                try {
                    if (lib.provideType() == LibraryType.VIRTUAL) {
                        final IPath path = Path.fromPortableString(lib.getPath());
                        final IResource libspec = project.getParent().findMember(path);
                        IFile fileToRead;

                        if (libspec != null && libspec.getType() == IResource.FILE) {
                            fileToRead = (IFile) libspec;
                            return LibrarySpecificationReader.readSpecification((IFile) libspec);
                        } else if (libspec == null) {
                            fileToRead = LibspecsFolder.get(project).getSpecFile(lib.getName());
                        } else {
                            fileToRead = null;
                        }
                        return fileToRead == null ? null : LibrarySpecificationReader.readSpecification(fileToRead);
                    } else if (lib.provideType() == LibraryType.JAVA || lib.provideType() == LibraryType.PYTHON) {
                        final IFile file = LibspecsFolder.get(project).getSpecFile(lib.getName());
                        return LibrarySpecificationReader.readReferencedSpecification(file, lib.getPath());
                    } else {
                        return null;
                    }
                } catch (final CannotReadlibrarySpecificationException e) {
                    return null;
                }
            }
        };
    }

    private synchronized RobotProjectConfig readProjectConfigurationIfNeeded() {
        if (configuration == null) {
            try {
                configuration = new RobotProjectConfigReader().readConfiguration(getProject());
            } catch (final CannotReadProjectConfigurationException e) {
                // oh well...
            }
        }
        return configuration;
    }
    
    public synchronized RobotProjectConfig getRobotProjectConfig() {
        readProjectConfigurationIfNeeded();
        return configuration;
    }

    /**
     * Clearing should be done when user changed his/hers execution environment (python+robot)
     */
    public synchronized void clearAll() {
        projectHolder = null;
        clearConfiguration();
    }

    public synchronized void clearConfiguration() {
        configuration = null;
        referencedVariableFiles = null;
        stdLibsSpecs = null;
        refLibsSpecs = null;
    }

    public synchronized RobotRuntimeEnvironment getRuntimeEnvironment() {
        readProjectConfigurationIfNeeded();
        if (configuration == null || configuration.usesPreferences()) {
            return RedPlugin.getDefault().getActiveRobotInstallation();
        }
        return RedPlugin.getDefault().getRobotInstallation(configuration.providePythonLocation());
    }

    public IFile getConfigurationFile() {
        return getProject().getFile(RobotProjectConfig.FILENAME);
    }

    public IFile getFile(final String filename) {
        return getProject().getFile(filename);
    }

    public List<File> getModuleSearchPaths() {
        return getRuntimeEnvironment().getModuleSearchPaths();
    }
    
    public synchronized List<String> getPythonpath() {
        readProjectConfigurationIfNeeded();
        if (configuration != null) {
            final Set<String> pp = newHashSet();
            for (final ReferencedLibrary lib : configuration.getLibraries()) {
                if (lib.provideType() == LibraryType.PYTHON) {
                    pp.add(lib.getPath());
                }
            }
            return newArrayList(pp);
        }
        return newArrayList();
    }
    
    public synchronized List<String> getClasspath() {
        readProjectConfigurationIfNeeded();
        if (configuration != null) {
            final Set<String> cp = newHashSet(".");
            for (final ReferencedLibrary lib : configuration.getLibraries()) {
                if (lib.provideType() == LibraryType.JAVA) {
                    cp.add(lib.getPath());
                }
            }
            return newArrayList(cp);
        }
        return newArrayList(".");
    }
    
    public synchronized boolean isStandardLibrary(final LibrarySpecification spec) {
        return isLibraryFrom(spec, getStandardLibraries());
    }
    
    public synchronized boolean isReferencedLibrary(final LibrarySpecification spec) {
        return isLibraryFrom(spec, getReferencedLibraries());
    }

    private boolean isLibraryFrom(final LibrarySpecification spec, final List<LibrarySpecification> libs) {
        if (libs == null) {
            return false;
        }
        for (final LibrarySpecification librarySpecification : libs) {
            if (librarySpecification == spec) {
                return true;
            }
        }
        return false;
    }
    
    public synchronized String getPythonLibraryPath(final String libName) {
        readProjectConfigurationIfNeeded();
        if (configuration != null) {
            for (final ReferencedLibrary lib : configuration.getLibraries()) {
                if (lib.provideType() == LibraryType.PYTHON && lib.getName().equals(libName)) {
                    return lib.getPath() + "/" + lib.getName() + ".py";
                }
            }
        }
        return "";
    }
    
    public List<String> getVariableFilePaths() {
        readProjectConfigurationIfNeeded();
        if (configuration != null) {
            final List<String> list = newArrayList();
            for (final ReferencedVariableFile variableFile : configuration.getReferencedVariableFiles()) {
                final String path = PathsConverter
                        .toAbsoluteFromWorkspaceRelativeIfPossible(new Path(variableFile.getPath())).toPortableString();
                final List<String> args = variableFile.getArguments();
                final String arguments = args == null || args.isEmpty() ? "" : ":" + Joiner.on(":").join(args);
                list.add(path + arguments);
            }
            return list;
        }
        return newArrayList();
    }
    
    public synchronized List<ReferencedVariableFile> getVariablesFromReferencedFiles() {
        if(referencedVariableFiles != null) {
            return referencedVariableFiles;
        }
        readProjectConfigurationIfNeeded();
        if (configuration != null) {
            referencedVariableFiles = newArrayList();
            for (final ReferencedVariableFile variableFile : configuration.getReferencedVariableFiles()) {
                final String path = variableFile.getPath();
                final Map<String, Object> varsMap = getRuntimeEnvironment().getVariablesFromFile(
                        PathsConverter.toAbsoluteFromWorkspaceRelativeIfPossible(new Path(path)).toPortableString(),
                        variableFile.getArguments());
                if (varsMap != null && !varsMap.isEmpty()) {
                    variableFile.setVariables(varsMap);
                    referencedVariableFiles.add(variableFile);
                }
            }
            return referencedVariableFiles;
        }
        return newArrayList();
    }
}

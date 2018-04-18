/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model;

import static com.google.common.base.Predicates.notNull;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Streams.concat;
import static java.util.stream.Collectors.toList;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Stream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;
import org.rf.ide.core.dryrun.RobotDryRunKeywordSource;
import org.rf.ide.core.executor.RobotRuntimeEnvironment;
import org.rf.ide.core.executor.RobotRuntimeEnvironment.RobotEnvironmentException;
import org.rf.ide.core.libraries.LibraryDescriptor;
import org.rf.ide.core.libraries.LibrarySpecification;
import org.rf.ide.core.libraries.LibrarySpecificationReader;
import org.rf.ide.core.libraries.LibrarySpecificationReader.CannotReadLibrarySpecificationException;
import org.rf.ide.core.project.ImportSearchPaths.PathsProvider;
import org.rf.ide.core.project.RobotProjectConfig;
import org.rf.ide.core.project.RobotProjectConfig.LibraryType;
import org.rf.ide.core.project.RobotProjectConfig.ReferencedLibrary;
import org.rf.ide.core.project.RobotProjectConfig.ReferencedVariableFile;
import org.rf.ide.core.project.RobotProjectConfig.SearchPath;
import org.rf.ide.core.project.RobotProjectConfigReader.CannotReadProjectConfigurationException;
import org.rf.ide.core.testdata.RobotParser;
import org.rf.ide.core.testdata.model.RobotExpressions;
import org.rf.ide.core.testdata.model.RobotProjectHolder;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.RedWorkspace;
import org.robotframework.ide.eclipse.main.plugin.project.LibrariesWatchHandler;
import org.robotframework.ide.eclipse.main.plugin.project.RedEclipseProjectConfig;
import org.robotframework.ide.eclipse.main.plugin.project.RedEclipseProjectConfig.PathResolvingException;
import org.robotframework.ide.eclipse.main.plugin.project.RedEclipseProjectConfigReader;
import org.robotframework.ide.eclipse.main.plugin.project.editor.RedProjectEditor;
import org.robotframework.ide.eclipse.main.plugin.project.editor.RedProjectEditorInput;
import org.robotframework.red.swt.SwtThread;
import org.robotframework.red.swt.SwtThread.Evaluation;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Joiner;

public class RobotProject extends RobotContainer {

    private RobotProjectHolder projectHolder;

    private RobotProjectConfig configuration;

    private Map<LibraryDescriptor, LibrarySpecification> stdLibsSpecs;
    private Map<LibraryDescriptor, LibrarySpecification> refLibsSpecs;

    private List<ReferencedVariableFile> referencedVariableFiles;

    private final LibrariesWatchHandler librariesWatchHandler;

    private final Map<String, RobotDryRunKeywordSource> kwSources = new ConcurrentHashMap<>();

    RobotProject(final RobotModel model, final IProject project) {
        super(model, project);
        this.librariesWatchHandler = new LibrariesWatchHandler(this);
    }

    public synchronized RobotProjectHolder getRobotProjectHolder() {
        if (projectHolder == null) {
            projectHolder = new RobotProjectHolder(getRuntimeEnvironment());
        }
        projectHolder.configure(getRobotProjectConfig(), getProject().getLocation().toFile());
        return projectHolder;
    }

    public RobotParser getRobotParser() {
        return RobotParser.create(getRobotProjectHolder(), createPathsProvider());
    }

    public IProject getProject() {
        return (IProject) container;
    }

    public LibspecsFolder getLibspecsFolder() {
        return LibspecsFolder.get(getProject());
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

    public synchronized Map<LibraryDescriptor, LibrarySpecification> getStandardLibraries() {
        if (stdLibsSpecs != null) {
            return stdLibsSpecs;
        }
        readProjectConfigurationIfNeeded();
        final RobotRuntimeEnvironment env = getRuntimeEnvironment();
        if (env == null || configuration == null) {
            return new LinkedHashMap<>();
        }

        final Stream<LibraryDescriptor> stdLibsDescriptorsStream = env.getStandardLibrariesNames().stream()
                .map(LibraryDescriptor::ofStandardLibrary);
        final Stream<LibraryDescriptor> remoteStdLibsDescriptorsStream = configuration.getRemoteLocations().stream()
                .map(LibraryDescriptor::ofStandardRemoteLibrary);

        stdLibsSpecs = new LinkedHashMap<>();
        concat(stdLibsDescriptorsStream, remoteStdLibsDescriptorsStream).forEach(descriptor -> {
            stdLibsSpecs.put(descriptor, libToSpec(this).apply(descriptor));
        });
        return stdLibsSpecs;
    }

    @VisibleForTesting
    public void setStandardLibraries(final Map<LibraryDescriptor, LibrarySpecification> libs) {
        stdLibsSpecs = libs;
    }

    public synchronized boolean hasReferencedLibraries() {
        readProjectConfigurationIfNeeded();
        if (refLibsSpecs != null && !refLibsSpecs.isEmpty()) {
            return true;
        }
        return configuration != null && configuration.hasReferencedLibraries();
    }

    public synchronized Map<LibraryDescriptor, LibrarySpecification> getReferencedLibraries() {
        if (refLibsSpecs != null) {
            return refLibsSpecs;
        }
        readProjectConfigurationIfNeeded();
        if (configuration == null) {
            return new LinkedHashMap<>();
        }

        refLibsSpecs = new LinkedHashMap<>();
        for (final ReferencedLibrary library : configuration.getLibraries()) {
            final LibraryDescriptor descriptor = LibraryDescriptor.ofReferencedLibrary(library);

            final LibrarySpecification spec = libToSpec(this).apply(descriptor);
            refLibsSpecs.put(descriptor, spec);

            if (spec != null) {
                librariesWatchHandler.registerLibrary(library, spec);
            }
        }
        return refLibsSpecs;
    }

    @VisibleForTesting
    public void setReferencedLibraries(final Map<LibraryDescriptor, LibrarySpecification> libs) {
        refLibsSpecs = libs;
    }

    public Stream<Entry<LibraryDescriptor, LibrarySpecification>> getLibraryEntriesStream() {
        return concat(getStandardLibraries().entrySet().stream(), getReferencedLibraries().entrySet().stream());
    }

    public Stream<LibraryDescriptor> getLibraryDescriptorsStream() {
        return concat(getStandardLibraries().keySet().stream(), getReferencedLibraries().keySet().stream());
    }

    public Stream<LibrarySpecification> getLibrarySpecificationsStream() {
        return concat(getStandardLibraries().values().stream(), getReferencedLibraries().values().stream())
                .filter(notNull());
    }

    public Collection<Entry<LibraryDescriptor, LibrarySpecification>> getLibraryEntries() {
        return getLibraryEntriesStream().collect(toList());
    }

    public Collection<LibraryDescriptor> getLibraryDescriptors() {
        return getLibraryDescriptorsStream().collect(toList());
    }

    public Collection<LibrarySpecification> getLibrarySpecifications() {
        return getLibrarySpecificationsStream().collect(toList());
    }

    public synchronized void unregisterWatchingOnReferencedLibraries(final List<ReferencedLibrary> libraries) {
        librariesWatchHandler.unregisterLibraries(libraries);
    }

    public void clearDirtyLibSpecs(final Collection<LibrarySpecification> libSpecs) {
        librariesWatchHandler.removeDirtySpecs(libSpecs);
    }

    private static Function<LibraryDescriptor, LibrarySpecification> libToSpec(final RobotProject robotProject) {
        return descriptor -> {
            try {
                File fileToRead = null;
                if (descriptor.getLibraryType() == LibraryType.VIRTUAL) {
                    final IPath path = Path.fromPortableString(descriptor.getPath());
                    if (!path.isAbsolute()) {
                        final IFile file = robotProject.getProject().getParent().getFile(path);
                        fileToRead = new File(file.getLocationURI());
                    }
                }
                if (fileToRead == null) {
                    final LibspecsFolder libspecsFolder = robotProject.getLibspecsFolder();

                    final String fileName = descriptor.generateLibspecFileName();
                    fileToRead = new File(libspecsFolder.getXmlSpecFile(fileName).getLocationURI());
                }

                final LibrarySpecification spec = LibrarySpecificationReader.readSpecification(fileToRead);
                spec.setDescriptor(descriptor);
                return spec;
            } catch (final CannotReadLibrarySpecificationException e) {
                return null;
            }
        };
    }

    private synchronized void readProjectConfigurationIfNeeded() {
        if (configuration == null) {
            try {
                configuration = new RedEclipseProjectConfigReader().readConfiguration(getProject());
            } catch (final CannotReadProjectConfigurationException e) {
                // oh well...
            }
        }
    }

    /**
     * Returns the configuration model from red.xml
     *
     * @return configuration from red.xml or null if file does not exist
     */
    public synchronized RobotProjectConfig getRobotProjectConfig() {
        readProjectConfigurationIfNeeded();
        return configuration;
    }

    @VisibleForTesting
    public void setRobotProjectConfig(final RobotProjectConfig config) {
        this.configuration = config;
    }

    /**
     * Returns the configuration model from opened editor.
     *
     * @return opened configuration or null if configuration is not opened in editor
     */
    public RobotProjectConfig getOpenedProjectConfig() {
        final RedProjectEditorInput redProjectInput = findEditorInputIfAlreadyOpened();
        if (redProjectInput != null) {
            return redProjectInput.getProjectConfiguration();
        } else {
            return null;
        }
    }

    private RedProjectEditorInput findEditorInputIfAlreadyOpened() {
        return SwtThread.syncEval(Evaluation.of(() -> {
            final IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
            if (window == null) {
                // in the meantime window could be destroyed actually..
                return null;
            }
            final IWorkbenchPage page = window.getActivePage();
            final FileEditorInput input = new FileEditorInput(getConfigurationFile());
            final IEditorPart editor = page.findEditor(input);
            return editor instanceof RedProjectEditor ? ((RedProjectEditor) editor).getRedProjectEditorInput() : null;
        }));
    }

    public void clearCachedData() {
        if (projectHolder != null) {
            projectHolder.clearModelFiles();
        }
    }

    /**
     * Clearing should be done when user changed his/hers execution environment (python+robot)
     */
    public synchronized void clearAll() {
        projectHolder = null;
        clearConfiguration();
        clearKwSources();
    }

    public synchronized void clearConfiguration() {
        configuration = null;
        referencedVariableFiles = null;
        stdLibsSpecs = null;
        refLibsSpecs = null;
    }

    public synchronized void clearKwSources() {
        kwSources.clear();
    }

    public synchronized RobotRuntimeEnvironment getRuntimeEnvironment() {
        readProjectConfigurationIfNeeded();
        if (configuration == null || configuration.usesPreferences()) {
            return RedPlugin.getDefault().getActiveRobotInstallation();
        }
        return RedPlugin.getDefault().getRobotInstallation(configuration.providePythonLocation(),
                configuration.providePythonInterpreter());
    }

    public IFile getConfigurationFile() {
        return getProject().getFile(RobotProjectConfig.FILENAME);
    }

    public IFile getFile(final String filename) {
        return getProject().getFile(filename);
    }

    public PathsProvider createPathsProvider() {
        return new ProjectPathsProvider();
    }

    public void setModuleSearchPaths(final List<File> paths) {
        getRobotProjectHolder().setModuleSearchPaths(paths);
    }

    public synchronized List<File> getModuleSearchPaths() {
        return getRobotProjectHolder().getModuleSearchPaths();
    }

    public synchronized List<String> getPythonpath() {
        readProjectConfigurationIfNeeded();
        final Set<String> pp = new LinkedHashSet<>();
        if (configuration != null) {
            pp.addAll(getReferenceLibPaths(LibraryType.PYTHON));
            pp.addAll(getAdditionalPaths(configuration.getPythonPath()));
        }
        return newArrayList(pp);
    }

    public synchronized List<String> getClasspath() {
        readProjectConfigurationIfNeeded();
        final Set<String> cp = new LinkedHashSet<>();
        cp.add(".");
        if (configuration != null) {
            cp.addAll(getReferenceLibPaths(LibraryType.JAVA));
            cp.addAll(getAdditionalPaths(configuration.getClassPath()));
        }
        return newArrayList(cp);
    }

    private synchronized List<String> getReferenceLibPaths(final LibraryType libType) {
        final List<String> paths = new ArrayList<>();
        for (final ReferencedLibrary lib : configuration.getLibraries()) {
            if (lib.provideType() == libType) {
                paths.add(RedWorkspace.Paths.toAbsoluteFromWorkspaceRelativeIfPossible(new Path(lib.getPath()))
                        .toOSString());
            }
        }
        return paths;
    }

    private synchronized List<String> getAdditionalPaths(final List<SearchPath> searchPaths) {
        final RedEclipseProjectConfig redConfig = new RedEclipseProjectConfig(configuration);
        final List<String> paths = new ArrayList<>();
        for (final SearchPath searchPath : searchPaths) {
            try {
                paths.add(redConfig.toAbsolutePath(searchPath, getProject()).getPath());
            } catch (final PathResolvingException e) {
                // we don't want to add syntax-problematic paths
            }
        }
        return paths;
    }

    public List<String> getVariableFilePaths() {
        readProjectConfigurationIfNeeded();

        final List<String> list = new ArrayList<>();
        if (configuration != null) {
            for (final ReferencedVariableFile variableFile : configuration.getReferencedVariableFiles()) {
                final String path = RedWorkspace.Paths
                        .toAbsoluteFromWorkspaceRelativeIfPossible(new Path(variableFile.getPath())).toOSString();
                final List<String> args = variableFile.getArguments();
                final String arguments = args == null || args.isEmpty() ? "" : ":" + Joiner.on(":").join(args);
                list.add(path + arguments);
            }
        }
        return list;
    }

    @VisibleForTesting
    public void setReferencedVariablesFiles(final List<ReferencedVariableFile> varFiles) {
        this.referencedVariableFiles = varFiles;
    }

    public synchronized List<ReferencedVariableFile> getVariablesFromReferencedFiles() {
        if (referencedVariableFiles != null) {
            return referencedVariableFiles;
        }
        readProjectConfigurationIfNeeded();
        if (configuration != null) {
            referencedVariableFiles = new ArrayList<>();
            for (final ReferencedVariableFile variableFile : configuration.getReferencedVariableFiles()) {
                IPath path = new Path(variableFile.getPath());
                if (!path.isAbsolute()) {
                    final IResource targetFile = getProject().getWorkspace().getRoot().findMember(path);
                    if (targetFile != null && targetFile.exists()) {
                        path = targetFile.getLocation();
                    }
                }

                try {
                    final Map<String, Object> varsMap = getRuntimeEnvironment()
                            .getVariablesFromFile(path.toPortableString(), variableFile.getArguments());
                    variableFile.setVariables(varsMap);
                    referencedVariableFiles.add(variableFile);
                } catch (final RobotEnvironmentException e) {
                    // unable to import the variables file
                }
            }
            return referencedVariableFiles;
        }
        return new ArrayList<>();
    }

    public String resolve(final String expression) {
        return RobotExpressions.resolve(getRobotProjectHolder().getVariableMappings(), expression);
    }

    public void addKeywordSource(final RobotDryRunKeywordSource keywordSource) {
        final String qualifiedKwName = keywordSource.getLibraryName() + "." + keywordSource.getName();
        kwSources.put(qualifiedKwName, keywordSource);
    }

    public synchronized Optional<RobotDryRunKeywordSource> getKeywordSource(final String qualifiedKwName) {
        return Optional.ofNullable(kwSources.get(qualifiedKwName));
    }

    private class ProjectPathsProvider implements PathsProvider {

        @Override
        public List<File> providePythonModulesSearchPaths() {
            return getModuleSearchPaths();
        }

        @Override
        public List<File> provideUserSearchPaths() {
            final RobotProjectConfig configuration = getRobotProjectConfig();
            if (configuration == null) {
                return new ArrayList<>();
            }
            final List<File> paths = new ArrayList<>();
            final RedEclipseProjectConfig redConfig = new RedEclipseProjectConfig(configuration);
            for (final SearchPath searchPath : configuration.getPythonPath()) {
                try {
                    final File searchPathParent = redConfig.toAbsolutePath(searchPath, getProject());
                    paths.add(searchPathParent);
                } catch (final PathResolvingException e) {
                    continue;
                }
            }
            return paths;
        }
    }
}

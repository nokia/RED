/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model;

import static com.google.common.base.Predicates.notNull;
import static java.util.stream.Collectors.toList;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;
import org.rf.ide.core.EnvironmentVariableReplacer;
import org.rf.ide.core.environment.IRuntimeEnvironment;
import org.rf.ide.core.environment.IRuntimeEnvironment.RuntimeEnvironmentException;
import org.rf.ide.core.environment.RobotVersion;
import org.rf.ide.core.execution.dryrun.RobotDryRunKeywordSource;
import org.rf.ide.core.libraries.LibraryDescriptor;
import org.rf.ide.core.libraries.LibrarySpecification;
import org.rf.ide.core.libraries.LibrarySpecificationReader;
import org.rf.ide.core.project.ImportSearchPaths.PathsProvider;
import org.rf.ide.core.project.NullRobotProjectConfig;
import org.rf.ide.core.project.RobotProjectConfig;
import org.rf.ide.core.project.RobotProjectConfig.LibraryType;
import org.rf.ide.core.project.RobotProjectConfig.ReferencedLibrary;
import org.rf.ide.core.project.RobotProjectConfig.ReferencedVariableFile;
import org.rf.ide.core.project.RobotProjectConfig.SearchPath;
import org.rf.ide.core.project.RobotProjectConfigReader.CannotReadProjectConfigurationException;
import org.rf.ide.core.testdata.RobotParser;
import org.rf.ide.core.testdata.model.RobotProjectHolder;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.RedWorkspace;
import org.robotframework.ide.eclipse.main.plugin.project.LibrariesWatchHandler;
import org.robotframework.ide.eclipse.main.plugin.project.RedEclipseProjectConfig;
import org.robotframework.ide.eclipse.main.plugin.project.RedEclipseProjectConfigReader;
import org.robotframework.ide.eclipse.main.plugin.project.editor.RedProjectEditor;
import org.robotframework.ide.eclipse.main.plugin.project.editor.RedProjectEditorInput;
import org.robotframework.red.swt.SwtThread;
import org.robotframework.red.swt.SwtThread.Evaluation;

import com.google.common.annotations.VisibleForTesting;

public class RobotProject extends RobotContainer {

    private RobotProjectHolder projectHolder;

    private RobotProjectConfig configuration;

    private RobotVersion parserComplianceVersion;

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

    public synchronized RobotParser getRobotParser() {
        return RobotParser.create(getRobotProjectHolder(), getRobotParserComplianceVersion(), createPathsProvider());
    }

    public IProject getProject() {
        return (IProject) container;
    }

    public LibspecsFolder getLibspecsFolder() {
        return LibspecsFolder.get(getProject());
    }

    @VisibleForTesting
    public void setRobotParserComplianceVersion(final RobotVersion version) {
        this.parserComplianceVersion = version;
    }

    public RobotVersion getRobotParserComplianceVersion() {
        if (parserComplianceVersion != null) {
            return parserComplianceVersion;
        }
        return getRuntimeEnvironment().getRobotVersion();
    }

    public synchronized Map<LibraryDescriptor, LibrarySpecification> getStandardLibraries() {
        if (stdLibsSpecs != null) {
            return stdLibsSpecs;
        }

        final Stream<LibraryDescriptor> stdLibsDescriptorsStream = getRuntimeEnvironment().getStandardLibrariesNames()
                .stream()
                .map(LibraryDescriptor::ofStandardLibrary);
        final Stream<LibraryDescriptor> remoteStdLibsDescriptorsStream = getRobotProjectConfig().getRemoteLocations()
                .stream()
                .map(LibraryDescriptor::ofStandardRemoteLibrary);

        stdLibsSpecs = new LinkedHashMap<>();
        Stream.concat(stdLibsDescriptorsStream, remoteStdLibsDescriptorsStream).forEach(descriptor -> {
            stdLibsSpecs.put(descriptor, findLibSpec(descriptor));
        });
        return stdLibsSpecs;
    }

    @VisibleForTesting
    public void setStandardLibraries(final Map<LibraryDescriptor, LibrarySpecification> libs) {
        stdLibsSpecs = libs;
    }

    public synchronized boolean hasReferencedLibraries() {
        if (refLibsSpecs != null && !refLibsSpecs.isEmpty()) {
            return true;
        }
        return getRobotProjectConfig().hasReferencedLibraries();
    }

    public synchronized Map<LibraryDescriptor, LibrarySpecification> getReferencedLibraries() {
        if (refLibsSpecs != null) {
            return refLibsSpecs;
        }

        refLibsSpecs = new LinkedHashMap<>();
        for (final ReferencedLibrary library : getRobotProjectConfig().getLibraries()) {
            final LibraryDescriptor descriptor = LibraryDescriptor.ofReferencedLibrary(library);

            final LibrarySpecification spec = findLibSpec(descriptor);
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
        return Stream.concat(getStandardLibraries().entrySet().stream(), getReferencedLibraries().entrySet().stream());
    }

    public Stream<LibraryDescriptor> getLibraryDescriptorsStream() {
        return Stream.concat(getStandardLibraries().keySet().stream(), getReferencedLibraries().keySet().stream());
    }

    public Stream<LibrarySpecification> getLibrarySpecificationsStream() {
        return Stream.concat(getStandardLibraries().values().stream(), getReferencedLibraries().values().stream())
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

    private LibrarySpecification findLibSpec(final LibraryDescriptor descriptor) {
        Optional<File> fileToRead = Optional.empty();
        if (descriptor.getLibraryType() == LibraryType.VIRTUAL) {
            final IPath path = Path.fromPortableString(descriptor.getPath());
            if (!path.isAbsolute()) {
                fileToRead = RedWorkspace.getLocalFile(getProject().getParent().getFile(path));
            }
        }
        if (!fileToRead.isPresent()) {
            final LibspecsFolder libspecsFolder = getLibspecsFolder();
            final String fileName = descriptor.generateLibspecFileName();
            fileToRead = RedWorkspace.getLocalFile(libspecsFolder.getXmlSpecFile(fileName));
        }
        return fileToRead.flatMap(LibrarySpecificationReader::readSpecification).map(spec -> {
            spec.setDescriptor(descriptor);
            return spec;
        }).orElse(null);
    }

    /**
     * Returns the configuration model from red.xml
     *
     * @return configuration from red.xml or dummy non-editable configuration object if file cannot
     *         be read
     */
    public synchronized RobotProjectConfig getRobotProjectConfig() {
        if (configuration == null) {
            try {
                configuration = new RedEclipseProjectConfigReader().readConfiguration(getProject());
            } catch (final CannotReadProjectConfigurationException e) {
                return new NullRobotProjectConfig();
            }
        }
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

    public synchronized IRuntimeEnvironment getRuntimeEnvironment() {
        final RobotProjectConfig config = getRobotProjectConfig();
        if (config.usesPreferences()) {
            return RedPlugin.getDefault().getActiveRobotInstallation();
        }
        final Path path = new Path(config.providePythonLocation());
        final File file = RedWorkspace.Paths.toAbsoluteFromWorkspaceRelativeIfPossible(path).toFile();
        return RedPlugin.getDefault().getRobotInstallation(file, config.providePythonInterpreter());
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

    public synchronized List<File> getModuleSearchPaths() {
        return getRobotProjectHolder().getModuleSearchPaths();
    }

    @VisibleForTesting
    public void setReferencedVariablesFiles(final List<ReferencedVariableFile> varFiles) {
        this.referencedVariableFiles = varFiles;
    }

    public synchronized List<ReferencedVariableFile> getVariablesFromReferencedFiles() {
        if (referencedVariableFiles != null) {
            return referencedVariableFiles;
        }
        final RobotProjectConfig config = getRobotProjectConfig();
        final IRuntimeEnvironment env = getRuntimeEnvironment();

        referencedVariableFiles = new ArrayList<>();
        for (final ReferencedVariableFile variableFile : config.getReferencedVariableFiles()) {
            IPath path = new Path(variableFile.getPath());
            if (!path.isAbsolute()) {
                final IResource targetFile = getProject().getWorkspace().getRoot().findMember(path);
                if (targetFile != null && targetFile.exists()) {
                    path = targetFile.getLocation();
                }
            }

            try {
                final Map<String, Object> varsMap = env.getVariablesFromFile(path.toFile(),
                        variableFile.getArguments());
                variableFile.setVariables(varsMap);
                referencedVariableFiles.add(variableFile);
            } catch (final RuntimeEnvironmentException e) {
                // unable to import the variables file
            }
        }
        return referencedVariableFiles;
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
        public boolean targetExists(final URI uri) {
            if (uri.getScheme().equalsIgnoreCase("file")) {
                try {
                    return new File(uri).exists();
                } catch (final IllegalArgumentException e) {
                    return false;
                }
            } else {
                final IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
                return Stream
                        .concat(Stream.of(root.findFilesForLocationURI(uri)),
                                Stream.of(root.findContainersForLocationURI(uri)))
                        .filter(IResource::exists)
                        .findFirst()
                        .isPresent();
            }
        }

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
            final EnvironmentVariableReplacer variableReplacer = new EnvironmentVariableReplacer();
            final RedEclipseProjectConfig redConfig = new RedEclipseProjectConfig(getProject(), configuration);
            return configuration.getPythonPath()
                    .stream()
                    .map(SearchPath::getLocation)
                    .map(variableReplacer::replaceKnownEnvironmentVariables)
                    .map(Path::new)
                    .map(redConfig::toAbsolutePath)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(toList());
        }
    }
}

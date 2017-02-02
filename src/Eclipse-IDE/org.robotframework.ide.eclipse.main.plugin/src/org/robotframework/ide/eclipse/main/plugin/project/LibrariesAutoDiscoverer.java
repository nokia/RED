/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project;

import static com.google.common.collect.Lists.newArrayList;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.rf.ide.core.dryrun.RobotDryRunLibraryImport;
import org.rf.ide.core.dryrun.RobotDryRunLibraryImport.DryRunLibraryImportStatus;
import org.rf.ide.core.dryrun.RobotDryRunLibraryImport.DryRunLibraryType;
import org.rf.ide.core.executor.EnvironmentSearchPaths;
import org.rf.ide.core.executor.RobotRuntimeEnvironment.RobotEnvironmentException;
import org.rf.ide.core.project.RobotProjectConfig;
import org.rf.ide.core.project.RobotProjectConfig.LibraryType;
import org.rf.ide.core.project.RobotProjectConfig.ReferencedLibrary;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.RedWorkspace;
import org.robotframework.ide.eclipse.main.plugin.launch.RobotLaunchConfigurationDelegate;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.project.editor.libraries.ILibraryClass;
import org.robotframework.ide.eclipse.main.plugin.project.editor.libraries.JarStructureBuilder;
import org.robotframework.ide.eclipse.main.plugin.project.editor.libraries.PythonLibStructureBuilder;
import org.robotframework.red.swt.SwtThread;

import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.google.common.io.Files;

/**
 * @author mmarzec
 */
public class LibrariesAutoDiscoverer extends AbstractAutoDiscoverer {

    private IEventBroker eventBroker;

    private final boolean showSummary;

    private Optional<String> libraryNameToDiscover = Optional.absent();

    public LibrariesAutoDiscoverer(final RobotProject robotProject, final Collection<? extends IResource> suiteFiles,
            final IEventBroker eventBroker) {
        this(robotProject, suiteFiles, eventBroker, null);
    }

    public LibrariesAutoDiscoverer(final RobotProject robotProject, final Collection<? extends IResource> suiteFiles,
            final IEventBroker eventBroker, final String libraryNameToDiscover) {
        this(robotProject, suiteFiles, true);
        this.eventBroker = eventBroker;
        this.libraryNameToDiscover = Optional.fromNullable(Strings.emptyToNull(libraryNameToDiscover));
    }

    public LibrariesAutoDiscoverer(final RobotProject robotProject, final Collection<? extends IResource> suiteFiles,
            final boolean showSummary) {
        super(robotProject, suiteFiles);
        this.showSummary = showSummary;
    }

    @Override
    public void start(final Shell parent) {
        if (startDryRun()) {
            final WorkspaceJob wsJob = new WorkspaceJob("Discovering libraries") {

                @Override
                public IStatus runInWorkspace(final IProgressMonitor monitor) throws CoreException {
                    try {
                        startDiscovering(monitor, new DryRunTargetsCollector());
                        if (monitor.isCanceled()) {
                            return Status.CANCEL_STATUS;
                        }
                        final List<RobotDryRunLibraryImport> importedLibraries = getImportedLibraries();
                        startAddingLibrariesToProjectConfiguration(monitor, importedLibraries);
                        if (showSummary) {
                            showSummary(parent, importedLibraries);
                        }
                    } catch (final InvocationTargetException e) {
                        MessageDialog.openError(parent, "Discovering libraries",
                                "Problems occurred during discovering libraries: " + e.getCause().getMessage());
                    } finally {
                        stopDryRun();
                    }

                    return Status.OK_STATUS;
                }

                @Override
                protected void canceling() {
                    dryRunHandler.destroyDryRunProcess();
                }
            };
            wsJob.setUser(true);
            wsJob.schedule();
        }
    }

    private List<RobotDryRunLibraryImport> getImportedLibraries() {
        final List<RobotDryRunLibraryImport> importedLibraries = dryRunOutputParser.getImportedLibraries();
        if (libraryNameToDiscover.isPresent()) {
            return LibraryImportFilter.filterByName(importedLibraries, libraryNameToDiscover.get());
        }
        return importedLibraries;
    }

    private void startAddingLibrariesToProjectConfiguration(final IProgressMonitor monitor,
            final List<RobotDryRunLibraryImport> importedLibraries) {
        if (!importedLibraries.isEmpty()) {
            final RobotProjectConfigUpdater configUpdater = RobotProjectConfigUpdater.create(robotProject);
            final List<RobotDryRunLibraryImport> librariesToAdd = configUpdater.getLibrariesToAdd(importedLibraries);

            final SubMonitor subMonitor = SubMonitor.convert(monitor);
            subMonitor.setWorkRemaining(librariesToAdd.size() + 1);
            for (final RobotDryRunLibraryImport libraryImport : librariesToAdd) {
                subMonitor.subTask("Adding discovered library to project configuration: " + libraryImport.getName());
                configUpdater.addLibrary(libraryImport);
                subMonitor.worked(1);
            }

            subMonitor.subTask("Updating project configuration...");
            configUpdater.update(eventBroker);
            subMonitor.worked(1);
            subMonitor.done();
        }
    }

    private void showSummary(final Shell parent, final List<RobotDryRunLibraryImport> importedLibraries) {
        SwtThread.syncExec(new Runnable() {

            @Override
            public void run() {
                new LibrariesAutoDiscovererWindow(parent, importedLibraries).open();
            }
        });
    }

    private class DryRunTargetsCollector implements IDryRunTargetsCollector {

        private final List<String> suiteNames = newArrayList();

        private final List<String> additionalProjectsLocations = newArrayList();

        @Override
        public void collectSuiteNamesAndAdditionalProjectsLocations() {
            final List<String> resourcesPaths = newArrayList();
            for (final IResource resource : suiteFiles) {
                RobotSuiteFile suiteFile = null;
                if (resource.getType() == IResource.FILE) {
                    suiteFile = RedPlugin.getModelManager().createSuiteFile((IFile) resource);
                }
                if (suiteFile != null && suiteFile.isResourceFile()) {
                    final IPath resourceFilePath = RedWorkspace.Paths
                            .toWorkspaceRelativeIfPossible(resource.getProjectRelativePath());
                    resourcesPaths.add(resourceFilePath.toString());
                } else {
                    if (resource.isLinked()) {
                        collectLinkedSuiteNamesAndProjectsLocations(resource);
                    } else {
                        suiteNames.add(RobotLaunchConfigurationDelegate.createSuiteName(resource));
                    }
                }
            }
            if (!resourcesPaths.isEmpty()) {
                final File tempSuiteFileWithResources = dryRunHandler.createTempSuiteFile(resourcesPaths,
                        new ArrayList<String>());
                if (tempSuiteFileWithResources != null) {
                    suiteNames.add(Files.getNameWithoutExtension(tempSuiteFileWithResources.getPath()));
                    additionalProjectsLocations.add(tempSuiteFileWithResources.getParent());
                }
            }
        }

        private void collectLinkedSuiteNamesAndProjectsLocations(final IResource resource) {
            final IPath linkedFileLocation = resource.getLocation();
            if (linkedFileLocation != null) {
                final File linkedFile = linkedFileLocation.toFile();
                if (linkedFile.exists()) {
                    suiteNames.add(Files.getNameWithoutExtension(linkedFile.getName()));
                    final String linkedFileParentPath = linkedFile.getParent();
                    if (!additionalProjectsLocations.contains(linkedFileParentPath)) {
                        additionalProjectsLocations.add(linkedFileParentPath);
                    }
                }
            }
        }

        @Override
        public List<String> getSuiteNames() {
            return suiteNames;
        }

        @Override
        public List<String> getAdditionalProjectsLocations() {
            return additionalProjectsLocations;
        }

    }

    private static class LibraryImportFilter {

        static List<RobotDryRunLibraryImport> filterByName(final List<RobotDryRunLibraryImport> importedLibraries,
                final String libraryName) {
            for (final RobotDryRunLibraryImport libraryImport : importedLibraries) {
                if (libraryImport.getName().equalsIgnoreCase(libraryName)) {
                    return Collections.singletonList(libraryImport);
                }
            }
            return Collections.emptyList();
        }

        static List<RobotDryRunLibraryImport> filterKnown(final List<RobotDryRunLibraryImport> importedLibraries) {
            final List<RobotDryRunLibraryImport> result = newArrayList();
            for (final RobotDryRunLibraryImport libraryImport : importedLibraries) {
                if (libraryImport.getType() != DryRunLibraryType.UNKNOWN) {
                    result.add(libraryImport);
                }
            }
            return result;
        }

        static List<RobotDryRunLibraryImport> filterNotExisting(final List<RobotDryRunLibraryImport> importedLibraries,
                final List<ReferencedLibrary> existingLibraries) {
            final List<RobotDryRunLibraryImport> result = newArrayList();
            final List<String> existingLibraryNames = newArrayList();
            for (final ReferencedLibrary existingLibrary : existingLibraries) {
                existingLibraryNames.add(existingLibrary.getName());
            }
            for (final RobotDryRunLibraryImport libraryImport : importedLibraries) {
                if (!existingLibraryNames.contains(libraryImport.getName())) {
                    result.add(libraryImport);
                } else {
                    libraryImport.setStatusAndAdditionalInfo(DryRunLibraryImportStatus.ALREADY_EXISTING,
                            "Library '" + libraryImport.getName() + "' already existing in project configuration.");
                }
            }
            return result;
        }

    }

    private static class RobotProjectConfigUpdater {

        private final RobotProject project;

        private final RobotProjectConfig config;

        private final boolean persistConfig;

        private final List<ReferencedLibrary> addedLibs = new ArrayList<>();

        private RobotProjectConfigUpdater(final RobotProject project, final RobotProjectConfig config,
                final boolean persistConfig) {
            this.project = project;
            this.config = config;
            this.persistConfig = persistConfig;
        }

        static RobotProjectConfigUpdater create(final RobotProject robotProject) {
            RobotProjectConfig config = robotProject.getOpenedProjectConfig();
            if (config == null) {
                config = new RedEclipseProjectConfigReader().readConfiguration(robotProject.getConfigurationFile());
                return new RobotProjectConfigUpdater(robotProject, config, true);
            }
            return new RobotProjectConfigUpdater(robotProject, config, false);
        }

        List<RobotDryRunLibraryImport> getLibrariesToAdd(final List<RobotDryRunLibraryImport> importedLibraries) {
            final List<RobotDryRunLibraryImport> knownLibraries = LibraryImportFilter.filterKnown(importedLibraries);
            return LibraryImportFilter.filterNotExisting(knownLibraries, config.getLibraries());
        }

        void addLibrary(final RobotDryRunLibraryImport dryRunLibraryImport) {
            if (dryRunLibraryImport.getType() == DryRunLibraryType.JAVA) {
                addJavaLibrary(dryRunLibraryImport);
            } else {
                addPythonLibrary(dryRunLibraryImport);
            }
        }

        void update(final IEventBroker eventBroker) {
            if (!addedLibs.isEmpty()) {
                sendProjectConfigChangedEvent(eventBroker);
                if (persistConfig) {
                    new RedEclipseProjectConfigWriter().writeConfiguration(config, project);
                }
            }
        }

        private void sendProjectConfigChangedEvent(IEventBroker eventBroker) {
            final RedProjectConfigEventData<List<ReferencedLibrary>> eventData = new RedProjectConfigEventData<>(
                    project.getConfigurationFile(), addedLibs);
            if (eventBroker == null) {
                eventBroker = PlatformUI.getWorkbench().getService(IEventBroker.class);
            }
            if (eventBroker != null) {
                eventBroker.send(RobotProjectConfigEvents.ROBOT_CONFIG_LIBRARIES_STRUCTURE_CHANGED, eventData);
            }
        }

        private void addPythonLibrary(final RobotDryRunLibraryImport dryRunLibraryImport) {
            final PythonLibStructureBuilder pythonLibStructureBuilder = new PythonLibStructureBuilder(
                    project.getRuntimeEnvironment(), project.getRobotProjectConfig(), project.getProject());
            Collection<ILibraryClass> pythonClasses = newArrayList();
            try {
                pythonClasses = pythonLibStructureBuilder.provideEntriesFromFile(dryRunLibraryImport.getSourcePath(),
                        Optional.of(dryRunLibraryImport.getName()), true);
            } catch (final RobotEnvironmentException e) {
                final Optional<File> modulePath = findPythonLibraryModulePath(dryRunLibraryImport);
                if (modulePath.isPresent()) {
                    final Path path = new Path(modulePath.get().getPath());
                    final ReferencedLibrary newLibrary = ReferencedLibrary.create(LibraryType.PYTHON,
                            dryRunLibraryImport.getName(), path.toPortableString());
                    addReferencedLibraries(dryRunLibraryImport, Collections.singletonList(newLibrary));
                } else {
                    dryRunLibraryImport.setStatusAndAdditionalInfo(DryRunLibraryImportStatus.NOT_ADDED, e.getMessage());
                }
                return;
            }

            addReferencedLibrariesFromClasses(dryRunLibraryImport, pythonClasses);
        }

        private Optional<File> findPythonLibraryModulePath(final RobotDryRunLibraryImport dryRunLibraryImport) {
            try {
                final EnvironmentSearchPaths envSearchPaths = new RedEclipseProjectConfig(config)
                        .createEnvironmentSearchPaths(project.getProject());
                return project.getRuntimeEnvironment().getModulePath(dryRunLibraryImport.getName(), envSearchPaths);
            } catch (final RobotEnvironmentException e1) {
                // that's fine
            }
            return Optional.absent();
        }

        private void addJavaLibrary(final RobotDryRunLibraryImport dryRunLibraryImport) {
            final JarStructureBuilder jarStructureBuilder = new JarStructureBuilder(project.getRuntimeEnvironment(),
                    project.getRobotProjectConfig(), project.getProject());
            Collection<ILibraryClass> classesFromJar = newArrayList();
            try {
                classesFromJar = jarStructureBuilder.provideEntriesFromFile(dryRunLibraryImport.getSourcePath());
            } catch (final RobotEnvironmentException e) {
                dryRunLibraryImport.setStatusAndAdditionalInfo(DryRunLibraryImportStatus.NOT_ADDED, e.getMessage());
                return;
            }

            addReferencedLibrariesFromClasses(dryRunLibraryImport, classesFromJar);
        }

        private void addReferencedLibrariesFromClasses(final RobotDryRunLibraryImport dryRunLibraryImport,
                final Collection<ILibraryClass> libClasses) {
            final Collection<ReferencedLibrary> librariesToAdd = new ArrayList<>();
            if (libClasses.isEmpty()) {
                dryRunLibraryImport.setStatusAndAdditionalInfo(DryRunLibraryImportStatus.NOT_ADDED,
                        "RED was unable to find classes inside '" + dryRunLibraryImport.getSourcePath() + "' module.");
            } else {
                for (final ILibraryClass libClass : libClasses) {
                    if (libClass.getQualifiedName().equalsIgnoreCase(dryRunLibraryImport.getName())) {
                        librariesToAdd.add(libClass.toReferencedLibrary(dryRunLibraryImport.getSourcePath()));
                    }
                }
                if (librariesToAdd.isEmpty()) {
                    dryRunLibraryImport.setStatusAndAdditionalInfo(DryRunLibraryImportStatus.NOT_ADDED,
                            "RED was unable to find class '" + dryRunLibraryImport.getName() + "' inside '"
                                    + dryRunLibraryImport.getSourcePath() + "' module.");
                }
            }

            addReferencedLibraries(dryRunLibraryImport, librariesToAdd);
        }

        private void addReferencedLibraries(final RobotDryRunLibraryImport dryRunLibraryImport,
                final Collection<ReferencedLibrary> librariesToAdd) {
            for (final ReferencedLibrary library : librariesToAdd) {
                if (config.addReferencedLibrary(library)) {
                    addedLibs.add(library);
                    dryRunLibraryImport.setStatusAndAdditionalInfo(DryRunLibraryImportStatus.ADDED, "");
                }
            }
        }

    }

}

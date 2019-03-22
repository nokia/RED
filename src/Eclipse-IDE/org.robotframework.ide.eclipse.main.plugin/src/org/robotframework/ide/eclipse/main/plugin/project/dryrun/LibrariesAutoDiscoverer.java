/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.dryrun;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.ui.PlatformUI;
import org.rf.ide.core.environment.EnvironmentSearchPaths;
import org.rf.ide.core.environment.IRuntimeEnvironment.RuntimeEnvironmentException;
import org.rf.ide.core.execution.dryrun.RobotDryRunLibraryEventListener;
import org.rf.ide.core.execution.dryrun.RobotDryRunLibraryImport;
import org.rf.ide.core.execution.dryrun.RobotDryRunLibraryImport.DryRunLibraryImportStatus;
import org.rf.ide.core.execution.dryrun.RobotDryRunLibraryImport.DryRunLibraryType;
import org.rf.ide.core.execution.dryrun.RobotDryRunLibraryImportCollector;
import org.rf.ide.core.libraries.LibraryDescriptor;
import org.rf.ide.core.project.RobotProjectConfig;
import org.rf.ide.core.project.RobotProjectConfig.ExcludedPath;
import org.rf.ide.core.project.RobotProjectConfig.LibraryType;
import org.rf.ide.core.project.RobotProjectConfig.ReferencedLibrary;
import org.rf.ide.core.project.RobotProjectConfig.RemoteLocation;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.RedWorkspace;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.project.LibrariesConfigUpdater;
import org.robotframework.ide.eclipse.main.plugin.project.RedEclipseProjectConfig;
import org.robotframework.ide.eclipse.main.plugin.project.editor.libraries.ILibraryClass;
import org.robotframework.ide.eclipse.main.plugin.project.editor.libraries.ArchiveStructureBuilder;
import org.robotframework.ide.eclipse.main.plugin.project.editor.libraries.PythonLibStructureBuilder;

import com.google.common.collect.Streams;

/**
 * @author mmarzec
 */
public abstract class LibrariesAutoDiscoverer extends AbstractAutoDiscoverer {

    public static void start(final Collection<RobotSuiteFile> suites, final DiscovererFactory discovererFactory) {
        final Map<RobotProject, List<RobotSuiteFile>> suitesGroupedByProject = suites.stream()
                .collect(groupingBy(RobotSuiteFile::getRobotProject, LinkedHashMap::new, toList()));

        // TODO: for now we want to start autodiscovering only for one project, see RED-1004
        final List<LibrariesAutoDiscoverer> discoverers = suitesGroupedByProject.entrySet()
                .stream()
                .filter(entry -> entry.getKey().getRuntimeEnvironment().hasRobotInstalled())
                .limit(1)
                .map(entry -> discovererFactory.create(entry.getKey(), entry.getValue()))
                .collect(toList());

        discoverers.forEach(LibrariesAutoDiscoverer::start);
    }

    private final Consumer<Collection<RobotDryRunLibraryImport>> summaryHandler;

    private final RobotDryRunLibraryImportCollector dryRunLibraryImportCollector;

    LibrariesAutoDiscoverer(final RobotProject robotProject,
            final Consumer<Collection<RobotDryRunLibraryImport>> summaryHandler) {
        super(robotProject);
        this.summaryHandler = summaryHandler;
        this.dryRunLibraryImportCollector = new RobotDryRunLibraryImportCollector(
                robotProject.getLibraryDescriptorsStream()
                        .filter(LibraryDescriptor::isStandardLibrary)
                        .map(LibraryDescriptor::getName)
                        .collect(toSet()));
    }

    @Override
    public Job start() {
        if (lockDryRun()) {
            final WorkspaceJob wsJob = new WorkspaceJob("Discovering libraries") {

                @Override
                public IStatus runInWorkspace(final IProgressMonitor monitor) throws CoreException {
                    try {
                        prepareDiscovering(monitor);
                        if (monitor.isCanceled()) {
                            return Status.CANCEL_STATUS;
                        }
                        startDiscovering(monitor);
                        if (monitor.isCanceled()) {
                            return Status.CANCEL_STATUS;
                        }
                        final List<RobotDryRunLibraryImport> libraryImports = getImportedLibraries();
                        startAddingLibrariesToProjectConfiguration(monitor, libraryImports);
                        summaryHandler.accept(libraryImports);
                    } catch (final InterruptedException e) {
                        // fine, will simply stop dry run
                    } finally {
                        monitor.done();
                        unlockDryRun();
                    }

                    return Status.OK_STATUS;
                }

                @Override
                protected void canceling() {
                    stopDiscovering();
                }
            };
            wsJob.setUser(true);
            wsJob.schedule();
            return wsJob;
        }
        return null;
    }

    abstract void prepareDiscovering(IProgressMonitor monitor);

    List<RobotDryRunLibraryImport> getImportedLibraries() {
        return dryRunLibraryImportCollector.getImportedLibraries();
    }

    @Override
    RobotDryRunLibraryEventListener createDryRunCollectorEventListener(final Consumer<String> libNameHandler) {
        return new RobotDryRunLibraryEventListener(dryRunLibraryImportCollector, libNameHandler);
    }

    @Override
    void startDryRunClient(final int port, final File dataSource) {
        final File projectLocation = robotProject.getProject().getLocation().toFile();
        final boolean recursiveInVirtualenv = RedPlugin.getDefault()
                .getPreferences()
                .isProjectModulesRecursiveAdditionOnVirtualenvEnabled();
        final RobotProjectConfig projectConfig = robotProject.getRobotProjectConfig();
        final List<String> excludedPaths = projectConfig.getExcludedPaths()
                .stream()
                .map(ExcludedPath::getPath)
                .collect(toList());
        final EnvironmentSearchPaths additionalPaths = new RedEclipseProjectConfig(robotProject.getProject(),
                projectConfig).createExecutionEnvironmentSearchPaths();

        robotProject.getRuntimeEnvironment()
                .startLibraryAutoDiscovering(port, dataSource, projectLocation, recursiveInVirtualenv, excludedPaths,
                        additionalPaths);
    }

    void setImporters(final RobotDryRunLibraryImport libraryImport, final Collection<RobotSuiteFile> suites) {
        final Set<URI> importers = suites.stream()
                .map(RobotSuiteFile::getFile)
                .map(RedWorkspace::tryToGetLocalUri)
                .filter(uri -> uri != null)
                .collect(toSet());
        libraryImport.setImporters(importers);
    }

    private void startAddingLibrariesToProjectConfiguration(final IProgressMonitor monitor,
            final List<RobotDryRunLibraryImport> libraryImports) {
        if (!libraryImports.isEmpty()) {
            final ImportedLibrariesConfigUpdater updater = ImportedLibrariesConfigUpdater.createFor(robotProject);
            updater.adjustImportStatuses(libraryImports);
            final List<RobotDryRunLibraryImport> libraryImportsToAdd = updater.filterLibrariesToAdd(libraryImports);

            final SubMonitor subMonitor = SubMonitor.convert(monitor);
            subMonitor.subTask("Adding libraries to project configuration...");
            subMonitor.setWorkRemaining(libraryImportsToAdd.size() + 1);
            for (final RobotDryRunLibraryImport libraryImport : libraryImportsToAdd) {
                updater.addLibrary(libraryImport);
                subMonitor.worked(1);
            }

            subMonitor.subTask("Updating project configuration...");
            final IEventBroker eventBroker = PlatformUI.getWorkbench().getService(IEventBroker.class);
            updater.finalizeLibrariesAdding(eventBroker);
            subMonitor.worked(1);
        }
    }

    private static class ImportedLibrariesConfigUpdater extends LibrariesConfigUpdater {

        public static ImportedLibrariesConfigUpdater createFor(final RobotProject project) {
            final Optional<RobotProjectConfig> openedConfig = project.getOpenedProjectConfig();
            final RobotProjectConfig config = openedConfig.orElseGet(project::getRobotProjectConfig);
            return new ImportedLibrariesConfigUpdater(project, config, !openedConfig.isPresent());
        }

        private ImportedLibrariesConfigUpdater(final RobotProject project, final RobotProjectConfig config,
                final boolean isConfigClosed) {
            super(project, config, isConfigClosed);
        }

        private void adjustImportStatuses(final List<RobotDryRunLibraryImport> libraryImports) {
            final Set<String> existingLibraryNames = Streams
                    .concat(config.getReferencedLibraries().stream().map(ReferencedLibrary::getName),
                            config.getRemoteLocations().stream().map(RemoteLocation::getRemoteName))
                    .collect(toSet());
            for (final RobotDryRunLibraryImport libraryImport : libraryImports) {
                if (libraryImport.getType() == DryRunLibraryType.UNKNOWN) {
                    libraryImport.setStatus(DryRunLibraryImportStatus.NOT_ADDED);
                } else if (existingLibraryNames.contains(libraryImport.getName())) {
                    libraryImport.setStatus(DryRunLibraryImportStatus.ALREADY_EXISTING);
                }
            }
        }

        private List<RobotDryRunLibraryImport> filterLibrariesToAdd(
                final List<RobotDryRunLibraryImport> libraryImports) {
            return libraryImports.stream()
                    .filter(libraryImport -> libraryImport.getStatus() == DryRunLibraryImportStatus.ADDED)
                    .sorted((import1, import2) -> import1.getName().compareToIgnoreCase(import2.getName()))
                    .collect(toList());
        }

        private void addLibrary(final RobotDryRunLibraryImport libraryImport) {
            if (libraryImport.getType() == DryRunLibraryType.JAVA) {
                addJavaLibrary(libraryImport);
            } else if (libraryImport.getType() == DryRunLibraryType.PYTHON) {
                addPythonLibrary(libraryImport);
            } else if (libraryImport.getType() == DryRunLibraryType.REMOTE) {
                addRemoteLibrary(libraryImport);
            }
        }

        private void addPythonLibrary(final RobotDryRunLibraryImport libraryImport) {
            final PythonLibStructureBuilder pythonLibStructureBuilder = new PythonLibStructureBuilder(
                    robotProject.getRuntimeEnvironment(), robotProject.getRobotProjectConfig(),
                    robotProject.getProject());
            try {
                final Collection<ILibraryClass> libraryClasses = pythonLibStructureBuilder
                        .provideAllEntriesFromFile(libraryImport.getSource());
                addReferencedLibrariesFromClasses(libraryImport, libraryClasses);
            } catch (final RuntimeEnvironmentException e) {
                final Optional<File> modulePath = findPythonLibraryModulePath(libraryImport);
                if (modulePath.isPresent()) {
                    final Path path = new Path(modulePath.get().getPath());
                    final ReferencedLibrary newLibrary = ReferencedLibrary.create(LibraryType.PYTHON,
                            libraryImport.getName(), path.toPortableString());
                    addLibraries(Collections.singletonList(newLibrary));
                } else {
                    libraryImport.setStatus(DryRunLibraryImportStatus.NOT_ADDED);
                    libraryImport.setAdditionalInfo(e.getMessage());
                }
            }
        }

        private Optional<File> findPythonLibraryModulePath(final RobotDryRunLibraryImport libraryImport) {
            try {
                final EnvironmentSearchPaths additionalPaths = new RedEclipseProjectConfig(robotProject.getProject(),
                        config).createAdditionalEnvironmentSearchPaths();
                return robotProject.getRuntimeEnvironment().getModulePath(libraryImport.getName(), additionalPaths);
            } catch (final RuntimeEnvironmentException e) {
                return Optional.empty();
            }
        }

        private void addJavaLibrary(final RobotDryRunLibraryImport libraryImport) {
            final ArchiveStructureBuilder jarStructureBuilder = new ArchiveStructureBuilder(
                    robotProject.getRuntimeEnvironment(), robotProject.getRobotProjectConfig(),
                    robotProject.getProject());
            try {
                final Collection<ILibraryClass> libraryClasses = jarStructureBuilder
                        .provideEntriesFromFile(libraryImport.getSource());
                addReferencedLibrariesFromClasses(libraryImport, libraryClasses);
            } catch (final RuntimeEnvironmentException e) {
                libraryImport.setStatus(DryRunLibraryImportStatus.NOT_ADDED);
                libraryImport.setAdditionalInfo(e.getMessage());
            }
        }

        private void addRemoteLibrary(final RobotDryRunLibraryImport libraryImport) {
            if (libraryImport.getSource() != null) {
                addRemoteLocation(RemoteLocation.create(libraryImport.getSource()));
            } else {
                libraryImport.setStatus(DryRunLibraryImportStatus.NOT_ADDED);
                libraryImport.setAdditionalInfo("Invalid number of arguments during Remote library import");
            }
        }

        private void addReferencedLibrariesFromClasses(final RobotDryRunLibraryImport libraryImport,
                final Collection<ILibraryClass> libraryClasses) {
            final Collection<ReferencedLibrary> librariesToAdd = new ArrayList<>();
            for (final ILibraryClass libraryClass : libraryClasses) {
                if (libraryClass.getQualifiedName().equalsIgnoreCase(libraryImport.getName())) {
                    librariesToAdd.add(libraryClass.toReferencedLibrary(libraryImport.getSource().getPath()));
                }
            }
            if (!librariesToAdd.isEmpty()) {
                addLibraries(librariesToAdd);
            } else {
                libraryImport.setStatus(DryRunLibraryImportStatus.NOT_ADDED);
                libraryImport.setAdditionalInfo("RED was unable to find class '" + libraryImport.getName()
                        + "' inside '" + libraryImport.getSource().toString() + "' module.");
            }
        }

    }

    @FunctionalInterface
    public interface DiscovererFactory {

        LibrariesAutoDiscoverer create(RobotProject project, Collection<RobotSuiteFile> suites);
    }

}

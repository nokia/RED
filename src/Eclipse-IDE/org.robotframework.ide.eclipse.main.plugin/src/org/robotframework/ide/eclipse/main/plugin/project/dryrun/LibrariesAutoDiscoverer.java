/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.dryrun;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.rf.ide.core.dryrun.RobotDryRunLibraryEventListener;
import org.rf.ide.core.dryrun.RobotDryRunLibraryImport;
import org.rf.ide.core.dryrun.RobotDryRunLibraryImport.DryRunLibraryImportStatus;
import org.rf.ide.core.dryrun.RobotDryRunLibraryImport.DryRunLibraryType;
import org.rf.ide.core.dryrun.RobotDryRunLibraryImportCollector;
import org.rf.ide.core.dryrun.RobotDryRunTemporarySuites;
import org.rf.ide.core.executor.EnvironmentSearchPaths;
import org.rf.ide.core.executor.RobotRuntimeEnvironment.RobotEnvironmentException;
import org.rf.ide.core.project.RobotProjectConfig.LibraryType;
import org.rf.ide.core.project.RobotProjectConfig.ReferencedLibrary;
import org.robotframework.ide.eclipse.main.plugin.RedWorkspace;
import org.robotframework.ide.eclipse.main.plugin.launch.RobotPathsNaming;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.project.LibrariesConfigUpdater;
import org.robotframework.ide.eclipse.main.plugin.project.RedEclipseProjectConfig;
import org.robotframework.ide.eclipse.main.plugin.project.editor.libraries.ILibraryClass;
import org.robotframework.ide.eclipse.main.plugin.project.editor.libraries.JarStructureBuilder;
import org.robotframework.ide.eclipse.main.plugin.project.editor.libraries.PythonLibStructureBuilder;
import org.robotframework.red.swt.SwtThread;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import com.google.common.io.Files;

/**
 * @author mmarzec
 */
public class LibrariesAutoDiscoverer extends AbstractAutoDiscoverer {

    private final boolean showSummary;

    private final Optional<String> libraryNameToDiscover;

    private final RobotDryRunLibraryImportCollector dryRunLibraryImportCollector;

    public LibrariesAutoDiscoverer(final RobotProject robotProject, final Collection<RobotSuiteFile> suites) {
        this(robotProject, suites, true, null);
    }

    public LibrariesAutoDiscoverer(final RobotProject robotProject, final Collection<RobotSuiteFile> suites,
            final String libraryNameToDiscover) {
        this(robotProject, suites, true, libraryNameToDiscover);
    }

    public LibrariesAutoDiscoverer(final RobotProject robotProject, final Collection<RobotSuiteFile> suites,
            final boolean showSummary) {
        this(robotProject, suites, showSummary, null);
    }

    @VisibleForTesting
    LibrariesAutoDiscoverer(final RobotProject robotProject, final Collection<RobotSuiteFile> suites,
            final boolean showSummary, final String libraryNameToDiscover) {
        super(robotProject, suites, new LibrariesSourcesCollector(robotProject), new DryRunTargetsCollector());
        this.showSummary = showSummary;
        this.libraryNameToDiscover = Optional.ofNullable(Strings.emptyToNull(libraryNameToDiscover));
        this.dryRunLibraryImportCollector = new RobotDryRunLibraryImportCollector(
                robotProject.getStandardLibraries().keySet());
    }

    @Override
    RobotDryRunLibraryEventListener createDryRunCollectorEventListener(final Consumer<String> startSuiteHandler) {
        return new RobotDryRunLibraryEventListener(dryRunLibraryImportCollector, startSuiteHandler);
    }

    @Override
    Job start(final Shell parent) {
        if (lockDryRun()) {
            final WorkspaceJob wsJob = new WorkspaceJob("Discovering libraries") {

                @Override
                public IStatus runInWorkspace(final IProgressMonitor monitor) throws CoreException {
                    try {
                        startDiscovering(monitor);
                        if (monitor.isCanceled()) {
                            return Status.CANCEL_STATUS;
                        }
                        final List<RobotDryRunLibraryImport> libraryImports = getLibraryImportsToProcess();
                        startAddingLibrariesToProjectConfiguration(monitor, libraryImports);
                        if (showSummary) {
                            showSummary(parent, libraryImports);
                        }
                    } catch (final CoreException e) {
                        throw new AutoDiscovererException("Problems occurred during discovering libraries.", e);
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

    private List<RobotDryRunLibraryImport> getLibraryImportsToProcess() {
        final List<RobotDryRunLibraryImport> libraryImports = dryRunLibraryImportCollector.getImportedLibraries();
        if (libraryNameToDiscover.isPresent()) {
            for (final RobotDryRunLibraryImport libraryImport : libraryImports) {
                if (libraryImport.getName().equalsIgnoreCase(libraryNameToDiscover.get())) {
                    return Collections.singletonList(libraryImport);
                }
            }
            return Collections.emptyList();
        }
        return libraryImports;
    }

    private void startAddingLibrariesToProjectConfiguration(final IProgressMonitor monitor,
            final List<RobotDryRunLibraryImport> libraryImports) {
        if (!libraryImports.isEmpty()) {
            final ImportedLibrariesConfigUpdater updater = new ImportedLibrariesConfigUpdater(robotProject);
            final List<RobotDryRunLibraryImport> libraryImportsToAdd = updater.getLibraryImportsToAdd(libraryImports);

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

    private void showSummary(final Shell parent, final List<RobotDryRunLibraryImport> libraryImports) {
        SwtThread.syncExec(() -> new LibrariesAutoDiscovererWindow(parent, libraryImports).open());
    }

    private static class DryRunTargetsCollector implements IDryRunTargetsCollector {

        private final List<String> suiteNames = new ArrayList<>();

        private final List<File> additionalProjectsLocations = new ArrayList<>();

        @Override
        public void collectSuiteNamesAndAdditionalProjectsLocations(final RobotProject robotProject,
                final Collection<RobotSuiteFile> suites) {
            final List<String> resourcesPaths = new ArrayList<>();
            for (final RobotSuiteFile suite : suites) {
                if (suite.isResourceFile()) {
                    final IPath filePath = RedWorkspace.Paths
                            .toWorkspaceRelativeIfPossible(suite.getFile().getProjectRelativePath());
                    resourcesPaths.add(filePath.toString());
                } else if (suite.getFile().isLinked()) {
                    collectLinkedSuiteNamesAndProjectsLocations(suite.getFile().getLocation());
                } else {
                    suiteNames.add(RobotPathsNaming.createSuiteName(suite.getFile()));
                }
            }
            final Optional<File> tempSuiteFile = RobotDryRunTemporarySuites.createResourceImportFile(resourcesPaths);
            tempSuiteFile.ifPresent(file -> {
                suiteNames.add(file.getParentFile().getName() + "." + Files.getNameWithoutExtension(file.getPath()));
                additionalProjectsLocations.add(file.getParentFile());
            });
        }

        private void collectLinkedSuiteNamesAndProjectsLocations(final IPath linkedFileLocation) {
            if (linkedFileLocation != null) {
                final File linkedFile = linkedFileLocation.toFile();
                if (linkedFile.exists()) {
                    suiteNames.add(Files.getNameWithoutExtension(linkedFile.getName()));
                    final File linkedFileParentPath = linkedFile.getParentFile();
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
        public List<File> getAdditionalProjectsLocations() {
            return additionalProjectsLocations;
        }

    }

    private static class ImportedLibrariesConfigUpdater extends LibrariesConfigUpdater {

        ImportedLibrariesConfigUpdater(final RobotProject robotProject) {
            super(robotProject);
        }

        List<RobotDryRunLibraryImport> getLibraryImportsToAdd(final List<RobotDryRunLibraryImport> libraryImports) {
            final List<String> existingLibraryNames = new ArrayList<>();
            for (final ReferencedLibrary existingLibrary : config.getLibraries()) {
                existingLibraryNames.add(existingLibrary.getName());
            }

            final List<RobotDryRunLibraryImport> result = new ArrayList<>();
            for (final RobotDryRunLibraryImport libraryImport : libraryImports) {
                if (libraryImport.getType() != DryRunLibraryType.UNKNOWN) {
                    if (existingLibraryNames.contains(libraryImport.getName())) {
                        libraryImport.setStatus(DryRunLibraryImportStatus.ALREADY_EXISTING);
                    } else {
                        result.add(libraryImport);
                    }
                }
            }
            return result;
        }

        void addLibrary(final RobotDryRunLibraryImport libraryImport) {
            if (libraryImport.getType() == DryRunLibraryType.JAVA) {
                addJavaLibrary(libraryImport);
            } else if (libraryImport.getType() == DryRunLibraryType.PYTHON) {
                addPythonLibrary(libraryImport);
            }
        }

        private void addPythonLibrary(final RobotDryRunLibraryImport libraryImport) {
            final PythonLibStructureBuilder pythonLibStructureBuilder = new PythonLibStructureBuilder(
                    robotProject.getRuntimeEnvironment(), robotProject.getRobotProjectConfig(),
                    robotProject.getProject());
            try {
                final Collection<ILibraryClass> libraryClasses = pythonLibStructureBuilder
                        .provideEntriesFromFile(libraryImport.getSourcePath(), libraryImport.getName());
                addReferencedLibrariesFromClasses(libraryImport, libraryClasses);
            } catch (final RobotEnvironmentException e) {
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
                final EnvironmentSearchPaths envSearchPaths = new RedEclipseProjectConfig(config)
                        .createEnvironmentSearchPaths(robotProject.getProject());
                return robotProject.getRuntimeEnvironment().getModulePath(libraryImport.getName(), envSearchPaths);
            } catch (final RobotEnvironmentException e1) {
                return Optional.empty();
            }
        }

        private void addJavaLibrary(final RobotDryRunLibraryImport libraryImport) {
            final JarStructureBuilder jarStructureBuilder = new JarStructureBuilder(
                    robotProject.getRuntimeEnvironment(), robotProject.getRobotProjectConfig(),
                    robotProject.getProject());
            try {
                final Collection<ILibraryClass> libraryClasses = jarStructureBuilder
                        .provideEntriesFromFile(libraryImport.getSourcePath());
                addReferencedLibrariesFromClasses(libraryImport, libraryClasses);
            } catch (final RobotEnvironmentException e) {
                libraryImport.setStatus(DryRunLibraryImportStatus.NOT_ADDED);
                libraryImport.setAdditionalInfo(e.getMessage());
            }
        }

        private void addReferencedLibrariesFromClasses(final RobotDryRunLibraryImport libraryImport,
                final Collection<ILibraryClass> libraryClasses) {
            final Collection<ReferencedLibrary> librariesToAdd = new ArrayList<>();
            for (final ILibraryClass libraryClass : libraryClasses) {
                if (libraryClass.getQualifiedName().equalsIgnoreCase(libraryImport.getName())) {
                    librariesToAdd.add(libraryClass.toReferencedLibrary(libraryImport.getSourcePath().getPath()));
                }
            }
            if (!librariesToAdd.isEmpty()) {
                addLibraries(librariesToAdd);
            } else {
                libraryImport.setStatus(DryRunLibraryImportStatus.NOT_ADDED);
                libraryImport.setAdditionalInfo("RED was unable to find class '" + libraryImport.getName()
                        + "' inside '" + libraryImport.getSourcePath() + "' module.");
            }
        }

    }

    @FunctionalInterface
    public interface DiscovererFactory {

        LibrariesAutoDiscoverer create(RobotProject project, Collection<RobotSuiteFile> suites);
    }

}

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
import org.rf.ide.core.project.RobotProjectConfig.LibraryType;
import org.rf.ide.core.project.RobotProjectConfig.ReferencedLibrary;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.RedWorkspace;
import org.robotframework.ide.eclipse.main.plugin.launch.RobotSuitesNaming;
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

    private final IEventBroker eventBroker;

    private final boolean showSummary;

    private final Optional<String> libraryNameToDiscover;

    public LibrariesAutoDiscoverer(final RobotProject robotProject, final Collection<? extends IResource> suiteFiles,
            final IEventBroker eventBroker) {
        this(robotProject, suiteFiles, eventBroker, true, null);
    }

    public LibrariesAutoDiscoverer(final RobotProject robotProject, final Collection<? extends IResource> suiteFiles,
            final IEventBroker eventBroker, final String libraryNameToDiscover) {
        this(robotProject, suiteFiles, eventBroker, true, libraryNameToDiscover);
    }

    public LibrariesAutoDiscoverer(final RobotProject robotProject, final Collection<? extends IResource> suiteFiles,
            final boolean showSummary) {
        this(robotProject, suiteFiles, null, showSummary, null);
    }

    private LibrariesAutoDiscoverer(final RobotProject robotProject, final Collection<? extends IResource> suiteFiles,
            final IEventBroker eventBroker, final boolean showSummary, final String libraryNameToDiscover) {
        super(robotProject, suiteFiles);
        this.eventBroker = eventBroker == null ? PlatformUI.getWorkbench().getService(IEventBroker.class) : eventBroker;
        this.showSummary = showSummary;
        this.libraryNameToDiscover = Optional.fromNullable(Strings.emptyToNull(libraryNameToDiscover));
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
                        final List<RobotDryRunLibraryImport> libraryImports = getLibraryImportsToProcess();
                        startAddingLibrariesToProjectConfiguration(monitor, libraryImports);
                        if (showSummary) {
                            showSummary(parent, libraryImports);
                        }
                    } catch (final InvocationTargetException e) {
                        MessageDialog.openError(parent, "Discovering libraries",
                                "Problems occurred during discovering libraries: " + e.getCause().getMessage());
                        return Status.CANCEL_STATUS;
                    } finally {
                        monitor.done();
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

    private List<RobotDryRunLibraryImport> getLibraryImportsToProcess() {
        final List<RobotDryRunLibraryImport> libraryImports = dryRunOutputParser.getImportedLibraries();
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
            updater.finalizeLibrariesAdding(eventBroker);
            subMonitor.worked(1);
        }
    }

    private void showSummary(final Shell parent, final List<RobotDryRunLibraryImport> libraryImports) {
        SwtThread.syncExec(new Runnable() {

            @Override
            public void run() {
                RedPlugin.logInfo("LIBRARY IMPORTS: \n" + libraryImports.toString());
                new LibrariesAutoDiscovererWindow(parent, libraryImports).open();
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
                        suiteNames.add(RobotSuitesNaming.createSuiteName(resource));
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

    private static class ImportedLibrariesConfigUpdater extends LibrariesConfigUpdater {

        ImportedLibrariesConfigUpdater(final RobotProject robotProject) {
            super(robotProject);
        }

        List<RobotDryRunLibraryImport> getLibraryImportsToAdd(final List<RobotDryRunLibraryImport> libraryImports) {
            final List<String> existingLibraryNames = newArrayList();
            for (final ReferencedLibrary existingLibrary : config.getLibraries()) {
                existingLibraryNames.add(existingLibrary.getName());
            }

            final List<RobotDryRunLibraryImport> result = newArrayList();
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
                final Collection<ILibraryClass> libraryClasses = pythonLibStructureBuilder.provideEntriesFromFile(
                        libraryImport.getSourcePath(), Optional.of(libraryImport.getName()), true);
                addReferencedLibrariesFromClasses(libraryImport, libraryClasses);
            } catch (final RobotEnvironmentException e) {
                final Optional<File> modulePath = findPythonLibraryModulePath(libraryImport);
                if (modulePath.isPresent()) {
                    final Path path = new Path(modulePath.get().getPath());
                    final ReferencedLibrary newLibrary = ReferencedLibrary.create(LibraryType.PYTHON,
                            libraryImport.getName(), path.toPortableString());
                    addLibraries(Collections.singletonList(newLibrary));
                    libraryImport.setStatus(DryRunLibraryImportStatus.ADDED);
                } else {
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
                return Optional.absent();
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
                libraryImport.setAdditionalInfo(e.getMessage());
            }
        }

        private void addReferencedLibrariesFromClasses(final RobotDryRunLibraryImport libraryImport,
                final Collection<ILibraryClass> libraryClasses) {
            final Collection<ReferencedLibrary> librariesToAdd = newArrayList();
            for (final ILibraryClass libraryClass : libraryClasses) {
                if (libraryClass.getQualifiedName().equalsIgnoreCase(libraryImport.getName())) {
                    librariesToAdd.add(libraryClass.toReferencedLibrary(libraryImport.getSourcePath()));
                }
            }
            if (!librariesToAdd.isEmpty()) {
                addLibraries(librariesToAdd);
                libraryImport.setStatus(DryRunLibraryImportStatus.ADDED);
            } else {
                libraryImport.setAdditionalInfo("RED was unable to find class '" + libraryImport.getName()
                        + "' inside '" + libraryImport.getSourcePath() + "' module.");
            }
        }

    }

}

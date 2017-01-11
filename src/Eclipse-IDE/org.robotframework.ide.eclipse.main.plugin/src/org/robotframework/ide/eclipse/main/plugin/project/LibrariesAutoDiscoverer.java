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
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
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
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.project.editor.libraries.JarStructureBuilder;
import org.robotframework.ide.eclipse.main.plugin.project.editor.libraries.JarStructureBuilder.JarClass;
import org.robotframework.ide.eclipse.main.plugin.project.editor.libraries.PythonLibStructureBuilder;
import org.robotframework.ide.eclipse.main.plugin.project.editor.libraries.PythonLibStructureBuilder.PythonClass;
import org.robotframework.red.swt.SwtThread;

import com.google.common.base.Optional;

/**
 * @author mmarzec
 */
public class LibrariesAutoDiscoverer extends AbstractAutoDiscoverer {

    private IEventBroker eventBroker;

    private boolean isSummaryWindowEnabled;

    private Optional<String> libraryNameToDiscover = Optional.absent();

    public LibrariesAutoDiscoverer(final RobotProject robotProject, final Collection<IResource> suiteFiles,
            final IEventBroker eventBroker) {
        this(robotProject, suiteFiles, eventBroker, null);
    }

    public LibrariesAutoDiscoverer(final RobotProject robotProject, final Collection<IResource> suiteFiles,
            final IEventBroker eventBroker, final String libraryNameToDiscover) {
        this(robotProject, suiteFiles, true);
        this.eventBroker = eventBroker;
        this.libraryNameToDiscover = libraryNameToDiscover != null && !libraryNameToDiscover.isEmpty()
                ? Optional.of(libraryNameToDiscover) : Optional.<String> absent();
    }

    public LibrariesAutoDiscoverer(final RobotProject robotProject, final Collection<IResource> suiteFiles,
            final boolean isSummaryWindowEnabled) {
        super(robotProject, suiteFiles);
        this.isSummaryWindowEnabled = isSummaryWindowEnabled;
    }

    @Override
    public void start(final Shell parent) {
        if (isDryRunRunning.compareAndSet(false, true)) {
            final WorkspaceJob wsJob = new WorkspaceJob("Discovering libraries") {

                @Override
                public IStatus runInWorkspace(final IProgressMonitor monitor) throws CoreException {
                    try {
                        startDiscovering(monitor);
                        startAddingLibrariesToProjectConfiguration(monitor);
                        if (isSummaryWindowEnabled) {
                            SwtThread.syncExec(new Runnable() {

                                @Override
                                public void run() {
                                    new LibrariesAutoDiscovererWindow(parent, dryRunOutputParser.getImportedLibraries())
                                            .open();
                                }
                            });
                        }
                    } catch (final InvocationTargetException e) {
                        MessageDialog.openError(parent, "Discovering libraries",
                                "Problems occurred during discovering libraries: " + e.getCause().getMessage());
                    } finally {
                        isDryRunRunning.set(false);
                    }

                    return Status.OK_STATUS;
                }

                @Override
                protected void canceling() {
                    isSummaryWindowEnabled = false;
                    dryRunHandler.destroyDryRunProcess();
                    this.cancel();
                }
            };
            wsJob.setUser(true);
            wsJob.schedule();
        }
    }

    private void startAddingLibrariesToProjectConfiguration(final IProgressMonitor monitor) {
        if (libraryNameToDiscover.isPresent()) {
            dryRunOutputParser.filterImportedLibrariesByName(libraryNameToDiscover.get());
            // after this filtering RobotDryRunOutputParser will return only one imported library or
            // nothing
        }
        final List<RobotDryRunLibraryImport> dryRunLibrariesImports = filterUnknownDryRunLibraryImports(
                dryRunOutputParser.getImportedLibraries());
        if (!dryRunLibrariesImports.isEmpty()) {
            RobotProjectConfig config = robotProject.getOpenedProjectConfig();
            final boolean inEditor = config != null;
            if (config == null) {
                config = new RedEclipseProjectConfigReader().readConfiguration(robotProject.getConfigurationFile());
            }
            final List<RobotDryRunLibraryImport> dryRunLibrariesToAdd = filterExistingReferencedLibraries(
                    dryRunLibrariesImports, config);

            final SubMonitor subMonitor = SubMonitor.convert(monitor);
            subMonitor.setWorkRemaining(dryRunLibrariesToAdd.size() + 1);
            final List<ReferencedLibrary> addedLibs = new ArrayList<>();
            for (final RobotDryRunLibraryImport libraryImport : dryRunLibrariesToAdd) {
                subMonitor.subTask("Adding discovered library to project configuration: " + libraryImport.getName());
                if (libraryImport.getType() == DryRunLibraryType.JAVA) {
                    addJavaLibraryToProjectConfiguration(config, libraryImport, addedLibs);
                } else {
                    addPythonLibraryToProjectConfiguration(config, libraryImport, addedLibs);
                }
                subMonitor.worked(1);
            }

            subMonitor.subTask("Updating project configuration...");
            if (!addedLibs.isEmpty()) {
                sendProjectConfigChangedEvent(addedLibs);
                if (!inEditor) {
                    new RedEclipseProjectConfigWriter().writeConfiguration(config, robotProject);
                }
            }
            subMonitor.worked(1);
            subMonitor.done();
        }
    }

    private void sendProjectConfigChangedEvent(final List<ReferencedLibrary> addedLibs) {
        final RedProjectConfigEventData<List<ReferencedLibrary>> eventData = new RedProjectConfigEventData<>(
                robotProject.getConfigurationFile(), addedLibs);
        if (eventBroker == null) {
            eventBroker = PlatformUI.getWorkbench().getService(IEventBroker.class);
        }
        if (eventBroker != null) {
            eventBroker.send(RobotProjectConfigEvents.ROBOT_CONFIG_LIBRARIES_STRUCTURE_CHANGED, eventData);
        }
    }

    private List<RobotDryRunLibraryImport> filterUnknownDryRunLibraryImports(
            final List<RobotDryRunLibraryImport> importedLibraries) {
        final List<RobotDryRunLibraryImport> filteredLibrariesImports = newArrayList();
        for (final RobotDryRunLibraryImport dryRunLibraryImport : importedLibraries) {
            if (dryRunLibraryImport.getType() != DryRunLibraryType.UNKNOWN) {
                filteredLibrariesImports.add(dryRunLibraryImport);
            }
        }
        return filteredLibrariesImports;
    }

    private List<RobotDryRunLibraryImport> filterExistingReferencedLibraries(
            final List<RobotDryRunLibraryImport> importedLibraries, final RobotProjectConfig config) {
        final List<RobotDryRunLibraryImport> filteredLibrariesImports = newArrayList();
        if (config != null) {
            final List<String> currentLibrariesNames = newArrayList();
            for (final ReferencedLibrary referencedLibrary : config.getLibraries()) {
                currentLibrariesNames.add(referencedLibrary.getName());
            }
            for (final RobotDryRunLibraryImport dryRunLibraryImport : importedLibraries) {
                if (!currentLibrariesNames.contains(dryRunLibraryImport.getName())) {
                    filteredLibrariesImports.add(dryRunLibraryImport);
                } else {
                    dryRunLibraryImport.setStatusAndAdditionalInfo(DryRunLibraryImportStatus.ALREADY_EXISTING,
                            "Library '" + dryRunLibraryImport.getName()
                                    + "' already existing in project configuration.");
                }
            }
        }
        return filteredLibrariesImports;
    }

    private void addPythonLibraryToProjectConfiguration(final RobotProjectConfig config,
            final RobotDryRunLibraryImport dryRunLibraryImport, final List<ReferencedLibrary> addedLibs) {
        final PythonLibStructureBuilder pythonLibStructureBuilder = new PythonLibStructureBuilder(
                robotProject.getRuntimeEnvironment(), robotProject.getRobotProjectConfig(), robotProject.getProject());
        Collection<PythonClass> pythonClasses = newArrayList();
        try {
            pythonClasses = pythonLibStructureBuilder.provideEntriesFromFile(dryRunLibraryImport.getSourcePath(),
                    Optional.of(dryRunLibraryImport.getName()), true);
        } catch (final RobotEnvironmentException e) {
            if (!isPythonLibraryRecognizedAndAddedByName(config, dryRunLibraryImport, addedLibs)) {
                dryRunLibraryImport.setStatusAndAdditionalInfo(DryRunLibraryImportStatus.NOT_ADDED, e.getMessage());
            }
            return;
        }

        final Collection<ReferencedLibrary> librariesToAdd = new ArrayList<>();
        if (pythonClasses.isEmpty()) {
            dryRunLibraryImport.setStatusAndAdditionalInfo(DryRunLibraryImportStatus.NOT_ADDED,
                    "RED was unable to find classes inside '" + dryRunLibraryImport.getSourcePath() + "' module.");
        } else {
            for (final PythonClass pythonClass : pythonClasses) {
                if (pythonClass.getQualifiedName().equalsIgnoreCase(dryRunLibraryImport.getName())) {
                    librariesToAdd.add(pythonClass.toReferencedLibrary(dryRunLibraryImport.getSourcePath()));
                }
            }
            if (librariesToAdd.isEmpty()) {
                dryRunLibraryImport.setStatusAndAdditionalInfo(DryRunLibraryImportStatus.NOT_ADDED,
                        "RED was unable to find class '" + dryRunLibraryImport.getName() + "' inside '"
                                + dryRunLibraryImport.getSourcePath() + "' module.");
            }
        }

        addReferencedLibrariesToProjectConfiguration(config, dryRunLibraryImport, addedLibs, librariesToAdd);
    }

    private boolean isPythonLibraryRecognizedAndAddedByName(final RobotProjectConfig config,
            final RobotDryRunLibraryImport dryRunLibraryImport, final List<ReferencedLibrary> addedLibs) {
        Optional<File> modulePath = Optional.absent();
        try {
            final EnvironmentSearchPaths envSearchPaths = new RedEclipseProjectConfig(config).createEnvironmentSearchPaths(robotProject.getProject());
            modulePath = robotProject.getRuntimeEnvironment().getModulePath(dryRunLibraryImport.getName(),
                    envSearchPaths);
        } catch (final RobotEnvironmentException e1) {
            // that's fine
        }
        if (modulePath.isPresent()) {
            final Path path = new Path(modulePath.get().getPath());
            final ReferencedLibrary newLibrary = ReferencedLibrary.create(LibraryType.PYTHON,
                    dryRunLibraryImport.getName(), path.toPortableString());
            final Collection<ReferencedLibrary> librariesToAdd = new ArrayList<>();
            librariesToAdd.add(newLibrary);
            addReferencedLibrariesToProjectConfiguration(config, dryRunLibraryImport, addedLibs, librariesToAdd);
            return true;
        }
        return false;
    }

    private void addJavaLibraryToProjectConfiguration(final RobotProjectConfig config,
            final RobotDryRunLibraryImport dryRunLibraryImport, final List<ReferencedLibrary> addedLibs) {
        final JarStructureBuilder jarStructureBuilder = new JarStructureBuilder(robotProject.getRuntimeEnvironment(),
                robotProject.getRobotProjectConfig(), robotProject.getProject());
        List<JarClass> classesFromJar = newArrayList();
        try {
            classesFromJar = jarStructureBuilder.provideEntriesFromFile(dryRunLibraryImport.getSourcePath());
        } catch (final RobotEnvironmentException e) {
            dryRunLibraryImport.setStatusAndAdditionalInfo(DryRunLibraryImportStatus.NOT_ADDED, e.getMessage());
            return;
        }
        final Collection<ReferencedLibrary> librariesToAdd = new ArrayList<>();
        if (classesFromJar.isEmpty()) {
            dryRunLibraryImport.setStatusAndAdditionalInfo(DryRunLibraryImportStatus.NOT_ADDED,
                    "RED was unable to find classes inside '" + dryRunLibraryImport.getSourcePath() + "' module.");
        } else {
            for (final JarClass jarClass : classesFromJar) {
                if (jarClass.getQualifiedName().equalsIgnoreCase(dryRunLibraryImport.getName())) {
                    librariesToAdd.add(jarClass.toReferencedLibrary(dryRunLibraryImport.getSourcePath()));
                }
            }
            if (librariesToAdd.isEmpty()) {
                dryRunLibraryImport.setStatusAndAdditionalInfo(DryRunLibraryImportStatus.NOT_ADDED,
                        "RED was unable to find class '" + dryRunLibraryImport.getName() + "' inside '"
                                + dryRunLibraryImport.getSourcePath() + "' module.");
            }
        }

        addReferencedLibrariesToProjectConfiguration(config, dryRunLibraryImport, addedLibs, librariesToAdd);
    }

    private void addReferencedLibrariesToProjectConfiguration(final RobotProjectConfig config,
            final RobotDryRunLibraryImport dryRunLibraryImport, final List<ReferencedLibrary> addedLibs,
            final Collection<ReferencedLibrary> librariesToAdd) {
        for (final ReferencedLibrary library : librariesToAdd) {
            if (config.addReferencedLibrary(library)) {
                addedLibs.add(library);
                dryRunLibraryImport.setStatusAndAdditionalInfo(DryRunLibraryImportStatus.ADDED, "");
            }
        }
    }

}

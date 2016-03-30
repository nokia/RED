/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project;

import static com.google.common.collect.Lists.newArrayList;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.rf.ide.core.executor.ILineHandler;
import org.rf.ide.core.executor.RobotDryRunHandler;
import org.rf.ide.core.executor.RobotDryRunOutputParser;
import org.rf.ide.core.executor.RobotDryRunOutputParser.DryRunLibraryImport;
import org.rf.ide.core.executor.RobotDryRunOutputParser.DryRunLibraryImportStatus;
import org.rf.ide.core.executor.RobotDryRunOutputParser.DryRunLibraryType;
import org.rf.ide.core.executor.RobotRuntimeEnvironment.RobotEnvironmentException;
import org.rf.ide.core.executor.RunCommandLineCallBuilder.RunCommandLine;
import org.robotframework.ide.eclipse.main.plugin.PathsConverter;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.launch.RobotLaunchConfigurationDelegate;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig.ReferencedLibrary;
import org.robotframework.ide.eclipse.main.plugin.project.editor.libraries.JarStructureBuilder;
import org.robotframework.ide.eclipse.main.plugin.project.editor.libraries.JarStructureBuilder.JarClass;
import org.robotframework.ide.eclipse.main.plugin.project.editor.libraries.PythonLibStructureBuilder;
import org.robotframework.ide.eclipse.main.plugin.project.editor.libraries.PythonLibStructureBuilder.PythonClass;
import org.robotframework.red.swt.SwtThread;

import com.google.common.io.Files;

/**
 * @author mmarzec
 */
public class LibrariesAutoDiscoverer {

    private IEventBroker eventBroker;

    private RobotProject robotProject;

    private List<IResource> suiteFiles = newArrayList();

    private RobotDryRunOutputParser dryRunOutputParser;

    private RobotDryRunHandler dryRunHandler;

    private boolean isSummaryWindowEnabled;

    private static AtomicBoolean isWorkspaceJobRunning = new AtomicBoolean(false);

    public LibrariesAutoDiscoverer(final RobotProject robotProject, final Collection<IResource> suiteFiles,
            final IEventBroker eventBroker) {
        this(robotProject, suiteFiles, true);
        this.eventBroker = eventBroker;
    }

    public LibrariesAutoDiscoverer(final RobotProject robotProject, final Collection<IResource> suiteFiles,
            final boolean isSummaryWindowEnabled) {
        this.robotProject = robotProject;
        this.suiteFiles.addAll(suiteFiles);
        this.isSummaryWindowEnabled = isSummaryWindowEnabled;
        dryRunOutputParser = new RobotDryRunOutputParser(robotProject.getStandardLibraries().keySet());
        dryRunHandler = new RobotDryRunHandler();
    }

    public void start() {
        if (!isWorkspaceJobRunning.get()) {
            isWorkspaceJobRunning.set(true);

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
                                    new LibrariesAutoDiscovererWindow(getActiveShell(),
                                            dryRunOutputParser.getImportedLibraries()).open();
                                }
                            });
                        }
                    } catch (final InvocationTargetException e) {
                        MessageDialog.openError(getActiveShell(), "Discovering libraries",
                                "Problems occured during discovering libraries: " + e.getCause().getMessage());
                    } finally {
                        isWorkspaceJobRunning.set(false);
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

    private void startDiscovering(final IProgressMonitor monitor) throws InvocationTargetException {

        SubMonitor subMonitor = SubMonitor.convert(monitor);
        subMonitor.subTask("Preparing Robot dry run execution...");
        subMonitor.setWorkRemaining(3);

        final Set<String> pythonpathDirs = collectPythonPathDirs();
        subMonitor.worked(1);

        final RunCommandLine dryRunCommandLine = createDryRunCommandLine(pythonpathDirs);
        subMonitor.worked(1);

        subMonitor.subTask("Executing Robot dry run...");
        executeDryRun(dryRunCommandLine);
        subMonitor.worked(1);

        subMonitor.done();
    }

    private Set<String> collectPythonPathDirs() throws InvocationTargetException {
        final Set<String> pythonpathDirs = new HashSet<>();
        try {
            pythonpathDirs.add(robotProject.getProject().getLocation().toOSString());
            collectDirsWithPythonMembers(robotProject.getProject().members(), pythonpathDirs);
        } catch (final CoreException e) {
            throw new InvocationTargetException(e);
        }
        pythonpathDirs.addAll(robotProject.getPythonpath());
        return pythonpathDirs;
    }

    private void collectDirsWithPythonMembers(final IResource[] members, final Set<String> pythonpathDirs)
            throws CoreException {
        if (members != null) {
            for (int i = 0; i < members.length; i++) {
                final IResource resource = members[i];
                if (resource.getType() == IResource.FILE) {
                    final String fileExtension = resource.getFileExtension();
                    if (fileExtension != null && fileExtension.equals("py")) {
                        final IContainer parent = resource.getParent();
                        if (parent != null && parent.getLocation() != null) {
                            pythonpathDirs.add(parent.getLocation().toOSString());
                        }
                    }
                } else if (resource.getType() == IResource.FOLDER) {
                    collectDirsWithPythonMembers(((IFolder) resource).members(), pythonpathDirs);
                }
            }
        }
    }

    private RunCommandLine createDryRunCommandLine(final Set<String> pythonpathDirs) throws InvocationTargetException {
        
        final List<String> resourcesPaths = newArrayList();
        final List<String> suiteNames = newArrayList();
        collectSuiteNamesAndResourcesPaths(suiteNames, resourcesPaths);
        final List<String> additionalProjectsLocations = newArrayList();
        if(!resourcesPaths.isEmpty()) {
            final File tempSuiteFileWithResources = dryRunHandler.createTempSuiteFile(resourcesPaths);
            if(tempSuiteFileWithResources != null) {
                suiteNames.add(Files.getNameWithoutExtension(tempSuiteFileWithResources.getPath()));
                additionalProjectsLocations.add(tempSuiteFileWithResources.getParent());
            }
        }
        
        RunCommandLine runCommandLine = null;
        try {
            runCommandLine = dryRunHandler.buildDryRunCommand(robotProject.getRuntimeEnvironment(),
                    robotProject.getProject().getLocation().toFile(), suiteNames, pythonpathDirs,
                    robotProject.getClasspath(), additionalProjectsLocations);
        } catch (IOException e) {
            throw new InvocationTargetException(e);
        }
        return runCommandLine;
    }

    private void collectSuiteNamesAndResourcesPaths(final List<String> suiteNames, final List<String> resourcesPaths) {
        for (final IResource resource : suiteFiles) {
            if (resource.getType() == IResource.FILE) {
                final RobotSuiteFile suiteFile = RedPlugin.getModelManager().createSuiteFile((IFile) resource);
                if (suiteFile != null && suiteFile.isResourceFile()) {
                    final IPath resourceFilePath = PathsConverter
                            .toWorkspaceRelativeIfPossible(resource.getProjectRelativePath());
                    resourcesPaths.add(resourceFilePath.toString());

                } else {
                    suiteNames.add(RobotLaunchConfigurationDelegate.createSuiteName(resource));
                }
            } else if (resource.getType() == IResource.FOLDER) {
                suiteNames.add(RobotLaunchConfigurationDelegate.createSuiteName(resource));
            }
        }
    }

    private void executeDryRun(final RunCommandLine dryRunCommandLine) throws InvocationTargetException {
        if (dryRunCommandLine != null) {
            final List<ILineHandler> dryRunOutputlisteners = newArrayList();
            dryRunOutputlisteners.add(dryRunOutputParser);
            dryRunHandler.startDryRunHandlerThread(dryRunCommandLine.getPort(), dryRunOutputlisteners);

            dryRunHandler.executeDryRunProcess(dryRunCommandLine);
        }
    }

    private void startAddingLibrariesToProjectConfiguration(final IProgressMonitor monitor) {
        final List<DryRunLibraryImport> importedLibraries = newArrayList();
        
        for (final DryRunLibraryImport dryRunLibraryImport : dryRunOutputParser.getImportedLibraries()) {
            if(dryRunLibraryImport.getType() != DryRunLibraryType.UNKNOWN) {
                importedLibraries.add(dryRunLibraryImport);
            }
        }
        
        if (!importedLibraries.isEmpty()) {
            RobotProjectConfig config = robotProject.getOpenedProjectConfig();
            final boolean inEditor = config != null;
            if (config == null) {
                config = new RobotProjectConfigReader().readConfiguration(robotProject.getConfigurationFile());
            }
            final List<DryRunLibraryImport> dryRunLibrariesToAdd = filterExistingReferencedLibraries(importedLibraries,
                    config);

            SubMonitor subMonitor = SubMonitor.convert(monitor);
            subMonitor.setWorkRemaining(dryRunLibrariesToAdd.size() + 1);
            final List<ReferencedLibrary> addedLibs = new ArrayList<>();
            for (final DryRunLibraryImport libraryImport : dryRunLibrariesToAdd) {
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
                final RedProjectConfigEventData<List<ReferencedLibrary>> eventData = new RedProjectConfigEventData<>(
                        robotProject.getConfigurationFile(), addedLibs);
                if (eventBroker == null) {
                    eventBroker = (IEventBroker) PlatformUI.getWorkbench().getService(IEventBroker.class);
                }
                if (eventBroker != null) {
                    eventBroker.send(RobotProjectConfigEvents.ROBOT_CONFIG_LIBRARIES_STRUCTURE_CHANGED, eventData);
                }

                if (!inEditor) {
                    new RobotProjectConfigWriter().writeConfiguration(config, robotProject);
                }
            }
            subMonitor.worked(1);
            subMonitor.done();
        }
    }

    private List<DryRunLibraryImport> filterExistingReferencedLibraries(
            final List<DryRunLibraryImport> importedLibraries, RobotProjectConfig config) {
        final List<DryRunLibraryImport> dryRunLibrariesToAdd = newArrayList();
        if (config != null) {
            final List<String> currentLibrariesNames = newArrayList();
            for (final ReferencedLibrary referencedLibrary : config.getLibraries()) {
                currentLibrariesNames.add(referencedLibrary.getName());
            }
            for (final DryRunLibraryImport dryRunLibraryImport : importedLibraries) {
                if (!currentLibrariesNames.contains(dryRunLibraryImport.getName())) {
                    dryRunLibrariesToAdd.add(dryRunLibraryImport);
                } else {
                    dryRunLibraryImport.setStatusAndAdditionalInfo(DryRunLibraryImportStatus.ALREADY_EXISTING,
                            "Library '" + dryRunLibraryImport.getName()
                                    + "' already existing in project configuration.");
                }
            }
        }
        return dryRunLibrariesToAdd;
    }

    private void addPythonLibraryToProjectConfiguration(final RobotProjectConfig config,
            final DryRunLibraryImport libraryImport, final List<ReferencedLibrary> addedLibs) {

        final PythonLibStructureBuilder pythonLibStructureBuilder = new PythonLibStructureBuilder(
                robotProject.getRuntimeEnvironment());
        Collection<PythonClass> pythonClasses = newArrayList();
        try {
            pythonClasses = pythonLibStructureBuilder.provideEntriesFromFile(libraryImport.getSourcePath());
        } catch (RobotEnvironmentException e) {
            libraryImport.setStatusAndAdditionalInfo(DryRunLibraryImportStatus.NOT_ADDED, e.getMessage());
            return;
        }

        final Collection<ReferencedLibrary> librariesToAdd = new ArrayList<>();
        if (pythonClasses.isEmpty()) {
            libraryImport.setStatusAndAdditionalInfo(DryRunLibraryImportStatus.NOT_ADDED,
                    "RED was unable to find classes inside '" + libraryImport.getSourcePath() + "' module.");
        } else {
            for (PythonClass pythonClass : pythonClasses) {
                if (pythonClass.getQualifiedName().equalsIgnoreCase(libraryImport.getName())) {
                    librariesToAdd.add(pythonClass.toReferencedLibrary(libraryImport.getSourcePath()));
                }
            }
            if (librariesToAdd.isEmpty()) {
                libraryImport.setStatusAndAdditionalInfo(DryRunLibraryImportStatus.NOT_ADDED,
                        "RED was unable to find class '" + libraryImport.getName() + "' inside '"
                                + libraryImport.getSourcePath() + "' module.");
            }
        }

        addReferencedLibrariesToProjectConfiguration(config, libraryImport, addedLibs, librariesToAdd);
    }

    private void addJavaLibraryToProjectConfiguration(final RobotProjectConfig config,
            final DryRunLibraryImport libraryImport, final List<ReferencedLibrary> addedLibs) {
        final JarStructureBuilder jarStructureBuilder = new JarStructureBuilder(robotProject.getRuntimeEnvironment());
        List<JarClass> classesFromJar = newArrayList();
        try {
            classesFromJar = jarStructureBuilder.provideEntriesFromFile(libraryImport.getSourcePath());
        } catch (RobotEnvironmentException e) {
            libraryImport.setStatusAndAdditionalInfo(DryRunLibraryImportStatus.NOT_ADDED, e.getMessage());
            return;
        }
        final Collection<ReferencedLibrary> librariesToAdd = new ArrayList<>();
        if (classesFromJar.isEmpty()) {
            libraryImport.setStatusAndAdditionalInfo(DryRunLibraryImportStatus.NOT_ADDED,
                    "RED was unable to find classes inside '" + libraryImport.getSourcePath() + "' module.");
        } else {
            for (JarClass jarClass : classesFromJar) {
                if (jarClass.getQualifiedName().equalsIgnoreCase(libraryImport.getName())) {
                    librariesToAdd.add(jarClass.toReferencedLibrary(libraryImport.getSourcePath()));
                }
            }
            if (librariesToAdd.isEmpty()) {
                libraryImport.setStatusAndAdditionalInfo(DryRunLibraryImportStatus.NOT_ADDED,
                        "RED was unable to find class '" + libraryImport.getName() + "' inside '"
                                + libraryImport.getSourcePath() + "' module.");
            }
        }

        addReferencedLibrariesToProjectConfiguration(config, libraryImport, addedLibs, librariesToAdd);
    }

    private void addReferencedLibrariesToProjectConfiguration(final RobotProjectConfig config,
            final DryRunLibraryImport libraryImport, final List<ReferencedLibrary> addedLibs,
            final Collection<ReferencedLibrary> librariesToAdd) {
        for (final ReferencedLibrary library : librariesToAdd) {
            if (config.addReferencedLibrary(library)) {
                addedLibs.add(library);
                libraryImport.setStatusAndAdditionalInfo(DryRunLibraryImportStatus.ADDED, "");
            }
        }
    }

    private static Shell getActiveShell() {
        final IWorkbench workbench = PlatformUI.getWorkbench();
        final IWorkbenchWindow workbenchWindow = workbench.getActiveWorkbenchWindow();
        return workbenchWindow != null ? workbenchWindow.getShell() : null;
    }

}

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
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

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
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.rf.ide.core.dryrun.IDryRunStartSuiteHandler;
import org.rf.ide.core.dryrun.RobotDryRunHandler;
import org.rf.ide.core.dryrun.RobotDryRunLibraryImport;
import org.rf.ide.core.dryrun.RobotDryRunLibraryImport.DryRunLibraryImportStatus;
import org.rf.ide.core.dryrun.RobotDryRunLibraryImport.DryRunLibraryType;
import org.rf.ide.core.dryrun.RobotDryRunOutputParser;
import org.rf.ide.core.executor.ILineHandler;
import org.rf.ide.core.executor.RobotRuntimeEnvironment;
import org.rf.ide.core.executor.RobotRuntimeEnvironment.RobotEnvironmentException;
import org.rf.ide.core.executor.RunCommandLineCallBuilder.RunCommandLine;
import org.robotframework.ide.eclipse.main.plugin.PathsConverter;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.launch.RobotLaunchConfigurationDelegate;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig.LibraryType;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig.ReferencedLibrary;
import org.robotframework.ide.eclipse.main.plugin.project.editor.libraries.JarStructureBuilder;
import org.robotframework.ide.eclipse.main.plugin.project.editor.libraries.JarStructureBuilder.JarClass;
import org.robotframework.ide.eclipse.main.plugin.project.editor.libraries.PythonLibStructureBuilder;
import org.robotframework.ide.eclipse.main.plugin.project.editor.libraries.PythonLibStructureBuilder.PythonClass;
import org.robotframework.red.swt.SwtThread;

import com.google.common.base.Optional;
import com.google.common.io.Files;

/**
 * @author mmarzec
 */
public class LibrariesAutoDiscoverer {

    private IEventBroker eventBroker;

    private final RobotProject robotProject;

    private final List<IResource> suiteFiles = Collections.synchronizedList(new ArrayList<IResource>());

    private final RobotDryRunOutputParser dryRunOutputParser;

    private final RobotDryRunHandler dryRunHandler;

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
        dryRunOutputParser = new RobotDryRunOutputParser();
        dryRunOutputParser.setupRobotDryRunLibraryImportCollector(robotProject.getStandardLibraries().keySet());
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

        final SubMonitor subMonitor = SubMonitor.convert(monitor);
        subMonitor.subTask("Preparing Robot dry run execution...");
        subMonitor.setWorkRemaining(3);

        final LibrariesSourcesCollector librariesSourcesCollector = collectPythonpathAndClasspathLocations();
        subMonitor.worked(1);

        final RunCommandLine dryRunCommandLine = createDryRunCommandLine(librariesSourcesCollector);
        subMonitor.worked(1);

        subMonitor.subTask("Executing Robot dry run...");
        executeDryRun(dryRunCommandLine, subMonitor);
        subMonitor.worked(1);

        subMonitor.done();
    }

    private LibrariesSourcesCollector collectPythonpathAndClasspathLocations() throws InvocationTargetException {
        final LibrariesSourcesCollector librariesSourcesCollector = new LibrariesSourcesCollector(robotProject);
        try {
            librariesSourcesCollector.collectPythonAndJavaLibrariesSources();
        } catch (final CoreException e) {
            throw new InvocationTargetException(e);
        }
        return librariesSourcesCollector;
    }

    private RunCommandLine createDryRunCommandLine(final LibrariesSourcesCollector librariesSourcesCollector)
            throws InvocationTargetException {
        final DryRunTargetsCollector dryRunTargetsCollector = new DryRunTargetsCollector();
        dryRunTargetsCollector.collectSuiteNamesAndAdditionalProjectsLocations();

        RunCommandLine runCommandLine = null;
        try {
            final RobotRuntimeEnvironment runtimeEnvironment = robotProject.getRuntimeEnvironment();
            if (runtimeEnvironment == null) {
                return null;
            }
            runCommandLine = dryRunHandler.buildDryRunCommand(runtimeEnvironment, getProjectLocationFile(),
                    dryRunTargetsCollector.getSuiteNames(), librariesSourcesCollector.getPythonpathLocations(),
                    librariesSourcesCollector.getClasspathLocations(),
                    dryRunTargetsCollector.getAdditionalProjectsLocations());
        } catch (final IOException e) {
            throw new InvocationTargetException(e);
        }
        return runCommandLine;
    }

    private void executeDryRun(final RunCommandLine dryRunCommandLine, final SubMonitor subMonitor)
            throws InvocationTargetException {
        if (dryRunCommandLine != null) {
            dryRunOutputParser.setStartSuiteHandler(new IDryRunStartSuiteHandler() {

                @Override
                public void processStartSuiteEvent(final String suiteName) {
                    subMonitor.subTask("Executing Robot dry run on suite: " + suiteName);
                }
            });

            final List<ILineHandler> dryRunOutputlisteners = newArrayList();
            dryRunOutputlisteners.add(dryRunOutputParser);
            dryRunHandler.startDryRunHandlerThread(dryRunCommandLine.getPort(), dryRunOutputlisteners);

            dryRunHandler.executeDryRunProcess(dryRunCommandLine, getProjectLocationFile());
        }
    }

    private void startAddingLibrariesToProjectConfiguration(final IProgressMonitor monitor) {
        final List<RobotDryRunLibraryImport> dryRunLibraryImports = filterUnknownDryRunLibraryImports();
        if (!dryRunLibraryImports.isEmpty()) {
            RobotProjectConfig config = robotProject.getOpenedProjectConfig();
            final boolean inEditor = config != null;
            if (config == null) {
                config = new RobotProjectConfigReader().readConfiguration(robotProject.getConfigurationFile());
            }
            final List<RobotDryRunLibraryImport> dryRunLibrariesToAdd = filterExistingReferencedLibraries(
                    dryRunLibraryImports, config);

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
                    new RobotProjectConfigWriter().writeConfiguration(config, robotProject);
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

    private List<RobotDryRunLibraryImport> filterUnknownDryRunLibraryImports() {
        final List<RobotDryRunLibraryImport> importedLibraries = newArrayList();
        for (final RobotDryRunLibraryImport dryRunLibraryImport : dryRunOutputParser.getImportedLibraries()) {
            if (dryRunLibraryImport.getType() != DryRunLibraryType.UNKNOWN) {
                importedLibraries.add(dryRunLibraryImport);
            }
        }
        return importedLibraries;
    }

    private List<RobotDryRunLibraryImport> filterExistingReferencedLibraries(
            final List<RobotDryRunLibraryImport> dryRunLibraryImports, final RobotProjectConfig config) {
        final List<RobotDryRunLibraryImport> dryRunLibrariesToAdd = newArrayList();
        if (config != null) {
            final List<String> currentLibrariesNames = newArrayList();
            for (final ReferencedLibrary referencedLibrary : config.getLibraries()) {
                currentLibrariesNames.add(referencedLibrary.getName());
            }
            for (final RobotDryRunLibraryImport dryRunLibraryImport : dryRunLibraryImports) {
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
            final RobotDryRunLibraryImport dryRunLibraryImport, final List<ReferencedLibrary> addedLibs) {
        final PythonLibStructureBuilder pythonLibStructureBuilder = new PythonLibStructureBuilder(
                robotProject.getRuntimeEnvironment(), robotProject.getRobotProjectConfig(), robotProject.getProject());
        Collection<PythonClass> pythonClasses = newArrayList();
        try {
            pythonClasses = pythonLibStructureBuilder.provideEntriesFromFile(dryRunLibraryImport.getSourcePath(),
                    Optional.of(dryRunLibraryImport.getName()));
        } catch (final RobotEnvironmentException e) {
            if (!isPythonLibraryRecognizedByName(config, dryRunLibraryImport, addedLibs)) {
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

    private boolean isPythonLibraryRecognizedByName(final RobotProjectConfig config,
            final RobotDryRunLibraryImport dryRunLibraryImport, final List<ReferencedLibrary> addedLibs) {
        Optional<File> modulePath = Optional.absent();
        try {
            modulePath = robotProject.getRuntimeEnvironment().getModulePath(dryRunLibraryImport.getName(),
                    config.createEnvironmentSearchPaths(robotProject.getProject()));
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

    private static Shell getActiveShell() {
        final IWorkbench workbench = PlatformUI.getWorkbench();
        final IWorkbenchWindow workbenchWindow = workbench.getActiveWorkbenchWindow();
        return workbenchWindow != null ? workbenchWindow.getShell() : null;
    }

    private File getProjectLocationFile() {
        File file = null;
        final IPath projectLocation = robotProject.getProject().getLocation();
        if (projectLocation != null) {
            file = projectLocation.toFile();
        }
        return file;
    }

    public void addSuiteFileToDiscovering(final IResource suiteFile) {
        synchronized (suiteFiles) {
            if (!suiteFiles.contains(suiteFile)) {
                suiteFiles.add(suiteFile);
            }
        }
    }

    public boolean hasSuiteFilesToDiscovering() {
        return !suiteFiles.isEmpty();
    }

    private class DryRunTargetsCollector {

        private final List<String> suiteNames = newArrayList();

        private final List<String> additionalProjectsLocations = newArrayList();

        public void collectSuiteNamesAndAdditionalProjectsLocations() {
            final List<String> resourcesPaths = newArrayList();
            for (final IResource resource : suiteFiles) {
                RobotSuiteFile suiteFile = null;
                if (resource.getType() == IResource.FILE) {
                    suiteFile = RedPlugin.getModelManager().createSuiteFile((IFile) resource);
                }
                if (suiteFile != null && suiteFile.isResourceFile()) {
                    final IPath resourceFilePath = PathsConverter
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
                final File tempSuiteFileWithResources = dryRunHandler.createTempSuiteFile(resourcesPaths);
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

        public List<String> getSuiteNames() {
            return suiteNames;
        }

        public List<String> getAdditionalProjectsLocations() {
            return additionalProjectsLocations;
        }
    }

}

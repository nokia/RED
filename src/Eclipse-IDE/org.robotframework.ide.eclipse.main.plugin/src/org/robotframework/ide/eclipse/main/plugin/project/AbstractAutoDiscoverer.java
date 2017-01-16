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
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.rf.ide.core.dryrun.IDryRunStartSuiteHandler;
import org.rf.ide.core.dryrun.RobotDryRunHandler;
import org.rf.ide.core.dryrun.RobotDryRunOutputParser;
import org.rf.ide.core.executor.ILineHandler;
import org.rf.ide.core.executor.RobotRuntimeEnvironment;
import org.rf.ide.core.executor.RunCommandLineCallBuilder.RunCommandLine;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.RedWorkspace;
import org.robotframework.ide.eclipse.main.plugin.launch.RobotLaunchConfigurationDelegate;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;

import com.google.common.io.Files;

/**
 * @author bembenek
 */
public abstract class AbstractAutoDiscoverer {

    private static final AtomicBoolean isDryRunRunning = new AtomicBoolean(false);

    protected final RobotProject robotProject;

    protected final RobotDryRunOutputParser dryRunOutputParser;

    protected final RobotDryRunHandler dryRunHandler;

    private final List<IResource> suiteFiles = Collections.synchronizedList(new ArrayList<IResource>());

    protected AbstractAutoDiscoverer(final RobotProject robotProject, final Collection<IResource> suiteFiles) {
        this.robotProject = robotProject;
        this.dryRunOutputParser = new RobotDryRunOutputParser();
        this.dryRunOutputParser.setupRobotDryRunLibraryImportCollector(robotProject.getStandardLibraries().keySet());
        this.dryRunHandler = new RobotDryRunHandler();
        this.suiteFiles.addAll(suiteFiles);
    }

    public void start() {
        final IWorkbench workbench = PlatformUI.getWorkbench();
        final IWorkbenchWindow workbenchWindow = workbench.getActiveWorkbenchWindow();
        final Shell parent = workbenchWindow != null ? workbenchWindow.getShell() : null;
        start(parent);
    }

    protected abstract void start(final Shell parent);

    protected final boolean startDryRun() {
        return isDryRunRunning.compareAndSet(false, true);
    }

    protected final void stopDryRun() {
        isDryRunRunning.set(false);
    }

    protected void startDiscovering(final IProgressMonitor monitor) throws InvocationTargetException {

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
        boolean shouldCollectRecursively = true;
        if (!RedPlugin.getDefault().getPreferences().isProjectModulesRecursiveAdditionOnVirtualenvEnabled()
                && robotProject.getRuntimeEnvironment().isVirtualenv()) {
            shouldCollectRecursively = false;
        }
        final LibrariesSourcesCollector librariesSourcesCollector = new LibrariesSourcesCollector(robotProject);
        try {
            librariesSourcesCollector.collectPythonAndJavaLibrariesSources(shouldCollectRecursively);
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

            final List<ILineHandler> dryRunOutputListeners = newArrayList();
            dryRunOutputListeners.add(dryRunOutputParser);
            dryRunHandler.startDryRunHandlerThread(dryRunCommandLine.getPort(), dryRunOutputListeners);

            dryRunHandler.executeDryRunProcess(dryRunCommandLine, getProjectLocationFile());
        }
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

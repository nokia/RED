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
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;

/**
 * @author bembenek
 */
abstract class AbstractAutoDiscoverer {

    private static final AtomicBoolean isDryRunRunning = new AtomicBoolean(false);

    final RobotProject robotProject;

    final RobotDryRunOutputParser dryRunOutputParser;

    final RobotDryRunHandler dryRunHandler;

    final List<IResource> suiteFiles = Collections.synchronizedList(new ArrayList<IResource>());

    AbstractAutoDiscoverer(final RobotProject robotProject, final Collection<? extends IResource> suiteFiles) {
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

    abstract void start(final Shell parent);

    synchronized final boolean startDryRun() {
        return isDryRunRunning.compareAndSet(false, true);
    }

    synchronized final void stopDryRun() {
        isDryRunRunning.set(false);
    }

    void startDiscovering(final IProgressMonitor monitor, final IDryRunTargetsCollector dryRunTargetsCollector)
            throws InvocationTargetException, InterruptedException {
        final SubMonitor subMonitor = SubMonitor.convert(monitor);
        subMonitor.subTask("Preparing Robot dry run execution...");
        subMonitor.setWorkRemaining(4);

        final LibrariesSourcesCollector librariesSourcesCollector = collectPythonpathAndClasspathLocations();
        subMonitor.worked(1);

        dryRunTargetsCollector.collectSuiteNamesAndAdditionalProjectsLocations();
        subMonitor.worked(1);

        final RunCommandLine dryRunCommandLine = createDryRunCommandLine(librariesSourcesCollector,
                dryRunTargetsCollector);
        subMonitor.worked(1);

        subMonitor.subTask("Executing Robot dry run...");
        executeDryRun(dryRunCommandLine, subMonitor);
        subMonitor.worked(1);
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

    private RunCommandLine createDryRunCommandLine(final LibrariesSourcesCollector librariesSourcesCollector,
            final IDryRunTargetsCollector dryRunTargetsCollector) throws InvocationTargetException {
        try {
            final RobotRuntimeEnvironment runtimeEnvironment = robotProject.getRuntimeEnvironment();
            if (runtimeEnvironment == null) {
                return null;
            }
            return dryRunHandler.buildDryRunCommand(runtimeEnvironment, getProjectLocationFile(),
                    dryRunTargetsCollector.getSuiteNames(), librariesSourcesCollector.getPythonpathLocations(),
                    librariesSourcesCollector.getClasspathLocations(),
                    dryRunTargetsCollector.getAdditionalProjectsLocations());
        } catch (final IOException e) {
            throw new InvocationTargetException(e);
        }
    }

    private void executeDryRun(final RunCommandLine dryRunCommandLine, final SubMonitor subMonitor)
            throws InvocationTargetException, InterruptedException {
        if (dryRunCommandLine != null && !subMonitor.isCanceled()) {
            dryRunOutputParser.setStartSuiteHandler(new IDryRunStartSuiteHandler() {

                @Override
                public void processStartSuiteEvent(final String suiteName) {
                    subMonitor.subTask("Executing Robot dry run on suite: " + suiteName);
                }
            });

            final int port = dryRunCommandLine.getPort();
            final List<ILineHandler> listeners = newArrayList();
            listeners.add(dryRunOutputParser);
            final Thread handlerThread = dryRunHandler.createDryRunHandlerThread(port, listeners);
            handlerThread.start();
            dryRunHandler.executeDryRunProcess(dryRunCommandLine, getProjectLocationFile());
            handlerThread.join();
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

    interface IDryRunTargetsCollector {

        void collectSuiteNamesAndAdditionalProjectsLocations();

        List<String> getSuiteNames();

        List<String> getAdditionalProjectsLocations();
    }

    static class AutoDiscovererException extends RuntimeException {

        private static final long serialVersionUID = 1L;

        public AutoDiscovererException(final String message, final Throwable cause) {
            super(message, cause);
        }
    }

}

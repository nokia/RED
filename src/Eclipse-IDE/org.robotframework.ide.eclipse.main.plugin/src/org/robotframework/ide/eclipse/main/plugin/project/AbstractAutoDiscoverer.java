/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project;

import static org.robotframework.ide.eclipse.main.plugin.RedPlugin.newCoreException;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.rf.ide.core.dryrun.RobotDryRunEventListener;
import org.rf.ide.core.dryrun.RobotDryRunHandler;
import org.rf.ide.core.dryrun.RobotDryRunKeywordSourceCollector;
import org.rf.ide.core.dryrun.RobotDryRunLibraryImportCollector;
import org.rf.ide.core.execution.TestsMode;
import org.rf.ide.core.execution.server.AgentConnectionServer;
import org.rf.ide.core.execution.server.AgentServerKeepAlive;
import org.rf.ide.core.execution.server.AgentServerTestsStarter;
import org.rf.ide.core.executor.EnvironmentSearchPaths;
import org.rf.ide.core.executor.RobotRuntimeEnvironment;
import org.rf.ide.core.executor.RunCommandLineCallBuilder;
import org.rf.ide.core.executor.RunCommandLineCallBuilder.RunCommandLine;
import org.robotframework.ide.eclipse.main.plugin.launch.AgentConnectionServerJob;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;

/**
 * @author bembenek
 */
public abstract class AbstractAutoDiscoverer {

    private static final AtomicBoolean IS_DRY_RUN_RUNNING = new AtomicBoolean(false);

    final RobotProject robotProject;

    final RobotDryRunLibraryImportCollector dryRunLibraryImportCollector;

    final RobotDryRunKeywordSourceCollector dryRunLKeywordSourceCollector;

    final RobotDryRunHandler dryRunHandler;

    final List<IResource> suiteFiles;

    AbstractAutoDiscoverer(final RobotProject robotProject, final Collection<? extends IResource> suiteFiles) {
        this.robotProject = robotProject;
        this.dryRunLibraryImportCollector = new RobotDryRunLibraryImportCollector(
                robotProject.getStandardLibraries().keySet());
        this.dryRunLKeywordSourceCollector = new RobotDryRunKeywordSourceCollector();
        this.dryRunHandler = new RobotDryRunHandler();
        this.suiteFiles = new ArrayList<IResource>(suiteFiles);
    }

    public void start() {
        final IWorkbench workbench = PlatformUI.getWorkbench();
        final IWorkbenchWindow workbenchWindow = workbench.getActiveWorkbenchWindow();
        final Shell parent = workbenchWindow != null ? workbenchWindow.getShell() : null;
        start(parent);
    }

    abstract void start(final Shell parent);

    final boolean lockDryRun() {
        return IS_DRY_RUN_RUNNING.compareAndSet(false, true);
    }

    final void unlockDryRun() {
        IS_DRY_RUN_RUNNING.set(false);
    }

    void startDiscovering(final IProgressMonitor monitor, final IDryRunTargetsCollector dryRunTargetsCollector)
            throws InvocationTargetException, InterruptedException {
        final SubMonitor subMonitor = SubMonitor.convert(monitor);
        subMonitor.subTask("Preparing Robot dry run execution...");
        subMonitor.setWorkRemaining(4);

        try {
            final LibrariesSourcesCollector librariesSourcesCollector = new LibrariesSourcesCollector(robotProject);
            librariesSourcesCollector.collectPythonAndJavaLibrariesSources();
            subMonitor.worked(1);

            dryRunTargetsCollector.collectSuiteNamesAndAdditionalProjectsLocations();
            subMonitor.worked(1);

            final int port = AgentConnectionServer.findFreePort();
            final RunCommandLine dryRunCommandLine = createDryRunCommandLine(librariesSourcesCollector,
                    dryRunTargetsCollector, port);
            subMonitor.worked(1);

            subMonitor.subTask("Executing Robot dry run...");
            if (!subMonitor.isCanceled()) {
                executeDryRun(dryRunCommandLine, port,
                        suiteName -> subMonitor.subTask("Executing Robot dry run on suite: " + suiteName));
            }
            subMonitor.worked(1);
        } catch (final CoreException | IOException e) {
            throw new InvocationTargetException(e);
        } finally {
            subMonitor.done();
        }
    }

    private RunCommandLine createDryRunCommandLine(final LibrariesSourcesCollector librariesSourcesCollector,
            final IDryRunTargetsCollector dryRunTargetsCollector, final int port) throws CoreException, IOException {
        final RobotRuntimeEnvironment runtimeEnvironment = robotProject.getRuntimeEnvironment();
        if (runtimeEnvironment == null) {
            throw newCoreException(
                    "There is no active runtime environment for project '" + robotProject.getName() + "'");
        }
        final EnvironmentSearchPaths searchPaths = librariesSourcesCollector.getEnvironmentSearchPaths();
        return RunCommandLineCallBuilder.forEnvironment(runtimeEnvironment, port)
                .useArgumentFile(true)
                .suitesToRun(dryRunTargetsCollector.getSuiteNames())
                .addLocationsToPythonPath(searchPaths.getExtendedPythonPaths(runtimeEnvironment.getInterpreter()))
                .addLocationsToClassPath(searchPaths.getClassPaths())
                .enableDryRun()
                .withProject(getProjectLocationFile())
                .withAdditionalProjectsLocations(dryRunTargetsCollector.getAdditionalProjectsLocations())
                .build();
    }

    private void executeDryRun(final RunCommandLine dryRunCommandLine, final int port,
            final Consumer<String> startSuiteHandler) throws InvocationTargetException, InterruptedException {
        final RobotDryRunEventListener dryRunEventListener = new RobotDryRunEventListener(dryRunLibraryImportCollector,
                dryRunLKeywordSourceCollector, startSuiteHandler);
        final AgentServerTestsStarter testsStarter = new AgentServerTestsStarter(TestsMode.RUN);

        final AgentConnectionServerJob serverJob = AgentConnectionServerJob
                .setupServerAt(AgentConnectionServer.DEFAULT_CONNECTION_HOST, port)
                .withConnectionTimeout(AgentConnectionServer.DEFAULT_CONNECTION_TIMEOUT, TimeUnit.SECONDS)
                .agentEventsListenedBy(testsStarter)
                .agentEventsListenedBy(dryRunEventListener)
                .agentEventsListenedBy(new AgentServerKeepAlive())
                .start()
                .waitForServer();

        testsStarter.allowClientTestsStart();

        dryRunHandler.executeDryRunProcess(dryRunCommandLine, getProjectLocationFile());

        serverJob.join();
    }

    private File getProjectLocationFile() {
        final IPath projectLocation = robotProject.getProject().getLocation();
        return projectLocation != null ? projectLocation.toFile() : null;
    }

    public interface IDryRunTargetsCollector {

        void collectSuiteNamesAndAdditionalProjectsLocations();

        List<String> getSuiteNames();

        List<File> getAdditionalProjectsLocations();
    }

    public static class AutoDiscovererException extends RuntimeException {

        private static final long serialVersionUID = 1L;

        public AutoDiscovererException(final String message, final Throwable cause) {
            super(message, cause);
        }
    }

}

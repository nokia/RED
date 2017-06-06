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
import org.rf.ide.core.dryrun.RobotDryRunKeywordSourceCollector;
import org.rf.ide.core.dryrun.RobotDryRunLibraryImportCollector;
import org.rf.ide.core.execution.agent.TestsMode;
import org.rf.ide.core.execution.server.AgentConnectionServer;
import org.rf.ide.core.execution.server.AgentServerKeepAlive;
import org.rf.ide.core.execution.server.AgentServerTestsStarter;
import org.rf.ide.core.executor.EnvironmentSearchPaths;
import org.rf.ide.core.executor.RobotRuntimeEnvironment;
import org.rf.ide.core.executor.RunCommandLineCallBuilder;
import org.rf.ide.core.executor.RunCommandLineCallBuilder.RunCommandLine;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.launch.AgentConnectionServerJob;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;

/**
 * @author bembenek
 */
public abstract class AbstractAutoDiscoverer {

    private static final AtomicBoolean IS_DRY_RUN_RUNNING = new AtomicBoolean(false);

    private static final int VIRTUAL_ENV_SEARCH_DEPTH = 1;

    final RobotProject robotProject;

    final RobotDryRunLibraryImportCollector dryRunLibraryImportCollector;

    final RobotDryRunKeywordSourceCollector dryRunLKeywordSourceCollector;

    private final List<IResource> suiteFiles;

    private final IDryRunTargetsCollector dryRunTargetsCollector;

    private final LibrariesSourcesCollector librariesSourcesCollector;

    private AgentConnectionServerJob serverJob;

    private final RobotDryRunHandler dryRunHandler;

    AbstractAutoDiscoverer(final RobotProject robotProject, final Collection<? extends IResource> suiteFiles,
            final IDryRunTargetsCollector dryRunTargetsCollector) {
        this.robotProject = robotProject;
        this.dryRunLibraryImportCollector = new RobotDryRunLibraryImportCollector(
                robotProject.getStandardLibraries().keySet());
        this.dryRunLKeywordSourceCollector = new RobotDryRunKeywordSourceCollector();
        this.suiteFiles = new ArrayList<IResource>(suiteFiles);
        this.dryRunTargetsCollector = dryRunTargetsCollector;
        this.librariesSourcesCollector = new LibrariesSourcesCollector();
        this.dryRunHandler = new RobotDryRunHandler();
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

    void stopDiscovering() {
        if (serverJob != null) {
            serverJob.stopServer();
        }
        dryRunHandler.destroyDryRunProcess();
    }

    void startDiscovering(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
        final SubMonitor subMonitor = SubMonitor.convert(monitor);
        subMonitor.subTask("Preparing Robot dry run execution...");
        subMonitor.setWorkRemaining(3);

        try {
            final RobotRuntimeEnvironment runtimeEnvironment = robotProject.getRuntimeEnvironment();
            if (runtimeEnvironment == null) {
                throw newCoreException(
                        "There is no active runtime environment for project '" + robotProject.getName() + "'");
            }

            collectLibrarySources(runtimeEnvironment);
            subMonitor.worked(1);

            dryRunTargetsCollector.collectSuiteNamesAndAdditionalProjectsLocations(robotProject, suiteFiles);
            subMonitor.worked(1);

            subMonitor.subTask("Executing Robot dry run...");
            if (!subMonitor.isCanceled()) {
                executeDryRun(runtimeEnvironment,
                        suiteName -> subMonitor.subTask("Executing Robot dry run on suite: " + suiteName));
            }
            subMonitor.worked(1);
        } catch (final CoreException | IOException e) {
            throw new InvocationTargetException(e);
        } finally {
            subMonitor.done();
        }
    }

    private void collectLibrarySources(final RobotRuntimeEnvironment runtimeEnvironment) throws CoreException {
        if (!runtimeEnvironment.isVirtualenv()
                || RedPlugin.getDefault().getPreferences().isProjectModulesRecursiveAdditionOnVirtualenvEnabled()) {
            librariesSourcesCollector.collectPythonAndJavaLibrariesSources(robotProject);
        } else {
            librariesSourcesCollector.collectPythonAndJavaLibrariesSources(robotProject, VIRTUAL_ENV_SEARCH_DEPTH);
        }
    }

    private void executeDryRun(final RobotRuntimeEnvironment runtimeEnvironment,
            final Consumer<String> startSuiteHandler) throws CoreException, InterruptedException, IOException {
        final String host = AgentConnectionServer.DEFAULT_CONNECTION_HOST;
        final int port = AgentConnectionServer.findFreePort();
        final int timeout = AgentConnectionServer.DEFAULT_CONNECTION_TIMEOUT;
        final RobotDryRunEventListener dryRunEventListener = new RobotDryRunEventListener(dryRunLibraryImportCollector,
                dryRunLKeywordSourceCollector, startSuiteHandler);

        serverJob = startDryRunServer(host, port, timeout, dryRunEventListener);

        dryRunHandler.executeDryRunProcess(createDryRunCommandLine(runtimeEnvironment, port), getProjectLocationFile());

        serverJob.join();
    }

    private AgentConnectionServerJob startDryRunServer(final String host, final int port, final int timeout,
            final RobotDryRunEventListener dryRunEventListener) throws InterruptedException {
        final AgentServerTestsStarter testsStarter = new AgentServerTestsStarter(TestsMode.RUN);
        final AgentConnectionServerJob serverJob = AgentConnectionServerJob.setupServerAt(host, port)
                .withConnectionTimeout(timeout, TimeUnit.SECONDS)
                .agentEventsListenedBy(testsStarter)
                .agentEventsListenedBy(dryRunEventListener)
                .agentEventsListenedBy(new AgentServerKeepAlive())
                .start()
                .waitForServer();

        testsStarter.allowClientTestsStart();

        return serverJob;
    }

    private RunCommandLine createDryRunCommandLine(final RobotRuntimeEnvironment runtimeEnvironment, final int port)
            throws IOException {
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

    private File getProjectLocationFile() {
        final IPath projectLocation = robotProject.getProject().getLocation();
        return projectLocation != null ? projectLocation.toFile() : null;
    }

    public interface IDryRunTargetsCollector {

        void collectSuiteNamesAndAdditionalProjectsLocations(RobotProject robotProject, List<IResource> suiteFiles);

        List<String> getSuiteNames();

        List<File> getAdditionalProjectsLocations();
    }

    public static class AutoDiscovererException extends RuntimeException {

        private static final long serialVersionUID = 1L;

        public AutoDiscovererException(final String message, final Throwable cause) {
            super(message, cause);
        }
    }

    private class RobotDryRunHandler {

        private Process dryRunProcess;

        public void executeDryRunProcess(final RunCommandLine dryRunCommandLine, final File projectDir)
                throws CoreException {
            try {
                ProcessBuilder processBuilder = new ProcessBuilder(dryRunCommandLine.getCommandLine());
                if (projectDir != null && projectDir.exists()) {
                    processBuilder = processBuilder.directory(projectDir);
                }
                dryRunProcess = processBuilder.start();
                dryRunProcess.waitFor();
            } catch (InterruptedException | IOException e) {
                throw newCoreException("Unable to start dry run process.", e);
            }
        }

        public void destroyDryRunProcess() {
            if (dryRunProcess != null) {
                dryRunProcess.destroy();
            }
        }
    }

}

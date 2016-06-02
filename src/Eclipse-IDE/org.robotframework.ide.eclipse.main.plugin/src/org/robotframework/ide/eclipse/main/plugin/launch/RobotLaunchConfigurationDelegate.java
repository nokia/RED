/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.launch;

import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.LaunchConfigurationDelegate;
import org.eclipse.debug.ui.ILaunchShortcut;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;
import org.rf.ide.core.execution.ExecutionElement;
import org.rf.ide.core.execution.IExecutionHandler;
import org.rf.ide.core.executor.ILineHandler;
import org.rf.ide.core.executor.RobotRuntimeEnvironment;
import org.rf.ide.core.executor.RunCommandLineCallBuilder;
import org.rf.ide.core.executor.RunCommandLineCallBuilder.IRunCommandLineBuilder;
import org.rf.ide.core.executor.RunCommandLineCallBuilder.RunCommandLine;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.debug.model.RobotDebugTarget;
import org.robotframework.ide.eclipse.main.plugin.debug.utils.DebugSocketManager;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.views.ExecutionView;
import org.robotframework.ide.eclipse.main.plugin.views.MessageLogView;
import org.robotframework.red.viewers.Selections;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.CaseFormat;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class RobotLaunchConfigurationDelegate extends LaunchConfigurationDelegate implements
        ILaunchConfigurationDelegate, ILaunchShortcut {

    private final ILaunchConfigurationType launchConfigurationType;

    private final ILaunchManager launchManager;
    
    private final RobotEventBroker robotEventBroker;
    
    private final AtomicBoolean isConfigurationRunning = new AtomicBoolean(false);
    
    private boolean hasViewsInitialized;
    
    public RobotLaunchConfigurationDelegate() {
        launchManager = DebugPlugin.getDefault().getLaunchManager();
        launchConfigurationType = launchManager.getLaunchConfigurationType(RobotLaunchConfiguration.TYPE_ID);
        robotEventBroker = new RobotEventBroker(
                (IEventBroker) PlatformUI.getWorkbench().getService(IEventBroker.class));
        
    }

    @Override
    public void launch(final ISelection selection, final String mode) {
        if (selection instanceof IStructuredSelection) {
            final List<IResource> resources = Selections.getElements((IStructuredSelection) selection, IResource.class);
            if (!resources.isEmpty()) {
                launch(resources, mode);
            }
        }
    }

    @Override
    public void launch(final IEditorPart editor, final String mode) {
        final IEditorInput input = editor.getEditorInput();
        if (input instanceof FileEditorInput) {
            final IResource file = ((FileEditorInput) input).getFile();
            launch(newArrayList(file), mode);
        }
    }

    private void launch(final List<IResource> resources, final String mode) {
        if (resources.isEmpty()) {
            throw new IllegalStateException("There should be at least one suite selected for launching");
        }
        final WorkspaceJob job = new WorkspaceJob("Launching Robot Tests") {
            @Override
            public IStatus runInWorkspace(final IProgressMonitor monitor) throws CoreException {

                ILaunchConfiguration config = findLaunchConfiguration(resources);
                if (config == null) {
                    config = RobotLaunchConfiguration.createDefault(launchConfigurationType, resources);
                }
                config.launch(mode, monitor);

                return Status.OK_STATUS;
            }
        };
        job.setUser(false);
        job.schedule();
    }

    private ILaunchConfiguration findLaunchConfiguration(final List<IResource> resources) throws CoreException {
        
        final ILaunchConfiguration[] launchConfigs = launchManager.getLaunchConfigurations(launchConfigurationType);
        if (resources.size() == 1 && (resources.get(0) instanceof IProject || resources.get(0) instanceof IFolder)) {
            final String resourceName = resources.get(0).getName();
            final String projectName = resources.get(0).getProject().getName();
            for (final ILaunchConfiguration configuration : launchConfigs) {
                if (configuration.getName().equals(resourceName)
                        && new RobotLaunchConfiguration(configuration).getProjectName().equals(projectName)) {
                    return configuration;
                }
            }
        }
        for (final ILaunchConfiguration configuration : launchConfigs) {
            final RobotLaunchConfiguration robotConfig = new RobotLaunchConfiguration(configuration);
            if (robotConfig.isSuitableFor(resources)) {
                return configuration;
            }
        }
        return null;
    }

    @Override
    public void launch(final ILaunchConfiguration configuration, final String mode, final ILaunch launch,
            final IProgressMonitor monitor) throws CoreException {
        if (!ILaunchManager.RUN_MODE.equals(mode) && !ILaunchManager.DEBUG_MODE.equals(mode)) {
            throw newCoreException("Unrecognized launch mode: '" + mode + "'");
        }

        if (isConfigurationRunning.getAndSet(true)) {
            return;
        }
        try {
            initViews();
            robotEventBroker.sendClearEventToMessageLogView();
            robotEventBroker.sendClearEventToExecutionView();
            doLaunch(configuration, mode, launch, monitor);
        } catch (final IOException e) {
            throw newCoreException("Unable to launch Robot", e);
        } finally {
            isConfigurationRunning.set(false);
        }
    }
    
    public void doLaunch(final ILaunchConfiguration configuration, final String mode, final ILaunch launch,
            final IProgressMonitor monitor) throws CoreException, IOException {
        // FIXME : use monitor for progress reporting and cancellation
        // possibility

        final RobotLaunchConfiguration robotConfig = new RobotLaunchConfiguration(configuration);
        final RobotProject robotProject = robotConfig.getRobotProject();
        final RobotRuntimeEnvironment runtimeEnvironment = getRobotRuntimeEnvironment(robotProject);
        Collection<IResource> suiteResources = getSuiteResources(robotConfig, robotProject.getProject());

        final Optional<Integer> remoteDebugPort = robotConfig.getRemoteDebugPort();

        final boolean isDebugging = ILaunchManager.DEBUG_MODE.equals(mode);
        final boolean isRemoteDebugging = isDebugging && remoteDebugPort.isPresent()
                && !robotConfig.getRemoteDebugHost().isEmpty();

        final String host = isRemoteDebugging ? robotConfig.getRemoteDebugHost() : "localhost";
        final Optional<Integer> connectionTimeout = isRemoteDebugging ? robotConfig.getRemoteDebugTimeout()
                : Optional.<Integer> absent();

        final RunCommandLine cmdLine = isRemoteDebugging
                ? createRemoteModeCmd(runtimeEnvironment, remoteDebugPort.get())
                : createStandardModeCmd(robotConfig, robotProject, suiteResources, isDebugging);
        
        if (cmdLine.getPort() < 0) {
            throw newCoreException("Unable to find free port");
        }

        DebugSocketManager socketManager = null;
        boolean isDebugServerSocketListening = false; 
        if (!isDebugging) {
            runtimeEnvironment.startTestRunnerAgentHandler(cmdLine.getPort(), new ILineHandler() {
                @Override
                public void processLine(final String line) {
                    robotEventBroker.sendAppendLineEventToMessageLogView(line);
                }
            }, new IExecutionHandler() {

                @Override
                public void processExecutionElement(final ExecutionElement executionElement) {
                    robotEventBroker.sendExecutionEventToExecutionView(executionElement);
                }
            });
        } else {
            socketManager = new DebugSocketManager(host, cmdLine.getPort(), connectionTimeout);
            new Thread(socketManager).start();
            isDebugServerSocketListening = waitForDebugServerSocket(socketManager);
        }

        final String description = robotConfig.createConsoleDescription(runtimeEnvironment);
        final String version = robotConfig.isUsingInterpreterFromProject() ? runtimeEnvironment.getVersion()
                : RobotRuntimeEnvironment.getVersion(robotConfig.getExecutor());

        final Process process = DebugPlugin.exec(cmdLine.getCommandLine(),
                robotProject.getProject().getLocation().toFile());
        final IProcess eclipseProcess = DebugPlugin.newProcess(launch, process, description);
        
        final RobotConsoleFacade consoleFacade = new RobotConsoleFacade();
        consoleFacade.connect(robotConfig, runtimeEnvironment, cmdLine, version);

        if (isRemoteDebugging) {
            if (isDebugServerSocketListening && socketManager.getServerSocket() != null) {
                consoleFacade.writeLine(
                        "Debug server is listening on " + host + ":" + remoteDebugPort.get()
                                + ", you can run a remote test");
            } else {
                if (eclipseProcess != null) {
                    eclipseProcess.terminate();
                }
                throw newCoreException("Cannot run Debug server on " + host + ":" + remoteDebugPort.get() + ".");
            }
        }
        
        if (isDebugging) {
            if(suiteResources.isEmpty()) {
                suiteResources = newArrayList();
                suiteResources.add(robotProject.getProject());
            }
            try {
                final RobotDebugTarget target = new RobotDebugTarget(launch, eclipseProcess, consoleFacade,
                        isRemoteDebugging);
                target.connect(newArrayList(suiteResources), robotEventBroker, socketManager);
                launch.addDebugTarget(target);
            } catch (final CoreException e) {
                if (socketManager.getServerSocket() != null) {
                    socketManager.getServerSocket().close();
                }
            }
        }

        try {
            if (process != null) {
                process.waitFor();
            }
        } catch (final InterruptedException e) {
            throw newCoreException("Robot process was interrupted", e);
        }
    }

    private RunCommandLine createRemoteModeCmd(final RobotRuntimeEnvironment env, final int port) throws IOException {
        return RunCommandLineCallBuilder.forRemoteEnvironment(env, port).build();
    }

    @VisibleForTesting
    RunCommandLine createStandardModeCmd(final RobotLaunchConfiguration robotConfig,
            final RobotProject robotProject, final Collection<IResource> suiteResources, final boolean isDebugging)
            throws CoreException, IOException {

        final IRunCommandLineBuilder builder = robotConfig.isUsingInterpreterFromProject()
                ? RunCommandLineCallBuilder.forEnvironment(robotProject.getRuntimeEnvironment())
                : RunCommandLineCallBuilder.forExecutor(robotConfig.getExecutor());

        builder.withProject(robotProject.getProject().getLocation().toFile());
        builder.addLocationsToClassPath(robotProject.getClasspath());
        builder.addLocationsToPythonPath(robotProject.getPythonpath());
        builder.addUserArgumentsForInterpreter(robotConfig.getInterpeterArguments());
        builder.addUserArgumentsForRobot(robotConfig.getExecutorArguments());

        builder.addVariableFiles(robotProject.getVariableFilePaths());

        builder.suitesToRun(getSuitesToRun(suiteResources));
        builder.testsToRun(getTestsToRun(robotConfig));

        if (robotConfig.isIncludeTagsEnabled()) {
            builder.includeTags(robotConfig.getIncludedTags());
        }
        if (robotConfig.isExcludeTagsEnabled()) {
            builder.excludeTags(robotConfig.getExcludedTags());
        }
        builder.enableDebug(isDebugging);
        return builder.build();
    }

    private RobotRuntimeEnvironment getRobotRuntimeEnvironment(final RobotProject robotProject) throws CoreException {
        
        final RobotRuntimeEnvironment runtimeEnvironment = robotProject.getRuntimeEnvironment();
        if (runtimeEnvironment == null) {
            throw newCoreException(
                    "There is no active runtime environment for project '" + robotProject.getName() + "'");
        }
        if (!runtimeEnvironment.hasRobotInstalled()) {
            throw newCoreException("The runtime environment " + runtimeEnvironment.getFile().getAbsolutePath()
                    + " is either not a python installation or it has no Robot installed");
        }
        return runtimeEnvironment;
    }

    private Collection<IResource> getSuiteResources(final RobotLaunchConfiguration robotConfig, final IProject project)
            throws CoreException {
        final Collection<String> suitePaths = robotConfig.getSuitePaths().keySet();

        final Map<String, IResource> resources = Maps.asMap(newHashSet(suitePaths), new Function<String, IResource>() {
            @Override
            public IResource apply(final String suitePath) {
                return project.findMember(org.eclipse.core.runtime.Path.fromPortableString(suitePath));
            }
        });

        final List<String> problems = new ArrayList<>();
        for (final Entry<String, IResource> entry : resources.entrySet()) {
            if (entry.getValue() == null || !entry.getValue().exists()) {
                problems.add("Suite '" + entry.getKey() + "' does not exist in project '"
                        + project.getName() + "'");
            }
        }
        if (!problems.isEmpty()) {
            throw newCoreException(Joiner.on('\n').join(problems));
        }
        return resources.values();
    }

    private static CoreException newCoreException(final String message) {
        return newCoreException(message, null);
    }

    private static CoreException newCoreException(final String message, final Throwable cause) {
        return new CoreException(new Status(IStatus.ERROR, RedPlugin.PLUGIN_ID, message, cause));
    }

    private List<String> getSuitesToRun(final Collection<IResource> suites) {
        final List<String> suiteNames = new ArrayList<String>();

        for (final IResource suite : suites) {
            suiteNames.add(createSuiteName(suite));
        }
        return suiteNames;
    }

    public static String createSuiteName(final IResource suite) {
        final String actualProjectName = suite.getProject().getLocation().lastSegment();
        return createSuiteName(actualProjectName, suite.getProjectRelativePath());
    }

    public static String createSuiteName(final String projectName, final IPath path) {
        final List<String> upperCased = newArrayList(toRobotFrameworkName(projectName));
        upperCased.addAll(
                Lists.transform(Arrays.asList(path.removeFileExtension().segments()), toRobotFrameworkName()));
        return Joiner.on('.').join(upperCased);
    }

    private Collection<String> getTestsToRun(final RobotLaunchConfiguration robotConfig) throws CoreException {
        final String projectName = robotConfig.getProjectName();
        final List<String> tests = new ArrayList<>();
        for (final Entry<String, List<String>> entries : robotConfig.getSuitePaths().entrySet()) {
            for (final String testName : entries.getValue()) {
                tests.add(createSuiteName(projectName, Path.fromPortableString(entries.getKey())) + "." + testName);
            }
        }
        return tests;
    }

    private static Function<String, String> toRobotFrameworkName() {
        return new Function<String, String>() {

            @Override
            public String apply(final String name) {
                return toRobotFrameworkName(name);
            }
        };
    }

    private static String toRobotFrameworkName(final String name) {
        // converts suite/test name to name used by RF
        String resultName = name;
        final int prefixIndex = resultName.indexOf("__");
        if (prefixIndex != -1) {
            resultName = resultName.substring(prefixIndex + 2);
        }
        final List<String> splittedNames = Splitter.on(' ').splitToList(resultName);
        final Iterable<String> titled = transform(splittedNames, new Function<String, String>() {

            @Override
            public String apply(final String name) {
                return CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, name);
            }
        });
        return Joiner.on(' ').join(titled);
    }

    private boolean waitForDebugServerSocket(final DebugSocketManager socketManager) {
        boolean isListening = false;
        int retryCounter = 0;
        while (!isListening && retryCounter < 20) {
            try (Socket temporarySocket = new Socket(socketManager.getHost(), socketManager.getPort())) {
                isListening = true;
            } catch (final IOException e) {
                if(socketManager.hasServerException()) {
                    return isListening;
                }
                try {
                    Thread.sleep(100);
                    retryCounter++;
                } catch (final InterruptedException e1) {
                    e1.printStackTrace();
                }
            }
        }
        return isListening;
    }
    
    private void initViews() {
        if (!hasViewsInitialized) {
            final IWorkbench workbench = PlatformUI.getWorkbench();
            workbench.getDisplay().syncExec(new Runnable() {

                @Override
                public void run() {
                    final IWorkbenchPage page = workbench.getActiveWorkbenchWindow().getActivePage();
                    if (page != null) {
                        final IViewPart messageLogViewPart = page.findView(MessageLogView.ID);
                        if (messageLogViewPart == null || !page.isPartVisible(messageLogViewPart)) {
                            try {
                                page.showView(MessageLogView.ID);
                            } catch (final PartInitException e) {
                                e.printStackTrace();
                            }
                        }

                        final IViewPart executionViewPart = page.findView(ExecutionView.ID);
                        if (executionViewPart == null || !page.isPartVisible(executionViewPart)) {
                            try {
                                page.showView(ExecutionView.ID);
                            } catch (final PartInitException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            });
            hasViewsInitialized = true;
        }
    }
}

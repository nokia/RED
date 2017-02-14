/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.launch;

import static com.google.common.collect.Lists.newArrayList;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.LaunchConfigurationDelegate;
import org.eclipse.debug.ui.ILaunchShortcut2;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCase;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCasesSection;
import org.robotframework.ide.eclipse.main.plugin.views.ExecutionView;
import org.robotframework.ide.eclipse.main.plugin.views.MessageLogView;
import org.robotframework.red.jface.dialogs.DetailedErrorDialog;
import org.robotframework.red.swt.SwtThread;
import org.robotframework.red.viewers.Selections;

import com.google.common.base.Optional;

public class RobotLaunchConfigurationDelegate extends LaunchConfigurationDelegate implements ILaunchShortcut2 {

    private final RobotEventBroker robotEventBroker;
    
    private final AtomicBoolean isConfigurationRunning = new AtomicBoolean(false);
    
    private boolean hasViewsInitialized;
    
    public RobotLaunchConfigurationDelegate() {
        robotEventBroker = new RobotEventBroker(PlatformUI.getWorkbench().getService(IEventBroker.class));
    }

    @Override
    protected IProject[] getProjectsForProblemSearch(final ILaunchConfiguration configuration, final String mode)
            throws CoreException {
        return new IProject[] { new RobotLaunchConfiguration(configuration).getRobotProject().getProject() };
    }

    @Override
    public void launch(final ISelection selection, final String mode) {
        if (!selection.isEmpty() && selection instanceof IStructuredSelection) {
            final List<IResource> resources = Selections.getAdaptableElements((IStructuredSelection) selection,
                    IResource.class);

            for (final Object o : ((IStructuredSelection) selection).toList()) {
                if (o instanceof RobotCasesSection) {
                    resources.add(((RobotCasesSection) o).getSuiteFile().getFile());
                }
            }
            if (!resources.isEmpty()) {
                createAndLaunchConfiguration(resources, mode);
            } else {
                createAndLaunchConfigurationForSelectedTestCases((IStructuredSelection) selection, mode);
            }
        }
    }

    private void createAndLaunchConfiguration(final List<IResource> resources, final String mode) {
        final ILaunchConfigurationType launchConfigurationType = DebugPlugin.getDefault()
                .getLaunchManager()
                .getLaunchConfigurationType(RobotLaunchConfiguration.TYPE_ID);
        try {
            final ILaunchConfiguration config = RobotLaunchConfiguration.createDefault(launchConfigurationType,
                    resources);
            doLaunchConfiguration(config, mode);
        } catch (final CoreException e) {
            DetailedErrorDialog.openErrorDialog("Cannot generate Robot Launch Configuration",
                    "RED was unable to create Robot Launch Configuration from selection.");
        }
    }

    private void createAndLaunchConfigurationForSelectedTestCases(final IStructuredSelection selection,
            final String mode) {
        final Optional<Map<IResource, List<String>>> resourcesToTests = mapResourcesToTestCases(selection);
        if (resourcesToTests.isPresent()) {
            try {
                final ILaunchConfiguration config = RobotLaunchConfiguration
                        .prepareLaunchConfigurationForSelectedTestCases(resourcesToTests.get());
                doLaunchConfiguration(config, mode);
            } catch (final CoreException e) {
                DetailedErrorDialog.openErrorDialog("Cannot generate Robot Launch Configuration",
                        "RED was unable to create Robot Launch Configuration from selection.");
            }
        }
    }

    private void doLaunchConfiguration(final ILaunchConfiguration config, final String mode) {
        if (config == null) {
            throw new IllegalStateException("There must be valid Robot Launch Configuration provided.");
        }
        final WorkspaceJob job = new WorkspaceJob("Launching Robot Tests") {

            @Override
            public IStatus runInWorkspace(final IProgressMonitor monitor) throws CoreException {
                config.launch(mode, monitor);

                return Status.OK_STATUS;
            }
        };
        job.setUser(false);
        job.schedule();
    }

    @Override
    public void launch(final IEditorPart editor, final String mode) {
        final IEditorInput input = editor.getEditorInput();
        if (input instanceof FileEditorInput) {
            final IResource file = ((FileEditorInput) input).getFile();
            launch(newArrayList(file), mode, false);
        }
    }

    private void launch(final List<IResource> resources, final String mode, final boolean generalOnly) {
        if (resources.isEmpty()) {
            throw new IllegalStateException("There should be at least one suite selected for launching");
        }
        final WorkspaceJob job = new WorkspaceJob("Launching Robot Tests") {
            @Override
            public IStatus runInWorkspace(final IProgressMonitor monitor) throws CoreException {

                ILaunchConfiguration config = null;
                if (generalOnly) {
                    config = RobotLaunchConfigurationFinder.findLaunchConfigurationExceptSelectedTestCases(resources);
                } else {
                    config = RobotLaunchConfigurationFinder.findLaunchConfiguration(resources);
                }
                if (config == null) {
                    final ILaunchConfigurationType launchConfigurationType = DebugPlugin.getDefault()
                            .getLaunchManager()
                            .getLaunchConfigurationType(RobotLaunchConfiguration.TYPE_ID);
                    config = RobotLaunchConfiguration.createDefault(launchConfigurationType, resources);
                }
                config.launch(mode, monitor);

                return Status.OK_STATUS;
            }
        };
        job.setUser(false);
        job.schedule();
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
            saveConfiguration(configuration);
        } catch (final IOException e) {
            throw newCoreException("Unable to launch Robot", e);
        } finally {
            isConfigurationRunning.set(false);
        }
    }
    
    private static void saveConfiguration(final ILaunchConfiguration configuration) throws CoreException {
        ILaunchConfigurationWorkingCopy toSave = null;
        if (configuration.isWorkingCopy()) {
            // since 3.3 ILaunchConfigurationWorkingCopy'ies can be nested
            final ILaunchConfiguration original = ((ILaunchConfigurationWorkingCopy) configuration).getOriginal();
            if (original != null) {
                toSave = original.getWorkingCopy();
            } else {
                toSave = (ILaunchConfigurationWorkingCopy) configuration;
            }
        } else {
            toSave = configuration.getWorkingCopy();
        }
        toSave.doSave();
    }

    private void initViews() {
        // TODO : is this field even needed?
        if (!hasViewsInitialized) {
            SwtThread.syncExec(new Runnable() {

                @Override
                public void run() {
                    final IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
                    if (page != null) {
                        openOrShowView(page, MessageLogView.ID);
                        openOrShowView(page, ExecutionView.ID);
                    }
                }
            });
            hasViewsInitialized = true;
        }
    }

    private void openOrShowView(final IWorkbenchPage page, final String viewId) {
        final IViewPart view = page.findView(viewId);
        if (view == null || !page.isPartVisible(view)) {
            try {
                page.showView(viewId);
            } catch (final PartInitException e) {
                e.printStackTrace();
            }
        }
    }

    private void doLaunch(final ILaunchConfiguration configuration, final String mode, final ILaunch launch,
            final IProgressMonitor monitor) throws CoreException, IOException {

        final RobotLaunchConfiguration robotConfig = new RobotLaunchConfiguration(configuration);

        RobotLaunchInMode launchMode = null;
        if (ILaunchManager.RUN_MODE.equals(mode)) {
            launchMode = new RobotLaunchInRunMode(robotEventBroker);

        } else if (ILaunchManager.DEBUG_MODE.equals(mode) && !robotConfig.isRemoteDefined()) {
            launchMode = new RobotLaunchInDebugMode(robotEventBroker);

        } else if (ILaunchManager.DEBUG_MODE.equals(mode)) {
            launchMode = new RobotLaunchInRemoteDebugMode(robotEventBroker);
        }
        launchMode.launch(robotConfig, launch, monitor);
    }

    private static CoreException newCoreException(final String message) {
        return newCoreException(message, null);
    }

    private static CoreException newCoreException(final String message, final Throwable cause) {
        return new CoreException(new Status(IStatus.ERROR, RedPlugin.PLUGIN_ID, message, cause));
    }

    @Override
    public ILaunchConfiguration[] getLaunchConfigurations(final ISelection selection) {
        if (!selection.isEmpty() && selection instanceof IStructuredSelection) {
            final IStructuredSelection ss = (IStructuredSelection) selection;
            final List<IResource> resources = Selections.getAdaptableElements(ss, IResource.class);
            if (!resources.isEmpty()) {
                try {
                    return new ILaunchConfiguration[] { RobotLaunchConfigurationFinder
                            .getLaunchConfigurationExceptSelectedTestCases(resources) };
                } catch (final CoreException e) {
                    // fine, will return null
                }
            } else {
                final Optional<Map<IResource, List<String>>> resourcesToTests = mapResourcesToTestCases(ss);
                if (!resourcesToTests.isPresent()) {
                    return null;
                }
                try {
                    final ILaunchConfiguration config = RobotLaunchConfigurationFinder
                            .getLaunchConfigurationForSelectedTestCases(resourcesToTests.get());
                    final RobotLaunchConfiguration robotConfig = new RobotLaunchConfiguration(config);
                    robotConfig.updateTestCases(resourcesToTests.get());
                    return new ILaunchConfiguration[] { config };
                } catch (final CoreException e) {
                    // fine, will return null
                }
            }
        }
        return null;
    }

    private static Optional<Map<IResource, List<String>>> mapResourcesToTestCases(
            final IStructuredSelection selection) {
        final Iterator<?> selectedElements = selection.iterator();
        final Map<IResource, List<String>> resourcesToTests = new HashMap<>();
        while (selectedElements.hasNext()) {
            final Object o = selectedElements.next();
            if (o instanceof RobotCase) {
                addRobotCaseToMap(resourcesToTests, (RobotCase) o);
            } else {
                // There is a selection element that should not be launched with others
                return Optional.absent();
            }
        }
        return Optional.of(resourcesToTests);
    }

    private static void addRobotCaseToMap(final Map<IResource, List<String>> map, final RobotCase robotCase) {
        final IResource res = robotCase.getSuiteFile().getFile();
        if (map.containsKey(res)) {
            map.get(res).add(robotCase.getName());
        } else {
            map.put(res, newArrayList(robotCase.getName()));
        }
    }

    @Override
    public ILaunchConfiguration[] getLaunchConfigurations(final IEditorPart editorpart) {
        return null;
    }

    @Override
    public IResource getLaunchableResource(final ISelection selection) {
        return null;
    }

    @Override
    public IResource getLaunchableResource(final IEditorPart editorpart) {
        return null;
    }
}

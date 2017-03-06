/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.launch.local;

import static com.google.common.collect.Lists.newArrayList;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.ILaunchShortcut2;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.part.FileEditorInput;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCase;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCasesSection;
import org.robotframework.ide.eclipse.main.plugin.propertytester.SelectionsPropertyTester;
import org.robotframework.red.jface.dialogs.DetailedErrorDialog;
import org.robotframework.red.viewers.Selections;

import com.google.common.base.Optional;

public class RobotLaunchConfigurationShortcut implements ILaunchShortcut2 {

    @Override
    public void launch(final ISelection selection, final String mode) {
        if (!selection.isEmpty() && selection instanceof IStructuredSelection) {
            final IStructuredSelection structuredSelection = (IStructuredSelection) selection;
            if (SelectionsPropertyTester.testIfAllElementsAreFromSameProject(structuredSelection, true)) {
                final List<IResource> resources = Selections.getAdaptableElements(structuredSelection, IResource.class);
                for (final Object o : structuredSelection.toList()) {
                    if (o instanceof RobotCasesSection) {
                        resources.add(((RobotCasesSection) o).getSuiteFile().getFile());
                    }
                }
                if (!resources.isEmpty()) {
                    createAndLaunchConfiguration(resources, mode);
                } else {
                    createAndLaunchConfigurationForSelectedTestCases(structuredSelection, mode);
                }
            } else {
                DetailedErrorDialog.openErrorDialog("Cannot generate Robot Launch Configuration",
                        "All selected elements need to be members of only one project.");
            }
        }
    }

    private void createAndLaunchConfiguration(final List<IResource> resources, final String mode) {
        try {
            final ILaunchConfigurationWorkingCopy config = RobotLaunchConfiguration.prepareDefault(resources);
            final ILaunchConfiguration sameConfig = RobotLaunchConfigurationFinder.findSameAs(config);
            if (sameConfig != null) {
                doLaunchConfiguration(sameConfig, mode);
            } else {
                config.doSave();
                doLaunchConfiguration(config, mode);
            }
        } catch (final CoreException e) {
            DetailedErrorDialog.openErrorDialog("Cannot generate Robot Launch Configuration",
                    "RED was unable to create Robot Launch Configuration from selection.");
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

    private void createAndLaunchConfigurationForSelectedTestCases(final IStructuredSelection selection,
            final String mode) {
        final Optional<Map<IResource, List<String>>> resourcesToTests = mapResourcesToTestCases(selection);
        if (resourcesToTests.isPresent()) {
            try {
                final ILaunchConfiguration config = RobotLaunchConfiguration
                        .prepareForSelectedTestCases(resourcesToTests.get());
                final ILaunchConfiguration sameConfig = RobotLaunchConfigurationFinder.findSameAs(config);
                if (sameConfig != null) {
                    doLaunchConfiguration(sameConfig, mode);
                } else {
                    doLaunchConfiguration(config, mode);
                }
            } catch (final CoreException e) {
                DetailedErrorDialog.openErrorDialog("Cannot generate Robot Launch Configuration",
                        "RED was unable to create Robot Launch Configuration from selection.");
            }
        }
    }

    @Override
    public ILaunchConfiguration[] getLaunchConfigurations(final ISelection selection) {
        ILaunchConfiguration config = null;
        if (!selection.isEmpty() && selection instanceof IStructuredSelection) {
            try {
                final IStructuredSelection ss = (IStructuredSelection) selection;
                if (SelectionsPropertyTester.testIfAllElementsAreFromSameProject(ss, true)) {
                    final List<IResource> resources = Selections.getAdaptableElements(ss, IResource.class);
                    if (!resources.isEmpty()) {
                        config = RobotLaunchConfigurationFinder
                                .getLaunchConfigurationExceptSelectedTestCases(resources);
                    } else {
                        final Optional<Map<IResource, List<String>>> resourcesToTests = mapResourcesToTestCases(ss);
                        if (resourcesToTests.isPresent()) {
                            config = RobotLaunchConfigurationFinder
                                    .getLaunchConfigurationForSelectedTestCases(resourcesToTests.get());
                            new RobotLaunchConfiguration(config).updateTestCases(resourcesToTests.get());
                        }
                    }
                }
            } catch (final CoreException e) {
                // nothing to do
            }
        }
        return config == null ? null : new ILaunchConfiguration[] { config };
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
    public void launch(final IEditorPart editor, final String mode) {
        final IEditorInput input = editor.getEditorInput();
        if (input instanceof FileEditorInput) {
            final IResource file = ((FileEditorInput) input).getFile();
            createAndLaunchConfiguration(newArrayList(file), mode);
        }
    }

    @Override
    public ILaunchConfiguration[] getLaunchConfigurations(final IEditorPart editorpart) {
        final IEditorInput input = editorpart.getEditorInput();
        if (input instanceof FileEditorInput) {
            try {
                final IResource file = ((FileEditorInput) input).getFile();
                final ILaunchConfigurationWorkingCopy config = RobotLaunchConfiguration
                        .prepareDefault(newArrayList(file));
                final ILaunchConfiguration sameConfig = RobotLaunchConfigurationFinder.findSameAs(config);
                return new ILaunchConfiguration[] { sameConfig != null ? sameConfig : config };
            } catch (final CoreException e) {
                return null;
            }
        }
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

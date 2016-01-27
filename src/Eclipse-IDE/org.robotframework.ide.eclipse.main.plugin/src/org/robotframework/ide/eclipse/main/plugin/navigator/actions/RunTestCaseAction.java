/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.navigator.actions;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.robotframework.ide.eclipse.main.plugin.launch.RobotLaunchConfiguration;
import org.robotframework.ide.eclipse.main.plugin.launch.RobotLaunchConfigurationDelegate;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCase;
import org.robotframework.red.viewers.Selections;

public class RunTestCaseAction extends Action implements IEnablementUpdatingAction {

    private final ISelectionProvider selectionProvider;

    private final Mode mode;

    public RunTestCaseAction(final ISelectionProvider selectionProvider, final Mode mode) {
        super(mode.actionName, mode.getImage());
        this.selectionProvider = selectionProvider;
        this.mode = mode;
    }

    @Override
    public void run() {
        runTestCase((IStructuredSelection) selectionProvider.getSelection(), mode);
    }

    public static void runTestCase(final IStructuredSelection selection, final Mode mode) {
        final WorkspaceJob job = new WorkspaceJob("Launching Robot Tests") {

            @Override
            public IStatus runInWorkspace(final IProgressMonitor monitor) throws CoreException {

                final List<RobotCase> selectedTestCases = Selections.getElements(selection, RobotCase.class);

                final List<IResource> suiteFiles = new ArrayList<IResource>();
                final List<String> testCasesNames = new ArrayList<String>();
                for (final RobotCase robotCase : selectedTestCases) {
                    final IResource suiteFile = robotCase.getSuiteFile().getFile();
                    if (!suiteFiles.contains(suiteFile)) {
                        suiteFiles.add(suiteFile);
                    }
                    testCasesNames.add(RobotLaunchConfigurationDelegate.createSuiteName(suiteFile) + "."
                            + robotCase.getName());
                }

                RobotLaunchConfiguration.createLaunchConfigurationForSelectedTestCases(suiteFiles, testCasesNames)
                        .launch(mode.launchMgrName, monitor);

                return Status.OK_STATUS;
            }
        };
        job.setUser(false);
        job.schedule();
    }

    @Override
    public void updateEnablement(final IStructuredSelection selection) {
        setEnabled(!Selections.getElements(selection, RobotCase.class).isEmpty());
    }

    public static enum Mode {
        RUN("Run", ILaunchManager.RUN_MODE, IDebugUIConstants.IMG_ACT_RUN),
        DEBUG("Debug", ILaunchManager.DEBUG_MODE, IDebugUIConstants.IMG_ACT_DEBUG);
        
        private final String actionName;

        private final String launchMgrName;

        private final String imageConst;

        private Mode(final String actionName, final String launchMgrName, final String imageConst) {
            this.actionName = actionName;
            this.launchMgrName = launchMgrName;
            this.imageConst = imageConst;
        }

        ImageDescriptor getImage() {
            return DebugUITools.getImageDescriptor(imageConst);
        }
    }
}

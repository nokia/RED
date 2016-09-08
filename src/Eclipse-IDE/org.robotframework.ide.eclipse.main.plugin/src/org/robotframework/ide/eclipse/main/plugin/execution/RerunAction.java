/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.execution;

import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.robotframework.ide.eclipse.main.plugin.RedImages;

public class RerunAction extends Action implements IWorkbenchAction {

    private static final String ID = "org.robotframework.action.executionView.RerunAction";

    public RerunAction() {
        super("Rerun Tests", RedImages.getRelaunchImage());
        setId(ID);
    }

    @Override
    public void run() {

        final ILaunch[] launches = DebugPlugin.getDefault().getLaunchManager().getLaunches();
        if (launches != null && launches.length > 0) {
            final WorkspaceJob job = new WorkspaceJob("Launching Robot Tests") {

                @Override
                public IStatus runInWorkspace(final IProgressMonitor monitor) throws CoreException {
                    final ILaunchConfiguration launchConfig = launches[launches.length-1].getLaunchConfiguration();
                    if (launchConfig != null) {
                        launchConfig.launch(ILaunchManager.RUN_MODE, monitor);
                    }
                    return Status.OK_STATUS;
                }
            };
            job.setUser(false);
            job.schedule();
        }
    }

    @Override
    public void dispose() {

    }

}

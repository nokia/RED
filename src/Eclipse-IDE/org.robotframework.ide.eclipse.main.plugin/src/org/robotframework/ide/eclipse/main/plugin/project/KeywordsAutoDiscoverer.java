/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Shell;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;

/**
 * @author bembenek
 */
public class KeywordsAutoDiscoverer extends AbstractAutoDiscoverer {

    public KeywordsAutoDiscoverer(final RobotProject robotProject) {
        super(robotProject, Collections.<IResource> emptyList());
    }

    @Override
    public void start(final Shell parent) {
        if (isDryRunRunning.compareAndSet(false, true)) {
            try {
                new ProgressMonitorDialog(parent).run(true, true, new IRunnableWithProgress() {

                    @Override
                    public void run(final IProgressMonitor monitor)
                            throws InvocationTargetException, InterruptedException {

                        try {
                            startDiscovering(monitor);
                            robotProject.updateKeywordSources(dryRunOutputParser.getKeywordSources());
                        } catch (final InvocationTargetException e) {
                            MessageDialog.openError(parent, "Discovering libraries",
                                    "Problems occurred during discovering libraries: " + e.getCause().getMessage());
                        } finally {
                            isDryRunRunning.set(false);
                        }
                    }
                });
            } catch (InvocationTargetException | InterruptedException e) {
                dryRunHandler.destroyDryRunProcess();
            }
        }
    }

}

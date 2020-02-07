/*
* Copyright 2016 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.robotframework.ide.eclipse.main.plugin.navigator.handlers;

import java.util.List;

import javax.inject.Named;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.rf.ide.core.project.RobotProjectConfig;
import org.robotframework.ide.eclipse.main.plugin.navigator.handlers.ConfigureRobotNatureHandler.E4ConfigureRobotNatureHandler;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectNature;
import org.robotframework.red.commands.DIParameterizedHandler;
import org.robotframework.red.swt.SwtThread;
import org.robotframework.red.swt.SwtThread.Evaluation;
import org.robotframework.red.viewers.Selections;

public class ConfigureRobotNatureHandler extends DIParameterizedHandler<E4ConfigureRobotNatureHandler> {

    public ConfigureRobotNatureHandler() {
        super(E4ConfigureRobotNatureHandler.class);
    }

    public static class E4ConfigureRobotNatureHandler {

        @Execute
        public void configure(final @Named(Selections.SELECTION) IStructuredSelection selection,
                @Named("org.robotframework.red.configureRobotNature.enablement") final String enablement)
                throws CoreException {

            final List<IProject> projects = Selections.getAdaptableElements(selection, IProject.class);

            final WorkspaceJob job = new WorkspaceJob("Configuring Project nature") {

                @Override
                public IStatus runInWorkspace(final IProgressMonitor monitor) throws CoreException {
                    for (final IProject project : projects) {
                        if ("enable".equalsIgnoreCase(enablement)) {
                            RobotProjectNature.addRobotNature(project, new NullProgressMonitor(),
                                    E4ConfigureRobotNatureHandler::shouldRedXmlBeReplaced);

                        } else if ("disable".equalsIgnoreCase(enablement)) {
                            RobotProjectNature.removeRobotNature(project, new NullProgressMonitor(),
                                    E4ConfigureRobotNatureHandler::shouldRedXmlBeRemoved);
                        }
                    }
                    return Status.OK_STATUS;
                }
            };
            job.setUser(false);
            job.schedule();
        }

        private static boolean shouldRedXmlBeRemoved(final String projectName) {
            return SwtThread.syncEval(Evaluation.of(() -> {
                final MessageDialog dialog = new MessageDialog(Display.getCurrent().getActiveShell(),
                        "Confirm configuration file removal", null,
                        String.format(
                                "You have deconfigured the project '%s' as a Robot project.\n"
                                        + "Do you want to remove project configuration file '%s' too?",
                                projectName, RobotProjectConfig.FILENAME),
                        MessageDialog.QUESTION, new String[] { "Leave", "Remove" }, 0);
                return dialog.open() == 1;
            }));
        }

        private static boolean shouldRedXmlBeReplaced(final String projectName) {
            return SwtThread.syncEval(Evaluation.of(() -> {
                final MessageDialog dialog = new MessageDialog(Display.getCurrent().getActiveShell(),
                        "Confirm configuration file replacement", null,
                        String.format(
                                "You have configured the project '%s' as a Robot project.\n"
                                        + "Do you want to replace project configuration file '%s' too?",
                                projectName, RobotProjectConfig.FILENAME),
                        MessageDialog.QUESTION, new String[] { "Leave", "Replace" }, 0);
                return dialog.open() == 1;
            }));
        }
    }
}

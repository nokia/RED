package org.robotframework.ide.eclipse.main.plugin.project.build.fix;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.statushandlers.StatusManager;
import org.robotframework.ide.core.executor.ILineHandler;
import org.robotframework.ide.core.executor.RobotRuntimeEnvironment;
import org.robotframework.ide.core.executor.RobotRuntimeEnvironment.RobotEnvironmentException;
import org.robotframework.ide.eclipse.main.plugin.RobotFramework;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotProblem;


public class InstallRobotUsingPipFixer extends MissingPythonInstallationFixer {

    @Override
    public String getLabel() {
        return "Try to install Robot using PIP";
    }

    @Override
    public void run(final IMarker marker) {
        final IProject project = marker.getResource().getProject();
        final RobotProject robotProject = RobotFramework.getModelManager().getModel().createRobotProject(project);
        final RobotRuntimeEnvironment runtimeEnvironment = robotProject.getRuntimeEnvironment();

        try {
            updateRobotFramework(getActiveShell(), runtimeEnvironment);
            project.deleteMarkers(RobotProblem.TYPE_ID, true, IResource.DEPTH_INFINITE);
            project.build(IncrementalProjectBuilder.FULL_BUILD, null);
        } catch (final CoreException e) {
            StatusManager.getManager().handle(new Status(IStatus.ERROR, RobotFramework.PLUGIN_ID, e.getMessage()),
                    StatusManager.SHOW);
        }
    }

    public static void updateRobotFramework(final Shell shell, final RobotRuntimeEnvironment selectedInstalation) {
        try {
            final boolean downloadStableVersion = MessageDialog.openQuestion(shell, "Installing Robot Framework",
                    "Do you want to install/upgrade to current stable version? Choose "
                            + "'No' if you prefer preview version (which may be unstable).");

            final ProgressMonitorDialog progressDialog = new ProgressMonitorDialog(shell);
            progressDialog.getProgressMonitor().setTaskName("Installing Robot Framework");
            progressDialog.run(true, false, new IRunnableWithProgress() {

                @Override
                public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                    monitor.beginTask("Installing Robot Framework", IProgressMonitor.UNKNOWN);
                    try {
                        final ILineHandler linesHandler = new ILineHandler() {
                            @Override
                            public void processLine(final String line) {
                                // pip is indenting some minor messages
                                // with spaces, so we
                                // will only show major ones in progress
                                if (!line.startsWith(" ")) {
                                    monitor.subTask(line);
                                }
                                }
                        };
                        selectedInstalation.installRobotUsingPip(linesHandler, downloadStableVersion);
                    } catch (final RobotEnvironmentException e) {
                        StatusManager.getManager().handle(
                                new Status(IStatus.ERROR, RobotFramework.PLUGIN_ID, e.getMessage()),
                                StatusManager.BLOCK);
                    }
                    }
            });
        } catch (InvocationTargetException | InterruptedException e) {
            StatusManager.getManager().handle(new Status(IStatus.ERROR, RobotFramework.PLUGIN_ID, e.getMessage()),
                    StatusManager.SHOW);
        }
    }

    private static Shell getActiveShell() {
        final IWorkbench workbench = PlatformUI.getWorkbench();
        final IWorkbenchWindow workbenchWindow = workbench.getActiveWorkbenchWindow();
        return workbenchWindow != null ? workbenchWindow.getShell() : null;
    }
}

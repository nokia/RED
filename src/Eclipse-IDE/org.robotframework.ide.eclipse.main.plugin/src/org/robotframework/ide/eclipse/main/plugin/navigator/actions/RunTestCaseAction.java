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
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchPage;
import org.robotframework.ide.eclipse.main.plugin.launch.RobotLaunchConfiguration;
import org.robotframework.ide.eclipse.main.plugin.launch.RobotLaunchConfigurationDelegate;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCase;
import org.robotframework.red.viewers.Selections;

public class RunTestCaseAction extends Action implements IEnablementUpdatingAction {

    private final IWorkbenchPage page;

    private final ISelectionProvider selectionProvider;

    private String mode;

    public RunTestCaseAction(final IWorkbenchPage page, final ISelectionProvider selectionProvider, final String mode) {
        super("Run");
        if (ILaunchManager.DEBUG_MODE.equals(mode)) {
            setText("Debug");
        }
        this.page = page;
        this.selectionProvider = selectionProvider;
        this.mode = mode;
    }

    @Override
    public void run() {

        final WorkspaceJob job = new WorkspaceJob("Launching Robot Tests") {

            @Override
            public IStatus runInWorkspace(final IProgressMonitor monitor) throws CoreException {

                final List<RobotCase> selectedTestCases = Selections.getElements(
                        (IStructuredSelection) selectionProvider.getSelection(), RobotCase.class);

                final List<IResource> suiteFiles = new ArrayList<IResource>();
                final List<String> testCasesNames = new ArrayList<String>();
                for (RobotCase robotCase : selectedTestCases) {
                    final IResource suiteFile = robotCase.getSuiteFile().getFile();
                    if (!suiteFiles.contains(suiteFile)) {
                        suiteFiles.add(suiteFile);
                    }
                    testCasesNames.add(RobotLaunchConfigurationDelegate.createSuiteName(suiteFile) + "."
                            + robotCase.getName());
                }

                RobotLaunchConfiguration.createLaunchConfigurationForSelectedTestCases(suiteFiles, testCasesNames)
                        .launch(mode, monitor);

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

}

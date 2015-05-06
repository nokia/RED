package org.robotframework.ide.eclipse.main.plugin.launch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.LaunchConfigurationDelegate;
import org.eclipse.debug.internal.ui.views.console.ProcessConsole;
import org.eclipse.debug.ui.ILaunchShortcut;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.progress.UIJob;
import org.robotframework.ide.core.executor.IRobotOutputListener;
import org.robotframework.ide.core.executor.RobotExecutor;
import org.robotframework.ide.eclipse.main.plugin.debug.RobotPartListener;
import org.robotframework.ide.eclipse.main.plugin.debug.model.RobotDebugTarget;
import org.robotframework.ide.eclipse.main.plugin.launch.tabs.RobotLaunchConfigurationMainTab;

public class RobotLaunchConfigurationDelegate extends LaunchConfigurationDelegate implements
        ILaunchConfigurationDelegate, ILaunchShortcut {

    private static final String ROBOT_LAUNCH_CONFIGURATION_TYPE = "org.robotframework.ide.robotLaunchConfiguration";

    private ILaunchConfigurationType launchConfigurationType;

    private ILaunchManager manager;
    
    private RobotEventBroker robotEventBroker;

    public RobotLaunchConfigurationDelegate() {
        manager = DebugPlugin.getDefault().getLaunchManager();
        launchConfigurationType = manager.getLaunchConfigurationType(ROBOT_LAUNCH_CONFIGURATION_TYPE);
        IEventBroker broker = (IEventBroker) PlatformUI.getWorkbench().getService(IEventBroker.class);
        robotEventBroker = new RobotEventBroker(broker);
    }

    @Override
    public void launch(final ILaunchConfiguration configuration, final String mode, final ILaunch launch,
            final IProgressMonitor monitor) throws CoreException {
        
        String projectNameAttribute = configuration.getAttribute(
                RobotLaunchConfigurationMainTab.PROJECT_NAME_ATTRIBUTE, "");
        IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectNameAttribute);

        if (project.exists()) {
            String resourceNameAttribute = configuration.getAttribute(
                    RobotLaunchConfigurationMainTab.RESOURCE_NAME_ATTRIBUTE, "");
            if (!resourceNameAttribute.equals("")) {

                clearMessageLogView();
                String executorNameAttribute = configuration.getAttribute(
                        RobotLaunchConfigurationMainTab.EXECUTOR_NAME_ATTRIBUTE, "");
                String executorArgsAttribute = configuration.getAttribute(
                        RobotLaunchConfigurationMainTab.EXECUTOR_ARGUMENTS_ATTRIBUTE, "");

                RobotExecutor robotExecutor = new RobotExecutor();
                robotExecutor.createTestRunnerAgentFile();
                Process process = null;
                RobotPartListener robotPartListener = null;

                if (mode.equals(ILaunchManager.RUN_MODE)) {

                    List<String> suites = new ArrayList<String>();
                    String[] resourceNames = resourceNameAttribute.split(RobotLaunchConfigurationMainTab.RESOURCES_SEPARATOR);
                    for (int i = 0; i < resourceNames.length; i++) {
                        IFile file = project.getFile(resourceNames[i]);
                        if (file.exists()) {
                            IContainer parent = file.getParent();
                            String fileName = file.getName();
                            String fileNameWithoutExtension = fileName.substring(0, fileName.lastIndexOf("."));
                            if (parent != null && parent.getType() == IResource.FOLDER) {
                                suites.add(parent.getName() + "." + fileNameWithoutExtension);
                            } else {
                                suites.add(fileNameWithoutExtension);
                            }
                        } else {
                            IFolder folder = project.getFolder(resourceNames[i]);
                            if (folder.exists()) {
                                suites.add(folder.getName());
                            }
                        }
                    }
                    String[] cmd = robotExecutor.createCommand(project.getLocation().toFile(), executorNameAttribute,
                            suites, executorArgsAttribute, false);

                    robotExecutor.startTestRunnerAgentHandler(new IRobotOutputListener() {

                        @Override
                        public void handleLine(String line) {
                            robotEventBroker.sendAppendLineEventToMessageLogView(line);
                        }
                    });
                    process = DebugPlugin.exec(cmd, project.getLocation().toFile());
                    DebugPlugin.newProcess(launch, process, executorNameAttribute);
                    printCommandOnConsole(cmd, executorNameAttribute);

                } else if (mode.equals(ILaunchManager.DEBUG_MODE)) {

                    IFile file = project.getFile(resourceNameAttribute);
                    if (file.exists()) {
                        new ShowDebugPerspectiveJob().schedule();

                        List<String> suiteList = new ArrayList<String>();
                        String fileName = file.getName();
                        String fileNameWithoutExtension = fileName.substring(0, fileName.lastIndexOf("."));
                        IContainer parent = file.getParent();
                        if (parent != null && parent.getType() == IResource.FOLDER) {
                            suiteList.add(parent.getName() + "." + fileNameWithoutExtension);
                        } else {
                            suiteList.add(fileNameWithoutExtension);
                        }

                        String[] cmd = robotExecutor.createCommand(project.getLocation().toFile(),
                                executorNameAttribute, suiteList, executorArgsAttribute, true);

                        process = DebugPlugin.exec(cmd, project.getLocation().toFile());
                        IProcess eclipseProcess = DebugPlugin.newProcess(launch, process, executorNameAttribute);
                        printCommandOnConsole(cmd, executorNameAttribute);

                        robotPartListener = new RobotPartListener(robotEventBroker);
                        new TogglePartListenerJob(robotPartListener, false).schedule();

                        IDebugTarget target = new RobotDebugTarget(launch, eclipseProcess, 0, file, robotPartListener, robotEventBroker);
                        launch.addDebugTarget(target);
                    }
                }

                try {
                    if (process != null) {
                        process.waitFor();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    if (robotPartListener != null) {
                        new TogglePartListenerJob(robotPartListener, true).schedule();
                    }
                    robotExecutor.removeTestRunnerAgentFile();
                }
            }
        }
    }

    @Override
    public void launch(final ISelection selection, final String mode) {

        if (selection instanceof IStructuredSelection) {
            WorkspaceJob job = new WorkspaceJob("Launching Robot Tests") {

                @Override
                public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {
                    clearMessageLogView();
                    List<IResource> resourcesList = new ArrayList<>();
                    for (Object element : ((IStructuredSelection) selection).toArray()) {
                        if (element instanceof IResource) {
                            resourcesList.add((IResource) element);
                        }
                    }

                    launchWithExistingOrNewConfiguration(resourcesList, mode, monitor);

                    return Status.OK_STATUS;
                }
            };
            job.setUser(false);
            job.schedule();
        }
    }

    @Override
    public void launch(IEditorPart editor, final String mode) {

        IEditorInput input = editor.getEditorInput();
        if (input instanceof FileEditorInput) {
            final IFile file = ((FileEditorInput) input).getFile();
            WorkspaceJob job = new WorkspaceJob("Launching Robot Tests") {

                @Override
                public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {
                    clearMessageLogView();
                    List<IResource> resourcesList = new ArrayList<>();
                    resourcesList.add(file);
                    launchWithExistingOrNewConfiguration(resourcesList, mode, monitor);

                    return Status.OK_STATUS;
                }
            };
            job.setUser(false);
            job.schedule();
        }
    }

    private void launchWithExistingOrNewConfiguration(List<IResource> resources, String mode, IProgressMonitor monitor) {

        try {
            ILaunchConfiguration[] configurations = findLaunchConfgurations(resources);
            if (configurations.length == 0) {
                IProject project = resources.get(0).getProject();
                StringBuilder resourceNameAttribute = new StringBuilder();
                for (int i = 0; i < resources.size(); i++) {
                    IResource resource = resources.get(i);
                    if (resource.getType() == IResource.FILE) {
                        IContainer parent = resource.getParent();
                        if (parent.getType() == IResource.FOLDER) {
                            resourceNameAttribute.append(parent.getName() + "/" + resource.getName());
                        } else {
                            resourceNameAttribute.append(resource.getName());
                        }
                    } else if (resource.getType() == IResource.FOLDER) {
                        resourceNameAttribute.append(resource.getName());
                    }
                    if (resources.size() > 1 && i < resources.size() - 1) {
                        resourceNameAttribute.append(RobotLaunchConfigurationMainTab.RESOURCES_SEPARATOR);
                    }
                }

                String configurationName = "";
                if (resources.size() > 1) {
                    configurationName = project.getName() + "_new_configuration";
                } else {
                    configurationName = manager.generateLaunchConfigurationName(resources.get(0).getName());
                }
                ILaunchConfigurationWorkingCopy configuration = launchConfigurationType.newInstance(null,
                        configurationName);
                configuration.setAttribute(RobotLaunchConfigurationMainTab.PROJECT_NAME_ATTRIBUTE, project.getName());
                configuration.setAttribute(RobotLaunchConfigurationMainTab.RESOURCE_NAME_ATTRIBUTE,
                        resourceNameAttribute.toString());
                configuration.setAttribute(RobotLaunchConfigurationMainTab.EXECUTOR_NAME_ATTRIBUTE,
                        RobotLaunchConfigurationMainTab.PYBOT_NAME);

                configuration.doSave();

                configurations = new ILaunchConfiguration[] { configuration };

            }

            configurations[0].launch(mode, monitor);

        } catch (CoreException e) {
            e.printStackTrace();
        }

    }

    private ILaunchConfiguration[] findLaunchConfgurations(List<IResource> selectedResources) {

        List<ILaunchConfiguration> configurations = new ArrayList<>();
        try {
            for (ILaunchConfiguration configuration : manager.getLaunchConfigurations(launchConfigurationType)) {
                String projectName = configuration.getAttribute(RobotLaunchConfigurationMainTab.PROJECT_NAME_ATTRIBUTE,
                        "");
                IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
                if (project.exists()) {
                    String configurationResourcesNames = configuration.getAttribute(
                            RobotLaunchConfigurationMainTab.RESOURCE_NAME_ATTRIBUTE, "");
                    String[] configurationResources = configurationResourcesNames.split(RobotLaunchConfigurationMainTab.RESOURCES_SEPARATOR);
                    if (configurationResources.length == selectedResources.size()) {
                        boolean hasResources = true;
                        for (int i = 0; i < configurationResources.length; i++) {
                            if (!selectedResources.contains(project.getFile(configurationResources[i]))
                                    && !selectedResources.contains(project.getFolder(configurationResources[i]))) {
                                hasResources = false;
                                break;
                            }
                        }
                        if (hasResources) {
                            configurations.add(configuration);
                            return configurations.toArray(new ILaunchConfiguration[configurations.size()]);
                        }
                    }
                }
            }
        } catch (CoreException e) {
            e.printStackTrace();
        }

        return configurations.toArray(new ILaunchConfiguration[configurations.size()]);
    }

    private void clearMessageLogView() {
        robotEventBroker.sendClearEventToMessageLogView();
    }

    private void printCommandOnConsole(String[] cmd, String executor) {

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Command: ");
        for (int i = 0; i < cmd.length; i++) {
            stringBuilder.append(cmd[i] + " ");
        }

        IConsole[] existingConsoles = ConsolePlugin.getDefault().getConsoleManager().getConsoles();
        for (int i = 0; i < existingConsoles.length; i++) {
            if (existingConsoles[i].getName().contains(executor)) {
                try {
                    ((ProcessConsole) existingConsoles[i]).newOutputStream().write(stringBuilder.toString() + '\n');
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class ShowDebugPerspectiveJob extends UIJob {

        public ShowDebugPerspectiveJob() {
            super("Show Debug Perspective");
            setSystem(true);
            setPriority(Job.INTERACTIVE);
        }

        /*
         * (non-Javadoc)
         * @see
         * org.eclipse.ui.progress.UIJob#runInUIThread(org.eclipse.core.runtime.IProgressMonitor)
         */
        @Override
        public IStatus runInUIThread(IProgressMonitor monitor) {

            IWorkbench workbench = PlatformUI.getWorkbench();
            try {
                workbench.showPerspective("org.eclipse.debug.ui.DebugPerspective", workbench.getActiveWorkbenchWindow());
            } catch (WorkbenchException e) {
                e.printStackTrace();
            }

            return Status.OK_STATUS;
        }
    }

    private class TogglePartListenerJob extends UIJob {

        private RobotPartListener listener;

        private boolean shouldBeRemoved;

        public TogglePartListenerJob(RobotPartListener listener, boolean shouldBeRemoved) {
            super("Toggle Part Listener");
            setSystem(true);
            setPriority(Job.INTERACTIVE);
            this.listener = listener;
            this.shouldBeRemoved = shouldBeRemoved;
        }

        /*
         * (non-Javadoc)
         * @see
         * org.eclipse.ui.progress.UIJob#runInUIThread(org.eclipse.core.runtime.IProgressMonitor)
         */
        @Override
        public IStatus runInUIThread(IProgressMonitor monitor) {
            if (shouldBeRemoved) {
                PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().removePartListener(listener);
            } else {
                PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().addPartListener(listener);
            }

            return Status.OK_STATUS;
        }
    }

}

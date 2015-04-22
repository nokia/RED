package org.robotframework.ide.eclipse.main.plugin.launch;

import java.nio.file.Path;
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
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.ui.ILaunchShortcut;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;
import org.robotframework.ide.core.executor.IRobotOutputListener;
import org.robotframework.ide.core.executor.RobotExecutor;
import org.robotframework.ide.eclipse.main.plugin.debug.model.RobotDebugTarget;
import org.robotframework.ide.eclipse.main.plugin.launch.tabs.RobotLaunchConfigurationMainTab;

public class RobotLaunchConfigurationDelegate implements ILaunchConfigurationDelegate, ILaunchShortcut {

    private static final String ROBOT_LAUNCH_CONFIGURATION_TYPE = "org.robotframework.ide.robotLaunchConfiguration";

    private static final String ROBOT_CONSOLE_NAME = "Robot Test Result";

    private RobotExecutor robotExecutor;

    private ILaunchConfigurationType launchConfigurationType;

    private ILaunchManager manager;

    private IEventBroker broker;

    public RobotLaunchConfigurationDelegate() {
        robotExecutor = new RobotExecutor();
        manager = DebugPlugin.getDefault().getLaunchManager();
        launchConfigurationType = manager.getLaunchConfigurationType(ROBOT_LAUNCH_CONFIGURATION_TYPE);
        broker = (IEventBroker) PlatformUI.getWorkbench().getService(IEventBroker.class);
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
                
                if(mode.equals(ILaunchManager.RUN_MODE)) {
                    String[] resourceNames = resourceNameAttribute.split(RobotLaunchConfigurationMainTab.RESOURCES_SEPARATOR);
                    for (int i = 0; i < resourceNames.length; i++) {
                        IFile file = project.getFile(resourceNames[i]);
                        if (file.exists()) {
                            IContainer parent = file.getParent();
                            String fileName = file.getName();
                            String fileNameWithoutExtension = fileName.substring(0, fileName.lastIndexOf("."));
                            if (parent != null) {
                                robotExecutor.addSuite(parent.getName(), fileNameWithoutExtension);
                            } else {
                                robotExecutor.addSuite("", fileNameWithoutExtension);
                            }
                        } else {
                            IFolder folder = project.getFolder(resourceNames[i]);
                            if (folder.exists()) {
                                robotExecutor.addSuite("", folder.getName());
                            }
                        }
                    }
                    
                    clearMessageLogView();
                    String executorNameAttribute = configuration.getAttribute(
                            RobotLaunchConfigurationMainTab.EXECUTOR_NAME_ATTRIBUTE, "");
                    String executorArgsAttribute = configuration.getAttribute(
                            RobotLaunchConfigurationMainTab.EXECUTOR_ARGUMENTS_ATTRIBUTE, "");
                    executeRobotTest(project, executorNameAttribute, executorArgsAttribute);
                
                } else if (mode.equals(ILaunchManager.DEBUG_MODE)) {
                    IFile file = project.getFile(resourceNameAttribute);
                    if (file.exists()) {
                        //TODO: automatically show Debug perspective
                        Path testRunnerAgentFilePath = robotExecutor.createTestRunnerAgentFile();
                        String[] commandLine = new String[] { "pybot.bat", "--suite",
                                file.getName().substring(0, file.getName().lastIndexOf(".")), "--listener",
                                testRunnerAgentFilePath.toString() + ":54470:True",
                                project.getLocation().toFile().getAbsolutePath() };
                        Process process = DebugPlugin.exec(commandLine, null);
                        IProcess p = DebugPlugin.newProcess(launch, process, "pybot.bat");
                        
                        IDebugTarget target = new RobotDebugTarget(launch, p, 0, project, file.getParent().getName()+"/"+file.getName(), robotExecutor, testRunnerAgentFilePath);
                        launch.addDebugTarget(target);
                    }
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
                        resourceNameAttribute.append(resource.getParent().getName() + "/" + resource.getName());
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

    private void executeRobotTest(IProject project, String executorName, String arguments) {
        ExecutorOutputStreamListener executorOutputStreamListener = new ExecutorOutputStreamListener(ROBOT_CONSOLE_NAME);
        robotExecutor.addOutputStreamListener(executorOutputStreamListener);

        robotExecutor.setMessageLogListener(new IRobotOutputListener() {

            @Override
            public void handleLine(String line) {
                broker.send("MessageLogView/AppendLine", line);
            }
        });

        robotExecutor.execute(project.getLocation().toFile(), executorName, arguments);
        robotExecutor.removeOutputStreamListener(executorOutputStreamListener);
    }

    private void clearMessageLogView() {
        broker.send("MessageLogView/Clear", "");
    }
}

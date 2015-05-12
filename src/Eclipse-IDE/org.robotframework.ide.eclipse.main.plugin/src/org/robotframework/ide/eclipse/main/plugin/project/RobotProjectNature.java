package org.robotframework.ide.eclipse.main.plugin.project;

import static com.google.common.collect.Lists.newArrayList;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.ArrayList;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.robotframework.ide.eclipse.main.plugin.RobotFramework;

public class RobotProjectNature implements IProjectNature {


    private static String ROBOT_NATURE = RobotFramework.PLUGIN_ID + ".robotNature";

    private static String ROBOT_LIBRARIES_BUILDER = RobotFramework.PLUGIN_ID + ".robotLibrariesBuilder";

    public static final String BUILDPATH_FILE = ".robotbuild";

    private static String SUITE_INIT_FILE = "__init__.robot";

	private IProject project;

    public static void addRobotNature(final IProject project, final IProgressMonitor monitor) throws CoreException {
        final IProjectDescription desc = project.getDescription();

        final ArrayList<String> natures = newArrayList(desc.getNatureIds());
        natures.add(ROBOT_NATURE);
        desc.setNatureIds(natures.toArray(new String[0]));

        project.setDescription(desc, monitor);
    }

    public static void removeRobotNature(final IProject project, final IProgressMonitor monitor) throws CoreException {
        final IProjectDescription desc = project.getDescription();

        final ArrayList<String> natures = newArrayList(desc.getNatureIds());
        natures.remove(ROBOT_NATURE);
        desc.setNatureIds(natures.toArray(new String[0]));

        project.setDescription(desc, monitor);
    }

    public static IFile createRobotInitializationFile(final IFolder folder) throws CoreException {
        final IFile initFile = folder.getFile(SUITE_INIT_FILE);
        initFile.create(new ByteArrayInputStream(new byte[0]), true, new NullProgressMonitor());
        return initFile;
    }

    public static boolean isRobotSuiteInitializationFile(final IFile file) {
        return file.exists() && file.getName().equals(SUITE_INIT_FILE) && hasRobotNature(file.getProject());
    }

    public static boolean isRobotSuite(final IFolder folder) {
        final IFile file = folder.getFile(SUITE_INIT_FILE);
        return file.exists() && hasRobotNature(folder.getProject());
    }

    public static boolean hasRobotNature(final IProject project) {
        try {
            return project.hasNature(ROBOT_NATURE);
        } catch (final CoreException e) {
            return false;
        }
    }

	@Override
	public void configure() throws CoreException {
        addToBuildSpec(project, ROBOT_LIBRARIES_BUILDER);
        final File pythonInstallation = RobotFramework.getDefault().getActiveRobotInstallation().getFile();
        new BuildpathFile(project).write(RobotProjectMetadata.create(pythonInstallation));
	}

	@Override
	public void deconfigure() throws CoreException {
        removeFromBuildSpec(project, ROBOT_LIBRARIES_BUILDER);
        final IProject project1 = project;
        final IFile initFile = project1.getFile(BUILDPATH_FILE);
        if (initFile.exists()) {
            initFile.delete(true, new NullProgressMonitor());
        }
	}

    @Override
	public IProject getProject() {
		return project;
	}

	@Override
	public void setProject(final IProject project) {
		this.project = project;
	}

    private void addToBuildSpec(final IProject project, final String builderId) throws CoreException {
        if (project == null) {
            return;
        }
        final IProjectDescription description = project.getDescription();
        final ICommand[] commands = description.getBuildSpec();
        for (final ICommand command : commands) {
            if (command.getBuilderName().equals(builderId)) {
                return; // builder is already installed
            }
        }
        final ICommand command = description.newCommand();
        command.setBuilderName(builderId);
        final ICommand[] newCommands = new ICommand[commands.length + 1];
        System.arraycopy(commands, 0, newCommands, 0, commands.length);
        newCommands[newCommands.length - 1] = command;
        description.setBuildSpec(newCommands);
        project.setDescription(description, null);
    }

    private void removeFromBuildSpec(final IProject project, final String builderId) throws CoreException {
        if (project == null) {
            return;
        }

        final IProjectDescription description = project.getDescription();
        final ICommand[] commands = description.getBuildSpec();
        for (int i = 0; i < commands.length; ++i) {
            if (commands[i].getBuilderName().equals(builderId)) {
                final ICommand[] newCommands = new ICommand[commands.length - 1];
                System.arraycopy(commands, 0, newCommands, 0, i);
                System.arraycopy(commands, i + 1, newCommands, i, commands.length - i - 1);
                description.setBuildSpec(newCommands);
                project.setDescription(description, null);
                return;
            }
        }
    }
}

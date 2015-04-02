package org.robotframework.ide.eclipse.main.plugin.nature;

import java.io.ByteArrayInputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.robotframework.ide.eclipse.main.plugin.RobotFrameworkPluginActivator;

public class RobotProjectNature implements IProjectNature {

    private static String ROBOT_NATURE = RobotFrameworkPluginActivator.PLUGIN_ID + ".robotNature";

    private static String SUITE_INIT_FILE = "__init__.robot";

	private IProject project;

    public static void addRobotNature(final IProject project, final IProgressMonitor monitor) throws CoreException {
        final IProjectDescription desc = project.getDescription();
        final String[] prevNatures = desc.getNatureIds();
        final String[] newNatures = new String[prevNatures.length + 1];
        System.arraycopy(prevNatures, 0, newNatures, 0, prevNatures.length);
        newNatures[prevNatures.length] = ROBOT_NATURE;
        desc.setNatureIds(newNatures);
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

    private static boolean hasRobotNature(final IProject project) {
        try {
            return project.hasNature(ROBOT_NATURE);
        } catch (final CoreException e) {
            return false;
        }
    }

	@Override
	public void configure() throws CoreException {
	}

	@Override
	public void deconfigure() throws CoreException {
	}

	@Override
	public IProject getProject() {
		return project;
	}

	@Override
	public void setProject(final IProject project) {
		this.project = project;
	}
}

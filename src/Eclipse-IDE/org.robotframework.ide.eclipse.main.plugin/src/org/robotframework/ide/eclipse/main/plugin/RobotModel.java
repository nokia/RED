package org.robotframework.ide.eclipse.main.plugin;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;

public class RobotModel {

    List<RobotProject> projects = new ArrayList<>();

    public RobotProject createRobotProject(final IProject project) {
        if (project == null) {
            return null;
        }
        final RobotProject robotProject = new RobotProject(project);
        if (projects.contains(robotProject)) {
            return projects.get(projects.indexOf(robotProject));
        } else {
            projects.add(robotProject);
            return robotProject;
        }
    }

    public RobotFolder createRobotSuite(final IFolder folder) {
        if (folder == null) {
            return null;
        }
        if (folder.getParent().equals(folder.getProject())) {
            return createRobotProject((IProject) folder.getParent()).createRobotSuite(folder);
        } else {
            return createRobotSuite((IFolder) folder.getParent()).createRobotSuite(folder);
        }
    }

    public RobotSuiteFile createSuiteFile(final IFile file) {
        if (file == null) {
            return null;
        }
        if (file.getParent().equals(file.getProject())) {
            return createRobotProject((IProject) file.getParent()).createSuiteFile(file);
        } else {
            return createRobotSuite((IFolder) file.getParent()).createSuiteFile(file);
        }
    }
}

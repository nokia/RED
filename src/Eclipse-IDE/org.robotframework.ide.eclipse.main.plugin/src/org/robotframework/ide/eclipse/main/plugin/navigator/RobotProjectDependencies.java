package org.robotframework.ide.eclipse.main.plugin.navigator;

import java.util.List;

import org.eclipse.core.resources.IProject;
import org.robotframework.ide.eclipse.main.plugin.RobotLibrary;
import org.robotframework.ide.eclipse.main.plugin.RobotModelManager;

class RobotProjectDependencies {

    private final IProject project;

    public RobotProjectDependencies(final IProject project) {
        this.project = project;
    }

    public List<RobotLibrary> getStandardLibraries() {
        return RobotModelManager.getInstance().getModel().createRobotProject(project).getStandardLibraries();
    }

    public String getVersion() {
        return RobotModelManager.getInstance().getModel().createRobotProject(project).getVersion();
    }
}
package org.robotframework.ide.eclipse.main.plugin;

import org.eclipse.core.resources.IProject;

public class RobotProject extends RobotContainer {

    RobotProject(final IProject project) {
        super(null, project);
    }

    public IProject getProject() {
        return (IProject) container;
    }
}

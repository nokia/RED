package org.robotframework.ide.eclipse.main.plugin;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import org.eclipse.core.resources.IProject;

public class RobotProject extends RobotContainer {

    RobotProject(final IProject project) {
        super(null, project);
    }

    public IProject getProject() {
        return (IProject) container;
    }

    public List<RobotLibrary> getStandardLibraries() {
        return newArrayList(new RobotLibrary("BuiltIn"));
    }
}

package org.robotframework.ide.eclipse.main.plugin.navigator;

import java.util.List;

import org.robotframework.ide.eclipse.main.plugin.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.project.library.LibrarySpecification;

class RobotProjectDependencies {

    protected final RobotProject project;

    public RobotProjectDependencies(final RobotProject robotProject) {
        this.project = robotProject;
    }

    List<LibrarySpecification> getLibraries() {
        return project.getStandardLibraries();
    }

    String getAdditionalInformation() {
        final String version = project.getVersion();
        return version == null ? "[???]" : "[" + version + "]";
    }

    String getName() {
        return "Robot Standard libraries";
    }
}
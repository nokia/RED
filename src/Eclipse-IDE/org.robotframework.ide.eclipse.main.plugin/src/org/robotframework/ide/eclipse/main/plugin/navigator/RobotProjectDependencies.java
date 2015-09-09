/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.navigator;

import java.util.List;

import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
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
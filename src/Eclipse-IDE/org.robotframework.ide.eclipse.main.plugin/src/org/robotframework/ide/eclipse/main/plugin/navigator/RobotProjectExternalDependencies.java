/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.navigator;

import java.util.List;

import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.project.library.LibrarySpecification;

class RobotProjectExternalDependencies extends RobotProjectDependencies {

    RobotProjectExternalDependencies(final RobotProject project) {
        super(project);
    }

    @Override
    List<LibrarySpecification> getLibraries() {
        return project.getReferencedLibraries();
    }

    @Override
    String getAdditionalInformation() {
        return "";
    }

    @Override
    String getName() {
        return "Robot Referenced libraries";
    }
}

/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.navigator;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig.ReferencedLibrary;
import org.robotframework.ide.eclipse.main.plugin.project.library.LibrarySpecification;

class RobotProjectExternalDependencies extends RobotProjectDependencies {

    RobotProjectExternalDependencies(final RobotProject project) {
        super(project);
    }

    @Override
    List<LibrarySpecification> getLibraries() {
        final List<LibrarySpecification> libraries = newArrayList();

        final Map<ReferencedLibrary, LibrarySpecification> libs = project.getReferencedLibrariesMappingWithNulls();
        for (final Entry<ReferencedLibrary, LibrarySpecification> entry : libs.entrySet()) {
            if (entry.getValue() != null) {
                libraries.add(entry.getValue());
            } else {
                libraries.add(new ErroneousLibrarySpecification(entry.getKey().getName()));
            }
        }
        return libraries;
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

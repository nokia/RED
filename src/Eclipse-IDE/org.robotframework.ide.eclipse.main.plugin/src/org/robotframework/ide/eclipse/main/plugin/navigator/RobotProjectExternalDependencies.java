/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.navigator;

import java.util.Map.Entry;
import java.util.stream.Stream;

import org.rf.ide.core.libraries.LibraryDescriptor;
import org.rf.ide.core.libraries.LibrarySpecification;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;

import com.google.common.base.Objects;

class RobotProjectExternalDependencies extends RobotProjectDependencies {

    RobotProjectExternalDependencies(final RobotProject project) {
        super(project);
    }

    @Override
    Stream<Entry<LibraryDescriptor, LibrarySpecification>> getLibrariesStream() {
        return project.getLibraryEntriesStream().filter(entry -> entry.getKey().isReferencedLibrary());
    }

    @Override
    String getName() {
        return "Robot Referenced libraries";
    }

    @Override
    String getAdditionalInformation() {
        return "";
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (RobotProjectExternalDependencies.class == obj.getClass()) {
            final RobotProjectExternalDependencies that = (RobotProjectExternalDependencies) obj;
            return Objects.equal(this.project, that.project) && Objects.equal(this.getName(), that.getName());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(project, getName());
    }
}

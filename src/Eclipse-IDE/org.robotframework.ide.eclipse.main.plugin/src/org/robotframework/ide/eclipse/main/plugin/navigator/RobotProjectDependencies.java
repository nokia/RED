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
import org.robotframework.ide.eclipse.main.plugin.project.library.KeywordSpecification;
import org.robotframework.ide.eclipse.main.plugin.project.library.LibrarySpecification;

import com.google.common.base.Objects;

class RobotProjectDependencies {

    protected final RobotProject project;

    public RobotProjectDependencies(final RobotProject robotProject) {
        this.project = robotProject;
    }

    List<LibrarySpecification> getLibraries() {
        final List<LibrarySpecification> libraries = newArrayList();

        final Map<String, LibrarySpecification> libs = project.getStandardLibrariesMappingWithNulls();
        for (final Entry<String, LibrarySpecification> entry : libs.entrySet()) {
            if (entry.getValue() != null) {
                libraries.add(entry.getValue());
            } else {
                libraries.add(new ErroneousLibrarySpecification(entry.getKey()));
            }
        }
        return libraries;
    }

    String getAdditionalInformation() {
        final String version = project.getVersion();
        return version == null ? "[???]" : "[" + version + "]";
    }

    String getName() {
        return "Robot Standard libraries";
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (RobotProjectDependencies.class == obj.getClass()) {
            final RobotProjectDependencies that = (RobotProjectDependencies) obj;
            return Objects.equal(this.getLibraries(), that.getLibraries());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getLibraries());
    }

    static class ErroneousLibrarySpecification extends LibrarySpecification {

        public ErroneousLibrarySpecification(final String name) {
            setName(name);
        }

        @Override
        public List<KeywordSpecification> getKeywords() {
            return newArrayList();
        }

    }
}
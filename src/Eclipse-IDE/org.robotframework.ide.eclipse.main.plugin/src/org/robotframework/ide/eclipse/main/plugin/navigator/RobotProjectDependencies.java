/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.navigator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Stream;

import org.rf.ide.core.libraries.KeywordSpecification;
import org.rf.ide.core.libraries.LibraryDescriptor;
import org.rf.ide.core.libraries.LibrarySpecification;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;

import com.google.common.base.Objects;

public class RobotProjectDependencies {

    protected final RobotProject project;

    public RobotProjectDependencies(final RobotProject robotProject) {
        this.project = robotProject;
    }

    List<LibrarySpecification> getLibraries() {
        final List<LibrarySpecification> libraries = new ArrayList<>();

        getLibrariesStream().forEach(entry -> {
            if (entry.getValue() != null) {
                libraries.add(entry.getValue());
            } else {
                final LibraryDescriptor descriptor = entry.getKey();
                libraries.add(new ErroneousLibrarySpecification(descriptor));
            }
        });
        return libraries;
    }

    Stream<Entry<LibraryDescriptor, LibrarySpecification>> getLibrariesStream() {
        return project.getLibraryEntriesStream().filter(entry -> entry.getKey().isStandardLibrary());
    }

    String getName() {
        return "Robot Standard libraries";
    }

    String getAdditionalInformation() {
        final String version = project.getVersion();
        return version == null ? "[???]" : "[" + version + "]";
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (RobotProjectDependencies.class == obj.getClass()) {
            final RobotProjectDependencies that = (RobotProjectDependencies) obj;
            return Objects.equal(this.project, that.project) && Objects.equal(this.getName(), that.getName());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(project, getName());
    }

    public static class ErroneousLibrarySpecification extends LibrarySpecification {

        public ErroneousLibrarySpecification(final LibraryDescriptor descriptor) {
            setDescriptor(descriptor);
        }

        @Override
        public List<KeywordSpecification> getKeywords() {
            return new ArrayList<>();
        }

        @Override
        public String getName() {
            return getDescriptor().getName();
        }

        @Override
        public String getVersion() {
            return "unknown";
        }

        @Override
        public String getScope() {
            return "unknown";
        }

        @Override
        public String getDocumentation() {
            return "";
        }

        @Override
        public boolean equals(final Object obj) {
            if (obj == null) {
                return false;
            }
            if (ErroneousLibrarySpecification.class == obj.getClass()) {
                final ErroneousLibrarySpecification that = (ErroneousLibrarySpecification) obj;
                return Objects.equal(this.getDescriptor(), that.getDescriptor());
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(getName());
        }
    }
}

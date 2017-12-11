/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.navigator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.rf.ide.core.project.RobotProjectConfig.RemoteLocation;
import org.robotframework.ide.eclipse.main.plugin.model.LibspecsFolder;
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
        final List<LibrarySpecification> libraries = new ArrayList<>();

        final LibspecsFolder libspecsFolder = LibspecsFolder.get(project.getProject());

        final Map<String, LibrarySpecification> libs = project.getStandardLibraries();
        for (final Entry<String, LibrarySpecification> entry : libs.entrySet()) {
            if (entry.getValue() != null) {
                libraries.add(entry.getValue());
            } else {
                final LibrarySpecification specification = new ErroneousLibrarySpecification(entry.getKey());
                if (entry.getKey().startsWith("Remote")) {
                    final RemoteLocation remoteLocation = RemoteLocation
                            .create(entry.getKey().substring("Remote".length()).trim());

                    specification.setRemoteLocation(remoteLocation);
                    specification.setSourceFile(libspecsFolder.getSpecFile(remoteLocation.createLibspecFileName()));
                } else {
                    specification.setSourceFile(libspecsFolder.getSpecFile(entry.getKey()));
                }
                libraries.add(specification);
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
            return new ArrayList<>();
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
                return Objects.equal(this.getName(), that.getName());
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(getName());
        }
    }
}

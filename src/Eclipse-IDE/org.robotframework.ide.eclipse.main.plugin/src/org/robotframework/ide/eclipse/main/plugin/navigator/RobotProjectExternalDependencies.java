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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.rf.ide.core.project.RobotProjectConfig.LibraryType;
import org.rf.ide.core.project.RobotProjectConfig.ReferencedLibrary;
import org.robotframework.ide.eclipse.main.plugin.model.LibspecsFolder;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.project.library.LibrarySpecification;

import com.google.common.base.Objects;

class RobotProjectExternalDependencies extends RobotProjectDependencies {

    RobotProjectExternalDependencies(final RobotProject project) {
        super(project);
    }

    @Override
    List<LibrarySpecification> getLibraries() {
        final List<LibrarySpecification> libraries = new ArrayList<>();

        final Map<ReferencedLibrary, LibrarySpecification> libs = project.getReferencedLibraries();
        for (final Entry<ReferencedLibrary, LibrarySpecification> entry : libs.entrySet()) {
            if (entry.getValue() != null) {
                libraries.add(entry.getValue());
            } else {
                final ReferencedLibrary lib = entry.getKey();
                final ErroneousLibrarySpecification specification = new ErroneousLibrarySpecification(lib.getName());

                specification.setReferenced(lib);
                specification.setSecondaryKey(lib.getPath());
                specification.setSourceFile(getSourceFile(lib));
                libraries.add(specification);
            }
        }
        return libraries;
    }

    private IFile getSourceFile(final ReferencedLibrary lib) {
        final IPath path = Path.fromPortableString(lib.getPath());
        final IResource libspec = project.getProject().getParent().findMember(path);

        final IFile sourceFile;
        if (lib.provideType() == LibraryType.VIRTUAL && libspec != null && libspec.exists()) {
            sourceFile = (IFile) libspec;
        } else {
            sourceFile = LibspecsFolder.get(project.getProject()).getSpecFile(lib.getName());
        }
        return sourceFile;
    }

    @Override
    String getAdditionalInformation() {
        return "";
    }

    @Override
    String getName() {
        return "Robot Referenced libraries";
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

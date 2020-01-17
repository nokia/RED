/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.editor.libraries;

import static org.assertj.core.api.Assertions.assertThat;
import static org.robotframework.red.junit.jupiter.ProjectExtension.getFile;

import java.util.Optional;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.rf.ide.core.libraries.LibraryDescriptor;
import org.rf.ide.core.libraries.LibrarySpecification;
import org.rf.ide.core.project.RobotProjectConfig.LibraryType;
import org.rf.ide.core.project.RobotProjectConfig.ReferencedLibrary;
import org.rf.ide.core.project.RobotProjectConfig.ReferencedLibraryArgumentsVariant;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.red.junit.jupiter.Project;
import org.robotframework.red.junit.jupiter.ProjectExtension;

import com.google.common.collect.ImmutableMap;

@ExtendWith(ProjectExtension.class)
public class LibraryLocationFinderTest {

    @Project(dirs = { "modOuter", "modOuter/modInner" },
            files = { "LibClass.py", "modOuter/__init__.py", "modOuter/modInner/__init__.py",
                    "modOuter/modInner/ModLib.py" })
    static IProject project;

    private static RobotProject robotProject;

    @BeforeAll
    public static void beforeSuite() throws Exception {
        robotProject = new RobotModel().createRobotProject(project);
    }

    @AfterAll
    public static void afterSuite() throws Exception {
        robotProject.clearConfiguration();
    }

    @Test
    public void testIfPathIsNotFound_forUnknownLibrary() throws Exception {
        final LibrarySpecification libSpec = LibrarySpecification.create("unknown");

        final Optional<IPath> location = LibraryLocationFinder.findPath(robotProject, libSpec);

        assertThat(location).isNotPresent();
    }

    @Test
    public void testIfPathIsFound_forStandardLibrary() throws Exception {
        final LibraryDescriptor libDesc = LibraryDescriptor.ofStandardLibrary("BuiltIn");
        final LibrarySpecification libSpec = LibrarySpecification.create("BuiltIn");
        libSpec.setDescriptor(libDesc);

        robotProject.setStandardLibraries(ImmutableMap.of(libDesc, libSpec));

        final Optional<IPath> location = LibraryLocationFinder.findPath(robotProject, libSpec);

        assertThat(location).hasValueSatisfying(path -> assertThat(path.lastSegment()).isEqualTo("BuiltIn.py"));
    }

    @Test
    public void testIfPathIsFound_forReferenceLibrary() throws Exception {
        final ReferencedLibrary lib = ReferencedLibrary.create(LibraryType.PYTHON, "LibClass",
                project.getName() + "/LibClass.py");
        final ReferencedLibraryArgumentsVariant variant = ReferencedLibraryArgumentsVariant.create();
        final LibraryDescriptor libDesc = LibraryDescriptor.ofReferencedLibrary(lib, variant);
        final LibrarySpecification libSpec = LibrarySpecification.create("LibClass");
        libSpec.setDescriptor(libDesc);

        robotProject.setReferencedLibraries(ImmutableMap.of(libDesc, libSpec));

        final Optional<IPath> location = LibraryLocationFinder.findPath(robotProject, libSpec);

        assertThat(location).hasValueSatisfying(
                path -> assertThat(path).isEqualTo(getFile(project, "LibClass.py").getLocation()));
    }

    @Test
    public void testIfPathIsFound_forReferenceLibraryWithQualifiedName() throws Exception {
        final ReferencedLibrary lib = ReferencedLibrary.create(LibraryType.PYTHON, "LibClass.LibClass",
                project.getName() + "/LibClass.py");
        final ReferencedLibraryArgumentsVariant variant = ReferencedLibraryArgumentsVariant.create();
        final LibraryDescriptor libDesc = LibraryDescriptor.ofReferencedLibrary(lib, variant);
        final LibrarySpecification libSpec = LibrarySpecification.create("LibClass.LibClass");
        libSpec.setDescriptor(libDesc);

        robotProject.setReferencedLibraries(ImmutableMap.of(libDesc, libSpec));

        final Optional<IPath> location = LibraryLocationFinder.findPath(robotProject, libSpec);

        assertThat(location).hasValueSatisfying(
                path -> assertThat(path).isEqualTo(getFile(project, "LibClass.py").getLocation()));
    }

    @Test
    public void testIfPathIsFound_forReferenceLibraryWithQualifiedNameAndDifferentClassName() throws Exception {
        final ReferencedLibrary lib = ReferencedLibrary.create(LibraryType.PYTHON, "LibClass.OtherClass",
                project.getName() + "/LibClass.py");
        final ReferencedLibraryArgumentsVariant variant = ReferencedLibraryArgumentsVariant.create();
        final LibraryDescriptor libDesc = LibraryDescriptor.ofReferencedLibrary(lib, variant);
        final LibrarySpecification libSpec = LibrarySpecification.create("LibClass.OtherClass");
        libSpec.setDescriptor(libDesc);

        robotProject.setReferencedLibraries(ImmutableMap.of(libDesc, libSpec));

        final Optional<IPath> location = LibraryLocationFinder.findPath(robotProject, libSpec);

        assertThat(location).hasValueSatisfying(
                path -> assertThat(path).isEqualTo(getFile(project, "LibClass.py").getLocation()));
    }

    @Test
    public void testIfPathIsFound_forReferenceLibraryModule() throws Exception {
        final ReferencedLibrary lib = ReferencedLibrary.create(LibraryType.PYTHON, "modOuter",
                project.getName() + "/modOuter/__init__.py");
        final ReferencedLibraryArgumentsVariant variant = ReferencedLibraryArgumentsVariant.create();
        final LibraryDescriptor libDesc = LibraryDescriptor.ofReferencedLibrary(lib, variant);
        final LibrarySpecification libSpec = LibrarySpecification.create("modOuter");
        libSpec.setDescriptor(libDesc);

        robotProject.setReferencedLibraries(ImmutableMap.of(libDesc, libSpec));

        final Optional<IPath> location = LibraryLocationFinder.findPath(robotProject, libSpec);

        assertThat(location).hasValueSatisfying(
                path -> assertThat(path).isEqualTo(getFile(project, "modOuter/__init__.py").getLocation()));
    }

    @Test
    public void testIfPathIsFound_forReferenceLibraryNestedModule() throws Exception {
        final ReferencedLibrary lib = ReferencedLibrary.create(LibraryType.PYTHON, "modOuter.modInner",
                project.getName() + "/modOuter/modInner/__init__.py");
        final ReferencedLibraryArgumentsVariant variant = ReferencedLibraryArgumentsVariant.create();
        final LibraryDescriptor libDesc = LibraryDescriptor.ofReferencedLibrary(lib, variant);
        final LibrarySpecification libSpec = LibrarySpecification.create("modOuter.modInner");
        libSpec.setDescriptor(libDesc);

        robotProject.setReferencedLibraries(ImmutableMap.of(libDesc, libSpec));

        final Optional<IPath> location = LibraryLocationFinder.findPath(robotProject, libSpec);

        assertThat(location).hasValueSatisfying(path -> assertThat(path)
                .isEqualTo(getFile(project, "modOuter/modInner/__init__.py").getLocation()));
    }

    @Test
    public void testIfPathIsFound_forReferenceLibraryFromModule() throws Exception {
        final ReferencedLibrary lib = ReferencedLibrary.create(LibraryType.PYTHON, "modOuter.modInner.ModLib",
                project.getName() + "/modOuter/modInner/ModLib.py");
        final ReferencedLibraryArgumentsVariant variant = ReferencedLibraryArgumentsVariant.create();
        final LibraryDescriptor libDesc = LibraryDescriptor.ofReferencedLibrary(lib, variant);
        final LibrarySpecification libSpec = LibrarySpecification.create("modOuter.modInner.ModLib");
        libSpec.setDescriptor(libDesc);

        robotProject.setReferencedLibraries(ImmutableMap.of(libDesc, libSpec));

        final Optional<IPath> location = LibraryLocationFinder.findPath(robotProject, libSpec);

        assertThat(location).hasValueSatisfying(path -> assertThat(path)
                .isEqualTo(getFile(project, "modOuter/modInner/ModLib.py").getLocation()));
    }

    @Test
    public void testIfPathIsFound_forReferenceLibraryFromModuleWithQualifiedName() throws Exception {
        final ReferencedLibrary lib = ReferencedLibrary.create(LibraryType.PYTHON, "modOuter.modInner.ModLib.ModLib",
                project.getName() + "/modOuter/modInner/ModLib.py");
        final ReferencedLibraryArgumentsVariant variant = ReferencedLibraryArgumentsVariant.create();
        final LibraryDescriptor libDesc = LibraryDescriptor.ofReferencedLibrary(lib, variant);
        final LibrarySpecification libSpec = LibrarySpecification.create("modOuter.modInner.ModLib.ModLib");
        libSpec.setDescriptor(libDesc);

        robotProject.setReferencedLibraries(ImmutableMap.of(libDesc, libSpec));

        final Optional<IPath> location = LibraryLocationFinder.findPath(robotProject, libSpec);

        assertThat(location).hasValueSatisfying(path -> assertThat(path)
                .isEqualTo(getFile(project, "modOuter/modInner/ModLib.py").getLocation()));
    }

}

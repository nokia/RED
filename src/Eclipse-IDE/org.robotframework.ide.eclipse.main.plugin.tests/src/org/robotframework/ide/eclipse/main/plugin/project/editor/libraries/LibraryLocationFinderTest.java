/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.editor.libraries;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.eclipse.core.runtime.IPath;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.rf.ide.core.libraries.LibraryDescriptor;
import org.rf.ide.core.libraries.LibrarySpecification;
import org.rf.ide.core.project.RobotProjectConfig.LibraryType;
import org.rf.ide.core.project.RobotProjectConfig.ReferencedLibrary;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.red.junit.ProjectProvider;

import com.google.common.collect.ImmutableMap;

public class LibraryLocationFinderTest {

    @ClassRule
    public static ProjectProvider projectProvider = new ProjectProvider(LibraryLocationFinderTest.class);

    private static final RobotModel model = new RobotModel();

    private static RobotProject robotProject;

    @BeforeClass
    public static void beforeSuite() throws Exception {
        robotProject = model.createRobotProject(projectProvider.getProject());
        projectProvider.createFile("LibClass.py");
        projectProvider.createDir("modOuter");
        projectProvider.createDir("modOuter/modInner");
        projectProvider.createFile("modOuter/__init__.py");
        projectProvider.createFile("modOuter/modInner/__init__.py");
        projectProvider.createFile("modOuter/modInner/ModLib.py");
    }

    @AfterClass
    public static void afterSuite() throws Exception {
        robotProject.clearConfiguration();
    }

    @Test
    public void testIfPathIsNotFound_forUnknownLibrary() throws Exception {
        final LibrarySpecification libSpec = LibrarySpecification.create("unknown");

        final Optional<IPath> location = LibraryLocationFinder.findPath(model, projectProvider.getProject(), libSpec);

        assertThat(location).isNotPresent();
    }

    @Test
    public void testIfPathIsFound_forStandardLibrary() throws Exception {
        final LibraryDescriptor libDesc = LibraryDescriptor.ofStandardLibrary("BuiltIn");
        final LibrarySpecification libSpec = LibrarySpecification.create("BuiltIn");
        libSpec.setDescriptor(libDesc);

        robotProject.setStandardLibraries(ImmutableMap.of(libDesc, libSpec));

        final Optional<IPath> location = LibraryLocationFinder.findPath(model, projectProvider.getProject(), libSpec);

        assertThat(location).hasValueSatisfying(path -> assertThat(path.lastSegment()).isEqualTo("BuiltIn.py"));
    }

    @Test
    public void testIfPathIsFound_forReferenceLibrary() throws Exception {
        final ReferencedLibrary lib = ReferencedLibrary.create(LibraryType.PYTHON, "LibClass",
                projectProvider.getProject().getName());
        final LibraryDescriptor libDesc = LibraryDescriptor.ofReferencedLibrary(lib);
        final LibrarySpecification libSpec = LibrarySpecification.create("LibClass");
        libSpec.setDescriptor(libDesc);

        robotProject.setReferencedLibraries(ImmutableMap.of(libDesc, libSpec));

        final Optional<IPath> location = LibraryLocationFinder.findPath(model, projectProvider.getProject(), libSpec);

        assertThat(location).hasValueSatisfying(
                path -> assertThat(path).isEqualTo(projectProvider.getFile("LibClass.py").getLocation()));
    }

    @Test
    public void testIfPathIsFound_forReferenceLibraryWithQualifiedName() throws Exception {
        final ReferencedLibrary lib = ReferencedLibrary.create(LibraryType.PYTHON, "LibClass.LibClass",
                projectProvider.getProject().getName());
        final LibraryDescriptor libDesc = LibraryDescriptor.ofReferencedLibrary(lib);
        final LibrarySpecification libSpec = LibrarySpecification.create("LibClass.LibClass");
        libSpec.setDescriptor(libDesc);

        robotProject.setReferencedLibraries(ImmutableMap.of(libDesc, libSpec));

        final Optional<IPath> location = LibraryLocationFinder.findPath(model, projectProvider.getProject(), libSpec);

        assertThat(location).hasValueSatisfying(
                path -> assertThat(path).isEqualTo(projectProvider.getFile("LibClass.py").getLocation()));
    }

    @Test
    public void testIfPathIsFound_forReferenceLibraryWithQualifiedNameAndDifferentClassName() throws Exception {
        final ReferencedLibrary lib = ReferencedLibrary.create(LibraryType.PYTHON, "LibClass.OtherClass",
                projectProvider.getProject().getName());
        final LibraryDescriptor libDesc = LibraryDescriptor.ofReferencedLibrary(lib);
        final LibrarySpecification libSpec = LibrarySpecification.create("LibClass.OtherClass");
        libSpec.setDescriptor(libDesc);

        robotProject.setReferencedLibraries(ImmutableMap.of(libDesc, libSpec));

        final Optional<IPath> location = LibraryLocationFinder.findPath(model, projectProvider.getProject(), libSpec);

        assertThat(location).hasValueSatisfying(
                path -> assertThat(path).isEqualTo(projectProvider.getFile("LibClass.py").getLocation()));
    }

    @Test
    public void testIfPathIsFound_forReferenceLibraryModule() throws Exception {
        final ReferencedLibrary lib = ReferencedLibrary.create(LibraryType.PYTHON, "modOuter",
                projectProvider.getProject().getName());
        final LibraryDescriptor libDesc = LibraryDescriptor.ofReferencedLibrary(lib);
        final LibrarySpecification libSpec = LibrarySpecification.create("modOuter");
        libSpec.setDescriptor(libDesc);

        robotProject.setReferencedLibraries(ImmutableMap.of(libDesc, libSpec));

        final Optional<IPath> location = LibraryLocationFinder.findPath(model, projectProvider.getProject(), libSpec);

        assertThat(location).hasValueSatisfying(
                path -> assertThat(path).isEqualTo(projectProvider.getFile("modOuter/__init__.py").getLocation()));
    }

    @Test
    public void testIfPathIsFound_forReferenceLibraryNestedModule() throws Exception {
        final ReferencedLibrary lib = ReferencedLibrary.create(LibraryType.PYTHON, "modOuter.modInner",
                projectProvider.getProject().getName());
        final LibraryDescriptor libDesc = LibraryDescriptor.ofReferencedLibrary(lib);
        final LibrarySpecification libSpec = LibrarySpecification.create("modOuter.modInner");
        libSpec.setDescriptor(libDesc);

        robotProject.setReferencedLibraries(ImmutableMap.of(libDesc, libSpec));

        final Optional<IPath> location = LibraryLocationFinder.findPath(model, projectProvider.getProject(), libSpec);

        assertThat(location).hasValueSatisfying(path -> assertThat(path)
                .isEqualTo(projectProvider.getFile("modOuter/modInner/__init__.py").getLocation()));
    }

    @Test
    public void testIfPathIsFound_forReferenceLibraryFromModule() throws Exception {
        final ReferencedLibrary lib = ReferencedLibrary.create(LibraryType.PYTHON, "modOuter.modInner.ModLib",
                projectProvider.getProject().getName());
        final LibraryDescriptor libDesc = LibraryDescriptor.ofReferencedLibrary(lib);
        final LibrarySpecification libSpec = LibrarySpecification.create("modOuter.modInner.ModLib");
        libSpec.setDescriptor(libDesc);

        robotProject.setReferencedLibraries(ImmutableMap.of(libDesc, libSpec));

        final Optional<IPath> location = LibraryLocationFinder.findPath(model, projectProvider.getProject(), libSpec);

        assertThat(location).hasValueSatisfying(path -> assertThat(path)
                .isEqualTo(projectProvider.getFile("modOuter/modInner/ModLib.py").getLocation()));
    }

    @Test
    public void testIfPathIsFound_forReferenceLibraryFromModuleWithQualifiedName() throws Exception {
        final ReferencedLibrary lib = ReferencedLibrary.create(LibraryType.PYTHON, "modOuter.modInner.ModLib.ModLib",
                projectProvider.getProject().getName());
        final LibraryDescriptor libDesc = LibraryDescriptor.ofReferencedLibrary(lib);
        final LibrarySpecification libSpec = LibrarySpecification.create("modOuter.modInner.ModLib.ModLib");
        libSpec.setDescriptor(libDesc);

        robotProject.setReferencedLibraries(ImmutableMap.of(libDesc, libSpec));

        final Optional<IPath> location = LibraryLocationFinder.findPath(model, projectProvider.getProject(), libSpec);

        assertThat(location).hasValueSatisfying(path -> assertThat(path)
                .isEqualTo(projectProvider.getFile("modOuter/modInner/ModLib.py").getLocation()));
    }

}

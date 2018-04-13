/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.refactoring;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Objects;
import java.util.Optional;

import org.assertj.core.api.Condition;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.ltk.core.refactoring.Change;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.rf.ide.core.project.RobotProjectConfig;
import org.rf.ide.core.project.RobotProjectConfig.ExcludedFolderPath;
import org.rf.ide.core.project.RobotProjectConfig.LibraryType;
import org.rf.ide.core.project.RobotProjectConfig.ReferencedLibrary;
import org.robotframework.ide.eclipse.main.plugin.project.RedEclipseProjectConfigReader;
import org.robotframework.red.junit.ProjectProvider;

public class RedXmlInFileChangesCollectorTest {

    private static final String PROJECT_NAME = RedXmlInFileChangesCollectorTest.class.getSimpleName();

    @ClassRule
    public static ProjectProvider projectProvider = new ProjectProvider(PROJECT_NAME);

    @BeforeClass
    public static void beforeSuite() throws Exception {
        projectProvider.createDir("a");
        projectProvider.createDir("a/b");
        projectProvider.createDir("c");
        projectProvider.createDir("libs");
        projectProvider.createFile("libs/lib.py", "class lib(object):", "    ROBOT_LIBRARY_VERSION = 1.0",
                "    def __init__(self):", "        pass", "    def keyword(self):", "        pass");
        projectProvider.createDir("libs/inner_lib");
        projectProvider.createFile("libs/inner_lib/__init__.py");
        projectProvider.createFile("libs/inner_lib/inside.py", "class inside(object):",
                "    ROBOT_LIBRARY_VERSION = 1.0", "    def __init__(self):", "        pass",
                "    def inside_keyword(self):", "        pass");
    }

    @Before
    public void beforeTest() throws Exception {
        final RobotProjectConfig config = new RobotProjectConfig();
        config.addExcludedPath("a");
        config.addExcludedPath("a/b");
        config.addExcludedPath("c");
        config.addReferencedLibrary(ReferencedLibrary.create(LibraryType.PYTHON, "lib", PROJECT_NAME + "/libs"));
        config.addReferencedLibrary(ReferencedLibrary.create(LibraryType.PYTHON, "inner_lib", PROJECT_NAME + "/libs"));
        config.addReferencedLibrary(
                ReferencedLibrary.create(LibraryType.PYTHON, "inner_lib.inside", PROJECT_NAME + "/libs"));
        config.addReferencedLibrary(
                ReferencedLibrary.create(LibraryType.PYTHON, "inside", PROJECT_NAME + "/libs/inner_lib"));
        projectProvider.configure(config);
    }

    @Test
    public void noChangeIsCollected_whenRemovedResourceDoesNotAffectAnything() {
        final IFile redXmlFile = projectProvider.getFile(new Path("red.xml"));

        final RedXmlInFileChangesCollector collector = new RedXmlInFileChangesCollector(redXmlFile,
                new Path(PROJECT_NAME + "/x"), Optional.empty());

        assertThat(collector.collect().isPresent()).isFalse();
    }

    @Test
    public void noChangeIsCollected_whenMovedResourceDoesNotAffectAnything() {
        final IFile redXmlFile = projectProvider.getFile(new Path("red.xml"));

        final RedXmlInFileChangesCollector collector = new RedXmlInFileChangesCollector(redXmlFile,
                new Path(PROJECT_NAME + "/x"), Optional.of(new Path(PROJECT_NAME + "/renamed")));
        final Optional<Change> change = collector.collect();

        assertThat(change.isPresent()).isFalse();
    }

    @Test
    public void textFileChangeIsCollected_whenRemovedResourceAffectsExcludedFolders() throws Exception {
        final IFile redXmlFile = projectProvider.getFile(new Path("red.xml"));

        final RedXmlInFileChangesCollector collector = new RedXmlInFileChangesCollector(redXmlFile,
                new Path(PROJECT_NAME + "/a"), Optional.empty());
        final Optional<Change> change = collector.collect();

        assertThat(change.isPresent()).isTrue();

        change.get().perform(new NullProgressMonitor());
        final RobotProjectConfig config = new RedEclipseProjectConfigReader().readConfiguration(redXmlFile);
        assertThat(config.getExcludedPath()).containsOnly(ExcludedFolderPath.create("c"));
    }

    @Test
    public void textFileChangeIsCollected_whenMovedResourceAffectsExcludedFolders() throws Exception {
        final IFile redXmlFile = projectProvider.getFile(new Path("red.xml"));

        final RedXmlInFileChangesCollector collector = new RedXmlInFileChangesCollector(redXmlFile,
                new Path(PROJECT_NAME + "/a"), Optional.of(new Path(PROJECT_NAME + "/moved")));
        final Optional<Change> change = collector.collect();

        assertThat(change.isPresent()).isTrue();

        change.get().perform(new NullProgressMonitor());
        final RobotProjectConfig config = new RedEclipseProjectConfigReader().readConfiguration(redXmlFile);
        assertThat(config.getExcludedPath()).containsOnly(ExcludedFolderPath.create("moved"),
                ExcludedFolderPath.create("moved/b"), ExcludedFolderPath.create("c"));
    }

    @Test
    public void testFileChangeIsCollected_whenRemovedResourceAffectsLibraries() throws Exception {
        final IFile redXmlFile = projectProvider.getFile(new Path("red.xml"));

        final RedXmlInFileChangesCollector collector = new RedXmlInFileChangesCollector(redXmlFile,
                new Path(PROJECT_NAME + "/libs/inner_lib"), Optional.empty());
        final Optional<Change> change = collector.collect();

        assertThat(change.isPresent()).isTrue();

        change.get().perform(new NullProgressMonitor());
        final RobotProjectConfig config = new RedEclipseProjectConfigReader().readConfiguration(redXmlFile);
        assertThat(config.getLibraries()).hasSize(1);
        assertThat(config.getLibraries().get(0))
                .has(sameFieldsAs(ReferencedLibrary.create(LibraryType.PYTHON, "lib", PROJECT_NAME + "/libs")));
    }

    @Test
    public void testFileChangeIsCollected_whenMovedResourceAffectsLibraries() throws Exception {
        final IFile redXmlFile = projectProvider.getFile(new Path("red.xml"));

        final RedXmlInFileChangesCollector collector = new RedXmlInFileChangesCollector(redXmlFile,
                new Path(PROJECT_NAME + "/libs/inner_lib"), Optional.of(new Path(PROJECT_NAME + "/libs/moved")));
        final Optional<Change> change = collector.collect();

        assertThat(change.isPresent()).isTrue();

        change.get().perform(new NullProgressMonitor());
        final RobotProjectConfig config = new RedEclipseProjectConfigReader().readConfiguration(redXmlFile);
        assertThat(config.getLibraries()).hasSize(4);
        assertThat(config.getLibraries().get(0))
                .has(sameFieldsAs(ReferencedLibrary.create(LibraryType.PYTHON, "lib", PROJECT_NAME + "/libs")));
        assertThat(config.getLibraries().get(1))
                .has(sameFieldsAs(ReferencedLibrary.create(LibraryType.PYTHON, "moved", PROJECT_NAME + "/libs")));
        assertThat(config.getLibraries().get(2)).has(
                sameFieldsAs(ReferencedLibrary.create(LibraryType.PYTHON, "moved.inside", PROJECT_NAME + "/libs")));
        assertThat(config.getLibraries().get(3)).has(
                sameFieldsAs(ReferencedLibrary.create(LibraryType.PYTHON, "inside", PROJECT_NAME + "/libs/moved")));
    }

    private static Condition<? super ReferencedLibrary> sameFieldsAs(final ReferencedLibrary library) {
        return new Condition<ReferencedLibrary>() {

            @Override
            public boolean matches(final ReferencedLibrary toMatch) {
                return Objects.equals(library.getType(), toMatch.getType())
                        && Objects.equals(library.getName(), toMatch.getName())
                        && Objects.equals(library.getPath(), toMatch.getPath());
            }
        };
    }
}

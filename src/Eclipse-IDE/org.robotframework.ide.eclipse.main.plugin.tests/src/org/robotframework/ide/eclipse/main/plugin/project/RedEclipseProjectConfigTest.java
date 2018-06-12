/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.Optional;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.rf.ide.core.SystemVariableAccessor;
import org.rf.ide.core.executor.EnvironmentSearchPaths;
import org.rf.ide.core.project.RobotProjectConfig;
import org.rf.ide.core.project.RobotProjectConfig.LibraryType;
import org.rf.ide.core.project.RobotProjectConfig.ReferencedLibrary;
import org.rf.ide.core.project.RobotProjectConfig.ReferencedVariableFile;
import org.rf.ide.core.project.RobotProjectConfig.RelativeTo;
import org.rf.ide.core.project.RobotProjectConfig.RelativityPoint;
import org.rf.ide.core.project.RobotProjectConfig.SearchPath;
import org.robotframework.red.junit.ProjectProvider;

public class RedEclipseProjectConfigTest {

    private static final String PROJECT_NAME = RedEclipseProjectConfigTest.class.getSimpleName();

    @ClassRule
    public static ProjectProvider projectProvider = new ProjectProvider(PROJECT_NAME);

    private static IProject project;

    @BeforeClass
    public static void beforeSuite() throws Exception {
        project = projectProvider.getProject();
        projectProvider.createFile("resource.txt");
    }

    @Test
    public void absolutePathIsResolved() throws Exception {
        final RobotProjectConfig projectConfig = new RobotProjectConfig();

        final RedEclipseProjectConfig redConfig = new RedEclipseProjectConfig(project, projectConfig);

        assertThat(redConfig.toAbsolutePath(project.getLocation().append("file.txt")))
                .hasValue(project.getLocation().append("file.txt").toFile());
    }

    @Test
    public void workspaceRelativePathIsResolved_forExistingResource() throws Exception {
        final RobotProjectConfig projectConfig = new RobotProjectConfig();
        projectConfig.setRelativityPoint(RelativityPoint.create(RelativeTo.WORKSPACE));

        final RedEclipseProjectConfig redConfig = new RedEclipseProjectConfig(project, projectConfig);

        assertThat(redConfig.toAbsolutePath(path("resource.txt")))
                .hasValue(project.getWorkspace().getRoot().getLocation().append("resource.txt").toFile());
    }

    @Test
    public void projectRelativePathIsResolved_forExistingResource() throws Exception {
        final RobotProjectConfig projectConfig = new RobotProjectConfig();
        projectConfig.setRelativityPoint(RelativityPoint.create(RelativeTo.PROJECT));

        final RedEclipseProjectConfig redConfig = new RedEclipseProjectConfig(project, projectConfig);

        assertThat(redConfig.toAbsolutePath(path("resource.txt")))
                .hasValue(project.getLocation().append("resource.txt").toFile());
    }

    @Test
    public void workspaceRelativePathIsResolved_forNonExistingResource() throws Exception {
        final RobotProjectConfig projectConfig = new RobotProjectConfig();
        projectConfig.setRelativityPoint(RelativityPoint.create(RelativeTo.WORKSPACE));

        final RedEclipseProjectConfig redConfig = new RedEclipseProjectConfig(project, projectConfig);

        assertThat(redConfig.toAbsolutePath(path("file.txt")))
                .hasValue(project.getWorkspace().getRoot().getLocation().append("file.txt").toFile());
    }

    @Test
    public void projectRelativePathIsResolved_forNonExistingResource() throws Exception {
        final RobotProjectConfig projectConfig = new RobotProjectConfig();
        projectConfig.setRelativityPoint(RelativityPoint.create(RelativeTo.PROJECT));

        final RedEclipseProjectConfig redConfig = new RedEclipseProjectConfig(project, projectConfig);

        assertThat(redConfig.toAbsolutePath(path("file.txt")))
                .hasValue(project.getLocation().append("file.txt").toFile());
    }

    @Test
    public void additionalEnvironmentSearchPathsContainOnlyUniqueCorrectPathsWithResolvedEnvironmentVariables()
            throws Exception {
        final SystemVariableAccessor variableAccessor = mock(SystemVariableAccessor.class);
        when(variableAccessor.getValue("KNOWN_1")).thenReturn(Optional.of("java"));
        when(variableAccessor.getValue("KNOWN_2")).thenReturn(Optional.of("python"));
        when(variableAccessor.getValue("JAR")).thenReturn(Optional.of("lib.jar"));
        when(variableAccessor.getValue("FOLDER")).thenReturn(Optional.of("folder"));

        final RobotProjectConfig projectConfig = new RobotProjectConfig();
        projectConfig.setRelativityPoint(RelativityPoint.create(RelativeTo.PROJECT));
        projectConfig.addClassPath(SearchPath.create("%{KNOWN_1}/lib.jar"));
        projectConfig.addClassPath(SearchPath.create("%{UNKNOWN_1}/unknown.jar"));
        projectConfig.addClassPath(SearchPath.create("${INCORRECT_1}/incorrect.jar"));
        projectConfig.addClassPath(SearchPath.create("%{KNOWN_1}/%{JAR}"));
        projectConfig.addPythonPath(SearchPath.create("%{KNOWN_2}/folder"));
        projectConfig.addPythonPath(SearchPath.create("%{UNKNOWN_2}/unknown"));
        projectConfig.addPythonPath(SearchPath.create("${INCORRECT_2}/incorrect"));
        projectConfig.addPythonPath(SearchPath.create("%{KNOWN_2}/%{FOLDER}"));

        final RedEclipseProjectConfig redConfig = new RedEclipseProjectConfig(project, projectConfig, variableAccessor);

        assertThat(redConfig.createAdditionalEnvironmentSearchPaths()).isEqualTo(new EnvironmentSearchPaths(
                newArrayList(absolutePath("java", "lib.jar")), newArrayList(absolutePath("python", "folder"))));
    }

    @Test
    public void executionEnvironmentSearchPathsContainOnlyUniqueCorrectPathsWithResolvedEnvironmentVariables()
            throws Exception {
        final SystemVariableAccessor variableAccessor = mock(SystemVariableAccessor.class);
        when(variableAccessor.getValue("KNOWN_1")).thenReturn(Optional.of("java"));
        when(variableAccessor.getValue("KNOWN_2")).thenReturn(Optional.of("python"));
        when(variableAccessor.getValue("JAR")).thenReturn(Optional.of("lib.jar"));
        when(variableAccessor.getValue("FOLDER")).thenReturn(Optional.of("folder"));

        final RobotProjectConfig projectConfig = new RobotProjectConfig();
        projectConfig.setRelativityPoint(RelativityPoint.create(RelativeTo.PROJECT));
        projectConfig.addClassPath(SearchPath.create("%{KNOWN_1}/lib.jar"));
        projectConfig.addClassPath(SearchPath.create("%{UNKNOWN_1}/unknown.jar"));
        projectConfig.addClassPath(SearchPath.create("${INCORRECT_1}/incorrect.jar"));
        projectConfig.addClassPath(SearchPath.create("%{KNOWN_1}/%{JAR}"));
        projectConfig.addClassPath(SearchPath.create("lib1.jar"));
        projectConfig.addPythonPath(SearchPath.create("%{KNOWN_2}/folder"));
        projectConfig.addPythonPath(SearchPath.create("%{UNKNOWN_2}/unknown"));
        projectConfig.addPythonPath(SearchPath.create("${INCORRECT_2}/incorrect"));
        projectConfig.addPythonPath(SearchPath.create("%{KNOWN_2}/%{FOLDER}"));
        projectConfig.addPythonPath(SearchPath.create("folder1"));
        projectConfig.addReferencedLibrary(
                ReferencedLibrary.create(LibraryType.JAVA, "JavaLib1", PROJECT_NAME + "/lib1.jar"));
        projectConfig.addReferencedLibrary(
                ReferencedLibrary.create(LibraryType.JAVA, "JavaLib2", PROJECT_NAME + "/lib2.jar"));
        projectConfig.addReferencedLibrary(
                ReferencedLibrary.create(LibraryType.PYTHON, "PyLib1", PROJECT_NAME + "/folder1"));
        projectConfig.addReferencedLibrary(
                ReferencedLibrary.create(LibraryType.PYTHON, "PyLib2", PROJECT_NAME + "/folder2"));

        final RedEclipseProjectConfig redConfig = new RedEclipseProjectConfig(project, projectConfig, variableAccessor);

        assertThat(redConfig.createExecutionEnvironmentSearchPaths()).isEqualTo(new EnvironmentSearchPaths(
                newArrayList(".", absolutePath("lib1.jar"), absolutePath("lib2.jar"), absolutePath("java", "lib.jar")),
                newArrayList(absolutePath("folder1"), absolutePath("folder2"), absolutePath("python", "folder"))));
    }

    @Test
    public void variableFilePathsAreReturned() throws Exception {
        final RobotProjectConfig projectConfig = new RobotProjectConfig();
        projectConfig.addReferencedVariableFile(ReferencedVariableFile.create(PROJECT_NAME + "/vars1.py"));
        projectConfig.addReferencedVariableFile(ReferencedVariableFile.create(PROJECT_NAME + "/vars2.py", "a", "b"));

        final RedEclipseProjectConfig redConfig = new RedEclipseProjectConfig(project, projectConfig);

        assertThat(redConfig.getVariableFilePaths()).containsExactly(absolutePath("vars1.py"),
                absolutePath("vars2.py:a:b"));
    }

    @Test
    public void cannotResolveAbsolutePath_whenBasePathIsNull() {
        assertThat(RedEclipseProjectConfig.resolveToAbsolutePath(null, path("path/pointing/somewhere"))).isNotPresent();
    }

    @Test
    public void cannotResolveAbsolutePath_whenRelativeIsParameterized() {
        assertThat(absoluteOf("/base", "dir/${PARAM}/file")).isNotPresent();
    }

    @Test
    public void childPathIsReturned_whenItIsAlreadyAbsolutePath() {
        assertThat(absoluteOf("/base", "/absolute")).hasValue(path("/absolute"));
        assertThat(absoluteOf("/base", "c:/dir")).hasValue(path("c:/dir"));
    }

    @Test
    public void resolvedPathIsReturned_whenBaseIsGivenWithRelativeChild() {
        assertThat(absoluteOf("/base", "relative")).hasValue(path("/relative"));
        assertThat(absoluteOf("/base.file", "relative")).hasValue(path("/relative"));

        assertThat(absoluteOf("/base/", "relative")).hasValue(path("/base/relative"));
        assertThat(absoluteOf("/base/c.file", "relative")).hasValue(path("/base/relative"));

        assertThat(absoluteOf("/base/", "relative/something")).hasValue(path("/base/relative/something"));
        assertThat(absoluteOf("/base/c.file", "relative/something")).hasValue(path("/base/relative/something"));

        assertThat(absoluteOf("/base/1/2/3/", ".././../relative/something"))
                .hasValue(path("/base/1/relative/something"));
        assertThat(absoluteOf("/base/1/2/3/c.file", ".././../relative/something"))
                .hasValue(path("/base/1/relative/something"));

        assertThat(absoluteOf("/base/", "relative path/containing!@#$%^&*();,.\"/differentchars"))
                .hasValue(path("/base/relative path/containing!@#$%^&*();,.\"/differentchars"));
    }

    private static Optional<IPath> absoluteOf(final String path1, final String path2) {
        return RedEclipseProjectConfig.resolveToAbsolutePath(new Path(path1), new Path(path2));
    }

    private static IPath path(final String path) {
        return new Path(path);
    }

    private static String absolutePath(final String... projectRelativeParts) {
        final String projectAbsPath = projectProvider.getProject().getLocation().toOSString();
        return projectAbsPath + File.separator + String.join(File.separator, projectRelativeParts);
    }
}

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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.rf.ide.core.SystemVariableAccessor;
import org.rf.ide.core.environment.EnvironmentSearchPaths;
import org.rf.ide.core.project.RobotProjectConfig;
import org.rf.ide.core.project.RobotProjectConfig.LibraryType;
import org.rf.ide.core.project.RobotProjectConfig.ReferencedLibrary;
import org.rf.ide.core.project.RobotProjectConfig.RelativeTo;
import org.rf.ide.core.project.RobotProjectConfig.RelativityPoint;
import org.rf.ide.core.project.RobotProjectConfig.SearchPath;
import org.robotframework.red.junit.jupiter.Project;
import org.robotframework.red.junit.jupiter.ProjectExtension;

@ExtendWith(ProjectExtension.class)
public class RedEclipseProjectConfigTest {

    @Project(files = { "resource.txt" })
    static IProject project;

    @Test
    public void absolutePathIsResolved() throws Exception {
        final RobotProjectConfig projectConfig = new RobotProjectConfig();

        final RedEclipseProjectConfig redConfig = new RedEclipseProjectConfig(project, projectConfig);

        assertThat(redConfig.resolveToAbsolutePath(SearchPath.create(project.getName() + "/file.txt")))
                .isEqualTo(project.getLocation().append("file.txt"));
    }

    @Test
    public void workspaceRelativePathIsResolved_forExistingResource() throws Exception {
        final RobotProjectConfig projectConfig = new RobotProjectConfig();
        projectConfig.setRelativityPoint(RelativityPoint.create(RelativeTo.WORKSPACE));

        final RedEclipseProjectConfig redConfig = new RedEclipseProjectConfig(project, projectConfig);

        assertThat(redConfig.resolveToAbsolutePath(SearchPath.create("resource.txt")))
                .isEqualTo(project.getWorkspace().getRoot().getLocation().append("resource.txt"));
    }

    @Test
    public void projectRelativePathIsResolved_forExistingResource() throws Exception {
        final RobotProjectConfig projectConfig = new RobotProjectConfig();
        projectConfig.setRelativityPoint(RelativityPoint.create(RelativeTo.PROJECT));

        final RedEclipseProjectConfig redConfig = new RedEclipseProjectConfig(project, projectConfig);

        assertThat(redConfig.resolveToAbsolutePath(SearchPath.create("resource.txt")))
                .isEqualTo(project.getLocation().append("resource.txt"));
    }

    @Test
    public void workspaceRelativePathIsResolved_forNonExistingResource() throws Exception {
        final RobotProjectConfig projectConfig = new RobotProjectConfig();
        projectConfig.setRelativityPoint(RelativityPoint.create(RelativeTo.WORKSPACE));

        final RedEclipseProjectConfig redConfig = new RedEclipseProjectConfig(project, projectConfig);

        assertThat(redConfig.resolveToAbsolutePath(SearchPath.create("file.txt")))
                .isEqualTo(project.getWorkspace().getRoot().getLocation().append("file.txt"));
    }

    @Test
    public void projectRelativePathIsResolved_forNonExistingResource() throws Exception {
        final RobotProjectConfig projectConfig = new RobotProjectConfig();
        projectConfig.setRelativityPoint(RelativityPoint.create(RelativeTo.PROJECT));

        final RedEclipseProjectConfig redConfig = new RedEclipseProjectConfig(project, projectConfig);

        assertThat(redConfig.resolveToAbsolutePath(SearchPath.create("file.txt")))
                .isEqualTo(project.getLocation().append("file.txt"));
    }

    @Test
    public void additionalEnvironmentSearchPathsContainOnlyUniqueCorrectPathsWithResolvedEnvironmentVariables()
            throws Exception {
        final SystemVariableAccessor variableAccessor = mock(SystemVariableAccessor.class);
        when(variableAccessor.getValue("KNOWN_1")).thenReturn(Optional.of("java"));
        when(variableAccessor.getValue("KNOWN_2")).thenReturn(Optional.of("python"));
        when(variableAccessor.getValue("JAR")).thenReturn(Optional.of("lib.jar"));
        when(variableAccessor.getValue("FOLDER")).thenReturn(Optional.of("folder"));
        when(variableAccessor.getPaths("CLASSPATH"))
                .thenReturn(newArrayList("FirstClassPath.jar", "SecondClassPath.jar"));

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
        when(variableAccessor.getPaths("CLASSPATH"))
                .thenReturn(newArrayList("FirstClassPath.jar", "SecondClassPath.jar"));

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
                ReferencedLibrary.create(LibraryType.JAVA, "JavaLib1", project.getName() + "/lib1.jar"));
        projectConfig.addReferencedLibrary(
                ReferencedLibrary.create(LibraryType.JAVA, "JavaLib2", project.getName() + "/lib2.jar"));
        projectConfig.addReferencedLibrary(
                ReferencedLibrary.create(LibraryType.PYTHON, "PyLib1", project.getName() + "/folder1/PyLib1.py"));
        projectConfig.addReferencedLibrary(
                ReferencedLibrary.create(LibraryType.PYTHON, "PyLib2",
                        project.getName() + "/folder2/PyLib2/__init__.py"));

        final RedEclipseProjectConfig redConfig = new RedEclipseProjectConfig(project, projectConfig, variableAccessor);

        assertThat(redConfig.createExecutionEnvironmentSearchPaths()).isEqualTo(new EnvironmentSearchPaths(
                newArrayList(".", absolutePath("lib1.jar"), absolutePath("lib2.jar"), absolutePath("java", "lib.jar"),
                        "FirstClassPath.jar", "SecondClassPath.jar"),
                newArrayList(absolutePath("folder1"), absolutePath("folder2"), absolutePath("python", "folder"))));
    }

    private static String absolutePath(final String... projectRelativeParts) {
        final String projectAbsPath = project.getLocation().toOSString();
        return projectAbsPath + File.separator + String.join(File.separator, projectRelativeParts);
    }
}

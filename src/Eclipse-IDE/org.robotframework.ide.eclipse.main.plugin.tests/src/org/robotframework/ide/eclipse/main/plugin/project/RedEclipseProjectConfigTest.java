/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.rf.ide.core.SystemVariableAccessor;
import org.rf.ide.core.project.RobotProjectConfig;
import org.rf.ide.core.project.RobotProjectConfig.RelativeTo;
import org.rf.ide.core.project.RobotProjectConfig.RelativityPoint;
import org.rf.ide.core.project.RobotProjectConfig.SearchPath;
import org.robotframework.red.junit.ProjectProvider;

public class RedEclipseProjectConfigTest {

    @ClassRule
    public static ProjectProvider projectProvider = new ProjectProvider(RedEclipseProjectConfigTest.class);

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
        final SearchPath toResolve = SearchPath.create(project.getLocation().append("file.txt").toOSString());

        assertThat(redConfig.toAbsolutePath(toResolve)).hasValue(project.getLocation().append("file.txt").toFile());
    }

    @Test
    public void workspaceRelativePathIsResolved_forExistingResource() throws Exception {
        final RobotProjectConfig projectConfig = new RobotProjectConfig();
        projectConfig.setRelativityPoint(RelativityPoint.create(RelativeTo.WORKSPACE));

        final RedEclipseProjectConfig redConfig = new RedEclipseProjectConfig(project, projectConfig);
        final SearchPath toResolve = SearchPath.create("resource.txt");

        assertThat(redConfig.toAbsolutePath(toResolve))
                .hasValue(project.getWorkspace().getRoot().getLocation().append("resource.txt").toFile());
    }

    @Test
    public void projectRelativePathIsResolved_forExistingResource() throws Exception {
        final RobotProjectConfig projectConfig = new RobotProjectConfig();
        projectConfig.setRelativityPoint(RelativityPoint.create(RelativeTo.PROJECT));

        final RedEclipseProjectConfig redConfig = new RedEclipseProjectConfig(project, projectConfig);
        final SearchPath toResolve = SearchPath.create("resource.txt");

        assertThat(redConfig.toAbsolutePath(toResolve)).hasValue(project.getLocation().append("resource.txt").toFile());
    }

    @Test
    public void workspaceRelativePathIsResolved_forNonExistingResource() throws Exception {
        final RobotProjectConfig projectConfig = new RobotProjectConfig();
        projectConfig.setRelativityPoint(RelativityPoint.create(RelativeTo.WORKSPACE));

        final RedEclipseProjectConfig redConfig = new RedEclipseProjectConfig(project, projectConfig);
        final SearchPath toResolve = SearchPath.create("file.txt");

        assertThat(redConfig.toAbsolutePath(toResolve))
                .hasValue(project.getWorkspace().getRoot().getLocation().append("file.txt").toFile());
    }

    @Test
    public void projectRelativePathIsResolved_forNonExistingResource() throws Exception {
        final RobotProjectConfig projectConfig = new RobotProjectConfig();
        projectConfig.setRelativityPoint(RelativityPoint.create(RelativeTo.PROJECT));

        final RedEclipseProjectConfig redConfig = new RedEclipseProjectConfig(project, projectConfig);
        final SearchPath toResolve = SearchPath.create("file.txt");

        assertThat(redConfig.toAbsolutePath(toResolve)).hasValue(project.getLocation().append("file.txt").toFile());
    }

    @Test
    public void pathWithSystemVariableIsResolved_whenContainsSingleSystemVariable() throws Exception {
        final RobotProjectConfig projectConfig = new RobotProjectConfig();
        projectConfig.setRelativityPoint(RelativityPoint.create(RelativeTo.PROJECT));
        final SystemVariableAccessor variableAccessor = mock(SystemVariableAccessor.class);
        when(variableAccessor.getValue("var"))
                .thenReturn(Optional.of(project.getLocation().append("resource.txt").toOSString()));

        final RedEclipseProjectConfig redConfig = new RedEclipseProjectConfig(project, projectConfig, variableAccessor);
        final SearchPath toResolve = SearchPath.create("%{var}");

        assertThat(redConfig.toAbsolutePath(toResolve)).hasValue(project.getLocation().append("resource.txt").toFile());
    }

    @Test
    public void pathWithSystemVariableIsResolved_whenContainsSeveralSystemVariables() throws Exception {
        final RobotProjectConfig projectConfig = new RobotProjectConfig();
        projectConfig.setRelativityPoint(RelativityPoint.create(RelativeTo.PROJECT));
        final SystemVariableAccessor variableAccessor = mock(SystemVariableAccessor.class);
        when(variableAccessor.getValue("var1")).thenReturn(Optional.of("a"));
        when(variableAccessor.getValue("var2")).thenReturn(Optional.of("b"));
        when(variableAccessor.getValue("var3")).thenReturn(Optional.of("c"));

        final RedEclipseProjectConfig redConfig = new RedEclipseProjectConfig(project, projectConfig, variableAccessor);
        final SearchPath toResolve = SearchPath.create("%{var1}/%{var2}/%{var3}/file.txt");

        assertThat(redConfig.toAbsolutePath(toResolve))
                .hasValue(project.getLocation().append("a/b/c/file.txt").toFile());
    }

    @Test
    public void pathWithSystemVariableIsNotResolved_whenContainsUnknownSystemVariable() throws Exception {
        final RobotProjectConfig projectConfig = new RobotProjectConfig();
        projectConfig.setRelativityPoint(RelativityPoint.create(RelativeTo.PROJECT));
        final SystemVariableAccessor variableAccessor = mock(SystemVariableAccessor.class);
        when(variableAccessor.getValue("known1")).thenReturn(Optional.of("a"));
        when(variableAccessor.getValue("known2")).thenReturn(Optional.of("b"));
        when(variableAccessor.getValue("unknown1")).thenReturn(Optional.empty());
        when(variableAccessor.getValue("unknown2")).thenReturn(Optional.empty());

        final RedEclipseProjectConfig redConfig = new RedEclipseProjectConfig(project, projectConfig, variableAccessor);
        final SearchPath toResolve = SearchPath.create("%{known1}/%{known2}/%{unknown1}/%{unknown2}/file.txt");

        assertThat(redConfig.toAbsolutePath(toResolve)).isNotPresent();
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
}

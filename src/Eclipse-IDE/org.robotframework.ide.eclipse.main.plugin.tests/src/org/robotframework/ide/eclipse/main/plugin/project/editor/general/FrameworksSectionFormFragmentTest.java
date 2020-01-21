/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.editor.general;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.robotframework.red.junit.jupiter.ProjectExtension.getDir;

import java.io.File;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.rf.ide.core.environment.IRuntimeEnvironment;
import org.rf.ide.core.environment.InvalidPythonRuntimeEnvironment;
import org.rf.ide.core.environment.MissingRobotRuntimeEnvironment;
import org.rf.ide.core.environment.NullRuntimeEnvironment;
import org.rf.ide.core.environment.PythonInstallationDirectoryFinder.PythonInstallationDirectory;
import org.rf.ide.core.environment.RobotRuntimeEnvironment;
import org.robotframework.red.junit.jupiter.Project;
import org.robotframework.red.junit.jupiter.ProjectExtension;
import org.robotframework.red.junit.jupiter.RedTempDirectory;

@ExtendWith({ ProjectExtension.class, RedTempDirectory.class })
public class FrameworksSectionFormFragmentTest {

    @Project(dirs = { "workspaceFolder" })
    static IProject project;

    @TempDir
    static File tempFolder;

    private static File nonWorkspaceFolder;

    private static IFolder workspaceFolder;

    @BeforeAll
    public static void setup() throws Exception {
        nonWorkspaceFolder = RedTempDirectory.createNewDir(tempFolder, "nonWorkspaceFolder");
        workspaceFolder = getDir(project, "workspaceFolder");
    }

    @Test
    public void activeFrameworkTextIsCreated_whenActiveRuntimeEnvironmentIsNullEnvironment() throws Exception {
        final IRuntimeEnvironment environment = new NullRuntimeEnvironment();
        assertThat(FrameworksSectionFormFragment.createActiveFrameworkText(environment, true)).isEqualTo(
                "<form><p><img href=\"image\"/> &lt;unknown&gt; (<a href=\"preferences\">from Preferences</a>)</p></form>");
        assertThat(FrameworksSectionFormFragment.createActiveFrameworkText(environment, false))
                .isEqualTo("<form><p><img href=\"image\"/> &lt;unknown&gt;</p></form>");
    }

    @Test
    public void activeFrameworkTextIsCreated_whenActiveRuntimeEnvironmentIsInvalidPythonEnvironmentFromOutOfWorkspace()
            throws Exception {
        final IRuntimeEnvironment environment = new InvalidPythonRuntimeEnvironment(nonWorkspaceFolder);

        final String path = nonWorkspaceFolder.getAbsolutePath();
        assertThat(FrameworksSectionFormFragment.createActiveFrameworkText(environment, true))
                .isEqualTo("<form><p><img href=\"image\"/> <a href=\"systemPath\">" + path
                        + "</a> &lt;unknown&gt; (<a href=\"preferences\">from Preferences</a>)</p></form>");
        assertThat(FrameworksSectionFormFragment.createActiveFrameworkText(environment, false)).isEqualTo(
                "<form><p><img href=\"image\"/> <a href=\"systemPath\">" + path + "</a> &lt;unknown&gt;</p></form>");
    }

    @Test
    public void activeFrameworkTextIsCreated_whenActiveRuntimeEnvironmentIsInvalidPythonEnvironmentFromWorkspace()
            throws Exception {
        final IRuntimeEnvironment environment = new InvalidPythonRuntimeEnvironment(
                workspaceFolder.getLocation().toFile());

        final String path = workspaceFolder.getLocation()
                .makeRelativeTo(ResourcesPlugin.getWorkspace().getRoot().getLocation())
                .toOSString();
        assertThat(FrameworksSectionFormFragment.createActiveFrameworkText(environment, true))
                .isEqualTo("<form><p><img href=\"image\"/> <a href=\"systemPath\">" + path
                        + "</a> &lt;unknown&gt; (<a href=\"preferences\">from Preferences</a>)</p></form>");
        assertThat(FrameworksSectionFormFragment.createActiveFrameworkText(environment, false)).isEqualTo(
                "<form><p><img href=\"image\"/> <a href=\"systemPath\">" + path + "</a> &lt;unknown&gt;</p></form>");
    }

    @Test
    public void activeFrameworkTextIsCreated_whenActiveRuntimeEnvironmentIsInvalidRobotEnvironmentFromOutOfWorkspace()
            throws Exception {
        final PythonInstallationDirectory pythonInstallation = mock(PythonInstallationDirectory.class);
        when(pythonInstallation.getAbsolutePath()).thenReturn(nonWorkspaceFolder.getAbsolutePath());
        final IRuntimeEnvironment environment = new MissingRobotRuntimeEnvironment(pythonInstallation);

        final String path = nonWorkspaceFolder.getAbsolutePath();
        assertThat(FrameworksSectionFormFragment.createActiveFrameworkText(environment, true))
                .isEqualTo("<form><p><img href=\"image\"/> <a href=\"systemPath\">" + path
                        + "</a> &lt;unknown&gt; (<a href=\"preferences\">from Preferences</a>)</p></form>");
        assertThat(FrameworksSectionFormFragment.createActiveFrameworkText(environment, false)).isEqualTo(
                "<form><p><img href=\"image\"/> <a href=\"systemPath\">" + path + "</a> &lt;unknown&gt;</p></form>");
    }

    @Test
    public void activeFrameworkTextIsCreated_whenActiveRuntimeEnvironmentIsInvalidRobotEnvironmentFromWorkspace()
            throws Exception {
        final PythonInstallationDirectory pythonInstallation = mock(PythonInstallationDirectory.class);
        when(pythonInstallation.getAbsolutePath()).thenReturn(workspaceFolder.getLocation().toFile().getAbsolutePath());
        final IRuntimeEnvironment environment = new MissingRobotRuntimeEnvironment(pythonInstallation);

        final String path = workspaceFolder.getLocation()
                .makeRelativeTo(ResourcesPlugin.getWorkspace().getRoot().getLocation())
                .toOSString();
        assertThat(FrameworksSectionFormFragment.createActiveFrameworkText(environment, true))
                .isEqualTo("<form><p><img href=\"image\"/> <a href=\"systemPath\">" + path
                        + "</a> &lt;unknown&gt; (<a href=\"preferences\">from Preferences</a>)</p></form>");
        assertThat(FrameworksSectionFormFragment.createActiveFrameworkText(environment, false)).isEqualTo(
                "<form><p><img href=\"image\"/> <a href=\"systemPath\">" + path + "</a> &lt;unknown&gt;</p></form>");
    }

    @Test
    public void activeFrameworkTextIsCreated_whenActiveRuntimeEnvironmentIsValidRobotEnvironmentFromOutOfWorkspace()
            throws Exception {
        final PythonInstallationDirectory pythonInstallation = mock(PythonInstallationDirectory.class);
        when(pythonInstallation.getAbsolutePath()).thenReturn(nonWorkspaceFolder.getAbsolutePath());
        final IRuntimeEnvironment environment = new RobotRuntimeEnvironment(pythonInstallation, "RF 1.2.3");

        final String path = nonWorkspaceFolder.getAbsolutePath();
        assertThat(FrameworksSectionFormFragment.createActiveFrameworkText(environment, true))
                .isEqualTo("<form><p><img href=\"image\"/> <a href=\"systemPath\">" + path
                        + "</a> RF 1.2.3 (<a href=\"preferences\">from Preferences</a>)</p></form>");
        assertThat(FrameworksSectionFormFragment.createActiveFrameworkText(environment, false)).isEqualTo(
                "<form><p><img href=\"image\"/> <a href=\"systemPath\">" + path + "</a> RF 1.2.3</p></form>");
    }

    @Test
    public void activeFrameworkTextIsCreated_whenActiveRuntimeEnvironmentIsValidRobotEnvironmentFromWorkspace()
            throws Exception {
        final PythonInstallationDirectory pythonInstallation = mock(PythonInstallationDirectory.class);
        when(pythonInstallation.getAbsolutePath()).thenReturn(workspaceFolder.getLocation().toFile().getAbsolutePath());
        final IRuntimeEnvironment environment = new RobotRuntimeEnvironment(pythonInstallation, "RF 1.2.3");

        final String path = workspaceFolder.getLocation()
                .makeRelativeTo(ResourcesPlugin.getWorkspace().getRoot().getLocation())
                .toOSString();
        assertThat(FrameworksSectionFormFragment.createActiveFrameworkText(environment, true))
                .isEqualTo("<form><p><img href=\"image\"/> <a href=\"systemPath\">" + path
                        + "</a> RF 1.2.3 (<a href=\"preferences\">from Preferences</a>)</p></form>");
        assertThat(FrameworksSectionFormFragment.createActiveFrameworkText(environment, false)).isEqualTo(
                "<form><p><img href=\"image\"/> <a href=\"systemPath\">" + path + "</a> RF 1.2.3</p></form>");
    }
}

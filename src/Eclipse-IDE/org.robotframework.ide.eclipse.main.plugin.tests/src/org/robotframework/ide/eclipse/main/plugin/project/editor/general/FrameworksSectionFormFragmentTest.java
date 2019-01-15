/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.editor.general;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.rf.ide.core.environment.InvalidPythonRuntimeEnvironment;
import org.rf.ide.core.environment.MissingRobotRuntimeEnvironment;
import org.rf.ide.core.environment.NullRuntimeEnvironment;
import org.rf.ide.core.environment.RobotRuntimeEnvironment;
import org.robotframework.red.junit.ProjectProvider;

public class FrameworksSectionFormFragmentTest {

    @ClassRule
    public static ProjectProvider projectProvider = new ProjectProvider(FrameworksSectionFormFragmentTest.class);

    @ClassRule
    public static TemporaryFolder temporaryFolder = new TemporaryFolder();

    private static File NON_WORKSPACE_DIR;

    private static IFolder WORKSPACE_DIR;

    @BeforeClass
    public static void setup() throws Exception {
        NON_WORKSPACE_DIR = temporaryFolder.newFolder("non_workspace_dir");
        WORKSPACE_DIR = projectProvider.createDir("workspace_dir");
    }

    @Test
    public void activeFrameworkTextIsCreated_whenActiveRuntimeEnvironmentIsNullEnvironment() throws Exception {
        final RobotRuntimeEnvironment environment = new NullRuntimeEnvironment();
        assertThat(FrameworksSectionFormFragment.createActiveFrameworkText(environment, true)).isEqualTo(
                "<form><p><img href=\"image\"/> &lt;unknown&gt; (<a href=\"preferences\">from Preferences</a>)</p></form>");
        assertThat(FrameworksSectionFormFragment.createActiveFrameworkText(environment, false))
                .isEqualTo("<form><p><img href=\"image\"/> &lt;unknown&gt;</p></form>");
    }

    @Test
    public void activeFrameworkTextIsCreated_whenActiveRuntimeEnvironmentIsInvalidPythonEnvironmentFromOutOfWorkspace()
            throws Exception {
        final RobotRuntimeEnvironment environment = new InvalidPythonRuntimeEnvironment(NON_WORKSPACE_DIR);

        final String path = NON_WORKSPACE_DIR.getAbsolutePath();
        assertThat(FrameworksSectionFormFragment.createActiveFrameworkText(environment, true))
                .isEqualTo("<form><p><img href=\"image\"/> <a href=\"systemPath\">" + path
                        + "</a> &lt;unknown&gt; (<a href=\"preferences\">from Preferences</a>)</p></form>");
        assertThat(FrameworksSectionFormFragment.createActiveFrameworkText(environment, false)).isEqualTo(
                "<form><p><img href=\"image\"/> <a href=\"systemPath\">" + path + "</a> &lt;unknown&gt;</p></form>");
    }

    @Test
    public void activeFrameworkTextIsCreated_whenActiveRuntimeEnvironmentIsInvalidPythonEnvironmentFromWorkspace()
            throws Exception {
        final RobotRuntimeEnvironment environment = new InvalidPythonRuntimeEnvironment(
                WORKSPACE_DIR.getLocation().toFile());

        final String path = WORKSPACE_DIR.getLocation()
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
        final RobotRuntimeEnvironment environment = new MissingRobotRuntimeEnvironment(NON_WORKSPACE_DIR);

        final String path = NON_WORKSPACE_DIR.getAbsolutePath();
        assertThat(FrameworksSectionFormFragment.createActiveFrameworkText(environment, true))
                .isEqualTo("<form><p><img href=\"image\"/> <a href=\"systemPath\">" + path
                        + "</a> &lt;unknown&gt; (<a href=\"preferences\">from Preferences</a>)</p></form>");
        assertThat(FrameworksSectionFormFragment.createActiveFrameworkText(environment, false)).isEqualTo(
                "<form><p><img href=\"image\"/> <a href=\"systemPath\">" + path + "</a> &lt;unknown&gt;</p></form>");
    }

    @Test
    public void activeFrameworkTextIsCreated_whenActiveRuntimeEnvironmentIsInvalidRobotEnvironmentFromWorkspace()
            throws Exception {
        final RobotRuntimeEnvironment environment = new MissingRobotRuntimeEnvironment(
                WORKSPACE_DIR.getLocation().toFile());

        final String path = WORKSPACE_DIR.getLocation()
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
        final RobotRuntimeEnvironment environment = new RobotRuntimeEnvironment(NON_WORKSPACE_DIR, "RF 1.2.3");

        final String path = NON_WORKSPACE_DIR.getAbsolutePath();
        assertThat(FrameworksSectionFormFragment.createActiveFrameworkText(environment, true))
                .isEqualTo("<form><p><img href=\"image\"/> <a href=\"systemPath\">" + path
                        + "</a> RF 1.2.3 (<a href=\"preferences\">from Preferences</a>)</p></form>");
        assertThat(FrameworksSectionFormFragment.createActiveFrameworkText(environment, false)).isEqualTo(
                "<form><p><img href=\"image\"/> <a href=\"systemPath\">" + path + "</a> RF 1.2.3</p></form>");
    }

    @Test
    public void activeFrameworkTextIsCreated_whenActiveRuntimeEnvironmentIsValidRobotEnvironmentFromWorkspace()
            throws Exception {
        final RobotRuntimeEnvironment environment = new RobotRuntimeEnvironment(WORKSPACE_DIR.getLocation().toFile(),
                "RF 1.2.3");

        final String path = WORKSPACE_DIR.getLocation()
                .makeRelativeTo(ResourcesPlugin.getWorkspace().getRoot().getLocation())
                .toOSString();
        assertThat(FrameworksSectionFormFragment.createActiveFrameworkText(environment, true))
                .isEqualTo("<form><p><img href=\"image\"/> <a href=\"systemPath\">" + path
                        + "</a> RF 1.2.3 (<a href=\"preferences\">from Preferences</a>)</p></form>");
        assertThat(FrameworksSectionFormFragment.createActiveFrameworkText(environment, false)).isEqualTo(
                "<form><p><img href=\"image\"/> <a href=\"systemPath\">" + path + "</a> RF 1.2.3</p></form>");
    }
}

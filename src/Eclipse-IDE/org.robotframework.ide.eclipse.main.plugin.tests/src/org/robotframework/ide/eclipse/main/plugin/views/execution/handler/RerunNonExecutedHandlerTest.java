/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.views.execution.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.io.File;
import java.net.URI;
import java.util.Optional;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.rf.ide.core.execution.agent.Status;
import org.robotframework.ide.eclipse.main.plugin.launch.RobotTestExecutionService.RobotTestsLaunch;
import org.robotframework.ide.eclipse.main.plugin.launch.local.RobotLaunchConfiguration;
import org.robotframework.ide.eclipse.main.plugin.views.execution.ExecutionStatusStore;
import org.robotframework.ide.eclipse.main.plugin.views.execution.ExecutionTreeNode;
import org.robotframework.red.junit.jupiter.LaunchConfig;
import org.robotframework.red.junit.jupiter.LaunchConfigExtension;
import org.robotframework.red.junit.jupiter.Project;
import org.robotframework.red.junit.jupiter.ProjectExtension;
import org.robotframework.red.junit.jupiter.RedTempDirectory;
import org.robotframework.red.junit.jupiter.StatefulProject;

@ExtendWith({ ProjectExtension.class, RedTempDirectory.class, LaunchConfigExtension.class })
public class RerunNonExecutedHandlerTest {

    @Project(cleanUpAfterEach = true)
    static StatefulProject project;

    @LaunchConfig(typeId = RobotLaunchConfiguration.TYPE_ID, name = "robot")
    ILaunchConfiguration launchCfg;

    @TempDir
    public File tempFolder;

    @Test
    public void coreExceptionIsThrown_whenConfigurationIsNull() throws Exception {
        final ILaunchConfigurationWorkingCopy configuration = null;

        final RobotTestsLaunch launch = new RobotTestsLaunch(configuration);

        assertThatExceptionOfType(CoreException.class)
                .isThrownBy(() -> RerunNonExecutedHandler.E4ShowNonExecutedOnlyHandler.getConfig(launch))
                .withMessage("Launch configuration does not exist")
                .withNoCause();
    }

    @Test
    public void coreExceptionIsThrown_whenConfigurationDoesNotExist() throws Exception {
        final ILaunchConfigurationWorkingCopy configuration = createConfigurationSpy();
        when(configuration.exists()).thenReturn(false);

        final RobotTestsLaunch launch = new RobotTestsLaunch(configuration);

        assertThatExceptionOfType(CoreException.class)
                .isThrownBy(() -> RerunNonExecutedHandler.E4ShowNonExecutedOnlyHandler.getConfig(launch))
                .withMessage("Launch configuration does not exist")
                .withNoCause();
    }

    @Test
    public void coreExceptionIsThrown_whenConfigurationExistsButNonExecutedTestsDoNotExist() throws Exception {
        final ILaunchConfigurationWorkingCopy configuration = createConfigurationSpy();
        when(configuration.exists()).thenReturn(true);

        final RobotTestsLaunch launch = spy(new RobotTestsLaunch(configuration));
        final RobotLaunchConfiguration robotConfig = new RobotLaunchConfiguration(configuration);
        robotConfig.setProjectName(project.getName());

        final ExecutionTreeNode root = ExecutionTreeNode.newSuiteNode(null, "root", null);
        final ExecutionTreeNode suite = ExecutionTreeNode.newSuiteNode(root, "suite", null);
        final ExecutionTreeNode test = ExecutionTreeNode.newTestNode(suite, "test", null);
        suite.setStatus(Status.PASS);
        suite.addChildren(test);
        root.addChildren(suite);

        final ExecutionStatusStore store = new ExecutionStatusStore();
        store.setExecutionTree(root);

        when(launch.getExecutionData(ExecutionStatusStore.class)).thenReturn(Optional.of(store));
        assertThatExceptionOfType(CoreException.class)
                .isThrownBy(() -> RerunNonExecutedHandler.E4ShowNonExecutedOnlyHandler.getConfig(launch))
                .withMessage("Non executed elements do not exist")
                .withNoCause();
    }

    @Test
    public void configurationForNonExecutedTestsRerunIsReturned_whenNonExecutedTestsExist() throws Exception {
        final ILaunchConfigurationWorkingCopy configuration = createConfigurationSpy();
        when(configuration.exists()).thenReturn(true);

        final RobotTestsLaunch launch = spy(new RobotTestsLaunch(configuration));
        final RobotLaunchConfiguration robotConfig = new RobotLaunchConfiguration(configuration);
        robotConfig.setProjectName(project.getName());
        project.createFile("suite.robot");

        final ExecutionTreeNode root = ExecutionTreeNode.newSuiteNode(null, "root", null);
        final ExecutionTreeNode suite = ExecutionTreeNode.newSuiteNode(root, "Suite",
                URI.create("file:///" + project.getLocation().toPortableString() + "/suite.robot"));
        final ExecutionTreeNode test = ExecutionTreeNode.newTestNode(suite, "test", suite.getPath());
        suite.addChildren(test);
        root.addChildren(suite);

        final ExecutionStatusStore store = new ExecutionStatusStore();
        store.setExecutionTree(root);

        when(launch.getExecutionData(ExecutionStatusStore.class)).thenReturn(Optional.of(store));
        assertThat(RerunNonExecutedHandler.E4ShowNonExecutedOnlyHandler.getConfig(launch).getAttributes())
                .containsEntry("Robot arguments", "-t RerunNonExecutedHandlerTest.Suite.test");
    }

    @Test
    public void configurationForNonExecutedTestsRerunIsReturned_whenNonExecutedTestsExistAndSuiteIsNested()
            throws Exception {
        final ILaunchConfigurationWorkingCopy configuration = createConfigurationSpy();
        when(configuration.exists()).thenReturn(true);

        final RobotTestsLaunch launch = spy(new RobotTestsLaunch(configuration));
        final RobotLaunchConfiguration robotConfig = new RobotLaunchConfiguration(configuration);
        robotConfig.setProjectName(project.getName());
        project.createFile("dir1");

        final ExecutionTreeNode root = ExecutionTreeNode.newSuiteNode(null, "root", null);
        final ExecutionTreeNode dir1 = ExecutionTreeNode.newSuiteNode(root, "Dir1",
                URI.create("file:///" + project.getLocation().toPortableString() + "/dir1"));
        final ExecutionTreeNode dir2 = ExecutionTreeNode.newSuiteNode(dir1, "Dir2",
                URI.create("file:///" + project.getLocation().toPortableString() + "/dir1/dir2"));
        final ExecutionTreeNode suite = ExecutionTreeNode.newSuiteNode(dir2, "Suite",
                URI.create("file:///" + project.getLocation().toPortableString() + "/suite.robot"));
        final ExecutionTreeNode test = ExecutionTreeNode.newTestNode(suite, "test", suite.getPath());
        suite.addChildren(test);
        dir2.addChildren(suite);
        dir1.addChildren(dir2);
        root.addChildren(dir1);

        final ExecutionStatusStore store = new ExecutionStatusStore();
        store.setExecutionTree(root);

        when(launch.getExecutionData(ExecutionStatusStore.class)).thenReturn(Optional.of(store));
        assertThat(RerunNonExecutedHandler.E4ShowNonExecutedOnlyHandler.getConfig(launch).getAttributes())
                .containsEntry("Robot arguments", "-t RerunNonExecutedHandlerTest.Dir1.Dir2.Suite.test");
    }

    @Test
    public void configurationForNonExecutedTestsRerunIsReturned_whenNonExecutedTestsExistAndSuiteIsLinked()
            throws Exception {
        final ILaunchConfigurationWorkingCopy configuration = createConfigurationSpy();
        when(configuration.exists()).thenReturn(true);

        final RobotTestsLaunch launch = spy(new RobotTestsLaunch(configuration));
        final RobotLaunchConfiguration robotConfig = new RobotLaunchConfiguration(configuration);
        robotConfig.setProjectName(project.getName());

        final File linkedNonWorkspaceDir = RedTempDirectory.createNewDir(tempFolder, "linked_dir");
        RedTempDirectory.createNewFile(linkedNonWorkspaceDir, "linked_suite.robot");
        project.createDirLink("linked_dir", linkedNonWorkspaceDir.toURI());
        project.getFile("linked_dir");

        final ExecutionTreeNode root = ExecutionTreeNode.newSuiteNode(null, "root", null);
        final ExecutionTreeNode dir = ExecutionTreeNode.newSuiteNode(root, "Linked Dir",
                URI.create(linkedNonWorkspaceDir.toURI().toString()));
        final ExecutionTreeNode suite = ExecutionTreeNode.newSuiteNode(dir, "Linked Suite",
                URI.create(linkedNonWorkspaceDir.toURI().toString() + "/linked_suite.robot"));
        final ExecutionTreeNode test = ExecutionTreeNode.newTestNode(suite, "test", suite.getPath());
        suite.addChildren(test);
        dir.addChildren(suite);
        root.addChildren(dir);

        final ExecutionStatusStore store = new ExecutionStatusStore();
        store.setExecutionTree(root);

        when(launch.getExecutionData(ExecutionStatusStore.class)).thenReturn(Optional.of(store));
        assertThat(RerunNonExecutedHandler.E4ShowNonExecutedOnlyHandler.getConfig(launch).getAttributes())
                .containsEntry("Robot arguments",
                        "-t \"RerunNonExecutedHandlerTest & Linked Dir.Linked Dir.Linked Suite.test\"");
    }

    @Test
    public void configurationForNonExecutedTestRerunIsReturned_whenNonExecutedTestsExistAndCustomRobotArgumentsAreSet()
            throws Exception {
        final ILaunchConfigurationWorkingCopy configuration = createConfigurationSpy();
        when(configuration.exists()).thenReturn(true);
        configuration.setAttribute("Robot arguments", "-a a -b b -c c");

        final RobotTestsLaunch launch = spy(new RobotTestsLaunch(configuration));
        final RobotLaunchConfiguration robotConfig = new RobotLaunchConfiguration(configuration);
        robotConfig.setProjectName(project.getName());
        project.createFile("suite.robot");

        final ExecutionTreeNode root = ExecutionTreeNode.newSuiteNode(null, "root", null);
        final ExecutionTreeNode suite = ExecutionTreeNode.newSuiteNode(root, "Suite",
                URI.create("file:///" + project.getLocation().toPortableString() + "/suite.robot"));
        final ExecutionTreeNode test = ExecutionTreeNode.newTestNode(suite, "test", suite.getPath());
        suite.addChildren(test);
        root.addChildren(suite);

        final ExecutionStatusStore store = new ExecutionStatusStore();
        store.setExecutionTree(root);

        when(launch.getExecutionData(ExecutionStatusStore.class)).thenReturn(Optional.of(store));
        assertThat(RerunNonExecutedHandler.E4ShowNonExecutedOnlyHandler.getConfig(launch).getAttributes())
                .containsEntry("Robot arguments",
                        "-a a -b b -c c -t RerunNonExecutedHandlerTest.Suite.test");
    }

    private ILaunchConfigurationWorkingCopy createConfigurationSpy() throws CoreException {
        return spy(launchCfg.getWorkingCopy());
    }
}

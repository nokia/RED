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

import com.google.common.collect.ImmutableMap;

@ExtendWith({ ProjectExtension.class, RedTempDirectory.class, LaunchConfigExtension.class })
public class RerunNonExecutedHandlerTest {

    @Project(dirs = { "dir", "dir/dir" }, cleanUpAfterEach = true)
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
                .withMessage("Non executed tests do not exist")
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

        final ExecutionStatusStore store = createExecutionStatusStore("/suite.robot");

        when(launch.getExecutionData(ExecutionStatusStore.class)).thenReturn(Optional.of(store));
        assertThat(RerunNonExecutedHandler.E4ShowNonExecutedOnlyHandler.getConfig(launch).getAttributes())
                .containsEntry("Test suites", ImmutableMap.of("suite.robot", "test"));
    }

    @Test
    public void configurationForNonExecutedTestsRerunIsReturned_whenNonExecutedTestsExistAndSuiteIsNested()
            throws Exception {
        final ILaunchConfigurationWorkingCopy configuration = createConfigurationSpy();
        when(configuration.exists()).thenReturn(true);

        final RobotTestsLaunch launch = spy(new RobotTestsLaunch(configuration));
        final RobotLaunchConfiguration robotConfig = new RobotLaunchConfiguration(configuration);
        robotConfig.setProjectName(project.getName());
        project.createFile("dir/dir/suite.robot");

        final ExecutionStatusStore store = createExecutionStatusStore("/dir/dir/suite.robot");

        when(launch.getExecutionData(ExecutionStatusStore.class)).thenReturn(Optional.of(store));
        assertThat(RerunNonExecutedHandler.E4ShowNonExecutedOnlyHandler.getConfig(launch).getAttributes())
                .containsEntry("Test suites", ImmutableMap.of("dir/dir/suite.robot", "test"));
    }

    @Test
    public void configurationForNonExecutedTestsRerunIsReturned_whenNonExecutedTestsExistAndSuiteIsLinked()
            throws Exception {
        final ILaunchConfigurationWorkingCopy configuration = createConfigurationSpy();
        when(configuration.exists()).thenReturn(true);

        final RobotTestsLaunch launch = spy(new RobotTestsLaunch(configuration));
        final RobotLaunchConfiguration robotConfig = new RobotLaunchConfiguration(configuration);
        robotConfig.setProjectName(project.getName());

        final File linkedNonWorkspaceFile = RedTempDirectory.createNewFile(tempFolder, "non_workspace_test.robot");
        project.createFileLink("linkedSuite.robot", linkedNonWorkspaceFile.toURI());

        final ExecutionStatusStore store = createExecutionStatusStore("/linkedSuite.robot");

        when(launch.getExecutionData(ExecutionStatusStore.class)).thenReturn(Optional.of(store));
        assertThat(RerunNonExecutedHandler.E4ShowNonExecutedOnlyHandler.getConfig(launch).getAttributes())
                .containsEntry("Test suites", ImmutableMap.of("linkedSuite.robot", "test"));
    }

    @Test
    public void configurationForNonExecutedTestsRerunIsReturned_whenNonExecutedTestsExistAndSuiteIsLinkedAndNasted()
            throws Exception {
        final ILaunchConfigurationWorkingCopy configuration = createConfigurationSpy();
        when(configuration.exists()).thenReturn(true);

        final RobotTestsLaunch launch = spy(new RobotTestsLaunch(configuration));
        final RobotLaunchConfiguration robotConfig = new RobotLaunchConfiguration(configuration);
        robotConfig.setProjectName(project.getName());

        final File linkedNonWorkspaceFile = RedTempDirectory.createNewFile(tempFolder, "non_workspace_test.robot");
        project.createFileLink("dir/linkedSuite.robot", linkedNonWorkspaceFile.toURI());

        final ExecutionStatusStore store = createExecutionStatusStore("/dir/linkedSuite.robot");

        when(launch.getExecutionData(ExecutionStatusStore.class)).thenReturn(Optional.of(store));
        assertThat(RerunNonExecutedHandler.E4ShowNonExecutedOnlyHandler.getConfig(launch).getAttributes())
                .containsEntry("Test suites", ImmutableMap.of("dir/linkedSuite.robot", "test"));
    }

    @Test
    public void configurationForNonExecutedTestRerunIsReturned_whenNonExecutedTestsExistAndCustomRobotArgumentsAreSet()
            throws Exception {
        final ILaunchConfigurationWorkingCopy configuration = createConfigurationSpy();
        when(configuration.exists()).thenReturn(true);
        configuration.setAttribute("Robot arguments", "-a -b -c");

        final RobotTestsLaunch launch = spy(new RobotTestsLaunch(configuration));
        final RobotLaunchConfiguration robotConfig = new RobotLaunchConfiguration(configuration);
        robotConfig.setProjectName(project.getName());
        project.createFile("suite.robot");

        final ExecutionStatusStore store = createExecutionStatusStore("/suite.robot");

        when(launch.getExecutionData(ExecutionStatusStore.class)).thenReturn(Optional.of(store));
        assertThat(RerunNonExecutedHandler.E4ShowNonExecutedOnlyHandler.getConfig(launch).getAttributes())
                .containsEntry("Robot arguments", "-a -b -c")
                .containsEntry("Test suites", ImmutableMap.of("suite.robot", "test"));
    }

    private ILaunchConfigurationWorkingCopy createConfigurationSpy() throws CoreException {
        return spy(launchCfg.getWorkingCopy());
    }

    private ExecutionStatusStore createExecutionStatusStore(final String suitePath) {
        final ExecutionTreeNode root = ExecutionTreeNode.newSuiteNode(null, "root", null);
        final ExecutionTreeNode suite = ExecutionTreeNode.newSuiteNode(root, "suite",
                URI.create("file:///" + project.getLocation().toPortableString() + suitePath));
        final ExecutionTreeNode test = ExecutionTreeNode.newTestNode(suite, "test", suite.getPath());
        suite.addChildren(test);
        root.addChildren(suite);

        final ExecutionStatusStore store = new ExecutionStatusStore();
        store.setExecutionTree(root);
        return store;
    }
}

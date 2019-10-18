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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.rf.ide.core.execution.agent.Status;
import org.robotframework.ide.eclipse.main.plugin.launch.RobotTestExecutionService.RobotTestsLaunch;
import org.robotframework.ide.eclipse.main.plugin.launch.local.RobotLaunchConfiguration;
import org.robotframework.ide.eclipse.main.plugin.views.execution.ExecutionStatusStore;
import org.robotframework.ide.eclipse.main.plugin.views.execution.ExecutionTreeNode;
import org.robotframework.red.junit.ProjectProvider;
import org.robotframework.red.junit.ResourceCreator;
import org.robotframework.red.junit.RunConfigurationProvider;

import com.google.common.collect.ImmutableMap;
public class RerunNonExecutedHandlerTest {

    private static final String PROJECT_NAME = RerunNonExecutedHandlerTest.class.getSimpleName();

    @ClassRule
    public static ProjectProvider projectProvider = new ProjectProvider(PROJECT_NAME);

    @Rule
    public RunConfigurationProvider robotRunConfigurationProvider = new RunConfigurationProvider(
            RobotLaunchConfiguration.TYPE_ID);

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Rule
    public ResourceCreator resourceCreator = new ResourceCreator();

    @BeforeClass
    public static void beforeSuite() throws Exception {
        projectProvider.createDir("dir");
        projectProvider.createDir("dir/dir");
    }

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
        robotConfig.setProjectName(projectProvider.getProject().getName());

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
        robotConfig.setProjectName(projectProvider.getProject().getName());
        projectProvider.createFile("suite.robot");

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
        robotConfig.setProjectName(projectProvider.getProject().getName());
        projectProvider.createFile("dir/dir/suite.robot");

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
        robotConfig.setProjectName(projectProvider.getProject().getName());

        final File linkedNonWorkspaceFile = tempFolder.newFile("non_workspace_test.robot");
        final IFile linkedSuite = projectProvider.getFile("linkedSuite.robot");
        resourceCreator.createLink(linkedNonWorkspaceFile.toURI(), linkedSuite);

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
        robotConfig.setProjectName(projectProvider.getProject().getName());

        final File linkedNonWorkspaceFile = tempFolder.newFile("non_workspace_test.robot");
        final IFile linkedSuite = projectProvider.getFile("dir/linkedSuite.robot");
        resourceCreator.createLink(linkedNonWorkspaceFile.toURI(), linkedSuite);

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
        robotConfig.setProjectName(projectProvider.getProject().getName());
        projectProvider.createFile("suite.robot");

        final ExecutionStatusStore store = createExecutionStatusStore("/suite.robot");

        when(launch.getExecutionData(ExecutionStatusStore.class)).thenReturn(Optional.of(store));
        assertThat(RerunNonExecutedHandler.E4ShowNonExecutedOnlyHandler.getConfig(launch).getAttributes())
                .containsEntry("Robot arguments", "-a -b -c")
                .containsEntry("Test suites", ImmutableMap.of("suite.robot", "test"));
    }

    private ILaunchConfigurationWorkingCopy createConfigurationSpy() throws CoreException {
        return spy(robotRunConfigurationProvider.create("robot").getWorkingCopy());
    }

    private ExecutionStatusStore createExecutionStatusStore(final String suitePath) {
        final ExecutionTreeNode root = ExecutionTreeNode.newSuiteNode(null, "root", null);
        final ExecutionTreeNode suite = ExecutionTreeNode.newSuiteNode(root, "suite",
                URI.create("file:///" + projectProvider.getProject().getLocation().toPortableString() + suitePath));
        final ExecutionTreeNode test = ExecutionTreeNode.newTestNode(suite, "test", suite.getPath());
        suite.addChildren(test);
        root.addChildren(suite);

        final ExecutionStatusStore store = new ExecutionStatusStore();
        store.setExecutionTree(root);
        return store;
    }
}

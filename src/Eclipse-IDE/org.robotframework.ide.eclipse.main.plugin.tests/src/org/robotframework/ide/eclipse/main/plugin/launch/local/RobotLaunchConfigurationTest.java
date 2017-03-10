/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.launch.local;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentMatchers;
import org.rf.ide.core.executor.SuiteExecutor;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.launch.IRobotLaunchConfiguration;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.red.junit.ProjectProvider;

import com.google.common.collect.ImmutableMap;

public class RobotLaunchConfigurationTest {

    private final static String PROJECT_NAME = RobotLaunchConfigurationTest.class.getSimpleName();

    private static final ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();

    private IProject project;

    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    @ClassRule
    public static ProjectProvider projectProvider = new ProjectProvider(PROJECT_NAME);

    @Before
    public void setup() throws CoreException {
        removeAllConfigurations();
        project = projectProvider.getProject();
    }

    @AfterClass
    public static void clean() throws CoreException {
        removeAllConfigurations();
    }

    private static void removeAllConfigurations() throws CoreException {
        final ILaunchConfigurationType type = manager.getLaunchConfigurationType(RobotLaunchConfiguration.TYPE_ID);
        final ILaunchConfiguration[] launchConfigs = manager.getLaunchConfigurations(type);
        for (final ILaunchConfiguration config : launchConfigs) {
            config.delete();
        }
    }

    @Test
    public void nullVariablesArrayIsReturned_whenThereAreNoVariablesDefined() throws Exception {
        // this mean that the process will inherit environment variables from parent process

        final ILaunchConfiguration config = mock(ILaunchConfiguration.class);
        when(config.getAttribute(eq(ILaunchManager.ATTR_ENVIRONMENT_VARIABLES),
                ArgumentMatchers.<Map<String, String>> isNull())).thenReturn(null);

        final RobotLaunchConfiguration robotConfig = new RobotLaunchConfiguration(config);

        assertThat(robotConfig.getEnvironmentVariables()).isNull();
    }

    @Test
    public void onlyVariablesFromConfigAreReturned_whenTheyAreDefinedAndOverrideIsEnabled() throws Exception {
        final Map<String, String> vars = ImmutableMap.of("VAR1", "x", "VAR2", "y", "VAR3", "z");

        final ILaunchConfiguration config = mock(ILaunchConfiguration.class);
        when(config.getAttribute(eq(ILaunchManager.ATTR_ENVIRONMENT_VARIABLES),
                ArgumentMatchers.<Map<String, String>> isNull())).thenReturn(vars);
        when(config.getAttribute(eq(ILaunchManager.ATTR_APPEND_ENVIRONMENT_VARIABLES), anyBoolean())).thenReturn(false);

        final RobotLaunchConfiguration robotConfig = new RobotLaunchConfiguration(config);

        assertThat(robotConfig.getEnvironmentVariables()).containsExactly("VAR1=x", "VAR2=y", "VAR3=z");
    }

    @Test
    public void inheritedVariablesFromCurrentProcessAndConfigVariablesAreReturned_whenTheyAreDefinedAndAppendingIsEnabled()
            throws Exception {
        final Map<String, String> vars = ImmutableMap.of("VAR1", "x", "VAR2", "y", "VAR3", "z");

        final ILaunchConfiguration config = mock(ILaunchConfiguration.class);
        when(config.getAttribute(eq(ILaunchManager.ATTR_ENVIRONMENT_VARIABLES),
                ArgumentMatchers.<Map<String, String>> isNull())).thenReturn(vars);
        when(config.getAttribute(eq(ILaunchManager.ATTR_APPEND_ENVIRONMENT_VARIABLES), anyBoolean())).thenReturn(true);

        final RobotLaunchConfiguration robotConfig = new RobotLaunchConfiguration(config);

        final String[] envVars = robotConfig.getEnvironmentVariables();
        assertThat(envVars.length).isGreaterThan(3);
        assertThat(envVars).containsSequence("VAR1=x", "VAR2=y", "VAR3=z");
    }

    @Test
    public void defaultConfigurationObtained_whenDefaultConfigurationIsCreated() throws CoreException {
        final RobotLaunchConfiguration robotConfig = getDefaultRobotLaunchConfiguration();

        assertThat(robotConfig.getName()).isEqualTo("Resource");
        assertThat(robotConfig.getProjectName()).isEqualTo(PROJECT_NAME);
        assertThat(robotConfig.getSuitePaths().keySet()).containsExactly("Resource");
        assertThat(robotConfig.getRobotArguments()).isEqualTo("");
        assertThat(robotConfig.isIncludeTagsEnabled()).isFalse();
        assertThat(robotConfig.isExcludeTagsEnabled()).isFalse();
        assertThat(robotConfig.getIncludedTags()).isEmpty();
        assertThat(robotConfig.getExcludedTags()).isEmpty();

        assertThat(robotConfig.isRemoteAgent()).isFalse();
        assertThat(robotConfig.getAgentConnectionHost()).isEqualTo("127.0.0.1");
        assertThat(robotConfig.getAgentConnectionPort()).isBetween(1, 65_535);
        assertThat(robotConfig.getAgentConnectionTimeout()).isEqualTo(30);

        assertThat(robotConfig.getInterpreter()).isEqualTo(SuiteExecutor.Python);
        assertThat(robotConfig.getExecutableFilePath()).isEqualTo("");
        assertThat(robotConfig.getExecutableFileArguments()).isEqualTo("");
    }

    @Test
    public void defaultConfigurationObtained_whenCustomConfigurationIsFilledWithDefaults() throws CoreException {
        final RobotLaunchConfiguration robotConfig = getDefaultRobotLaunchConfiguration();

        robotConfig.setRobotArguments("arguments");
        robotConfig.setProjectName(PROJECT_NAME);
        robotConfig.setSuitePaths(ImmutableMap.of("key", newArrayList("value")));
        robotConfig.setIsIncludeTagsEnabled(true);
        robotConfig.setIsExcludeTagsEnabled(true);
        robotConfig.setExcludedTags(newArrayList("excluded"));
        robotConfig.setIncludedTags(newArrayList("included"));

        robotConfig.setRemoteAgent(true);
        robotConfig.setAgentConnectionHostValue("1.2.3.4");
        robotConfig.setAgentConnectionPortValue("987");
        robotConfig.setAgentConnectionTimeoutValue("123");

        robotConfig.setInterpreter(SuiteExecutor.PyPy);
        robotConfig.setExecutableFilePath("path");
        robotConfig.setExecutableFileArguments("-new");

        robotConfig.fillDefaults();

        assertThat(robotConfig.getProjectName()).isEqualTo("");
        assertThat(robotConfig.getSuitePaths()).isEmpty();
        assertThat(robotConfig.getRobotArguments()).isEqualTo("");
        assertThat(robotConfig.isIncludeTagsEnabled()).isFalse();
        assertThat(robotConfig.isExcludeTagsEnabled()).isFalse();
        assertThat(robotConfig.getIncludedTags()).isEmpty();
        assertThat(robotConfig.getExcludedTags()).isEmpty();

        assertThat(robotConfig.isRemoteAgent()).isFalse();
        assertThat(robotConfig.getAgentConnectionHost()).isEqualTo("127.0.0.1");
        assertThat(robotConfig.getAgentConnectionPort()).isBetween(1, 65_535);
        assertThat(robotConfig.getAgentConnectionTimeout()).isEqualTo(30);

        assertThat(robotConfig.getInterpreter()).isEqualTo(SuiteExecutor.Python);
        assertThat(robotConfig.getExecutableFilePath()).isEqualTo("");
        assertThat(robotConfig.getExecutableFileArguments()).isEqualTo("");
    }

    @Test
    public void onlySelectedTestCasesAreUsed_inConfigurationForSelectedTestCases() throws CoreException {
        final IResource res1 = project.getFile("Resource1");
        final IResource res2 = project.getFile("Resource2");
        final List<String> casesForRes1 = newArrayList("case1", "case3");
        final List<String> casesForRes2 = newArrayList("case1");

        final ILaunchConfigurationWorkingCopy configuration = RobotLaunchConfiguration
                .prepareForSelectedTestCases(ImmutableMap.of(res1, casesForRes1, res2, casesForRes2));
        final RobotLaunchConfiguration robotConfig = new RobotLaunchConfiguration(configuration);

        assertThat(robotConfig.getProjectName()).isEqualTo(PROJECT_NAME);
        final Map<String, List<String>> suitePaths = robotConfig.getSuitePaths();
        assertThat(suitePaths.keySet()).containsExactlyInAnyOrder("Resource1", "Resource2");
        assertThat(suitePaths).containsEntry("Resource1", casesForRes1);
        assertThat(suitePaths).containsEntry("Resource2", casesForRes2);
        assertThat(robotConfig.getRobotArguments()).isEqualTo("");
        assertThat(robotConfig.isIncludeTagsEnabled()).isFalse();
        assertThat(robotConfig.isExcludeTagsEnabled()).isFalse();
        assertThat(robotConfig.getIncludedTags()).isEmpty();
        assertThat(robotConfig.getExcludedTags()).isEmpty();
        assertThat(robotConfig.getInterpreter()).isEqualTo(SuiteExecutor.Python);
    }

    @Test
    public void suitesObtained_whenSuitesCollectedFromConfiguration() throws CoreException {
        final List<IResource> resources = newArrayList();
        for (int i = 0; i < 3; i++) {
            final IResource res = project.getFile("Resource " + i + ".fake");
            resources.add(res);
        }
        final ILaunchConfigurationWorkingCopy configuration = RobotLaunchConfiguration.prepareDefault(resources);
        final RobotLaunchConfiguration robotConfig = new RobotLaunchConfiguration(configuration);
        final Map<IResource, List<String>> obtainedSuites = robotConfig.collectSuitesToRun();
        assertThat(obtainedSuites).hasSameSizeAs(resources);
        for (int i = 0; i < resources.size(); i++) {
            assertThat(obtainedSuites).containsKey(resources.get(i));
        }
    }

    @Test
    public void robotProjectObtainedFromConfiguration_whenProjectInWorkspace() throws CoreException {
        final IRobotLaunchConfiguration robotConfig = getDefaultRobotLaunchConfiguration();
        final RobotProject projectFromConfig = robotConfig.getRobotProject();
        assertThat(projectFromConfig).isEqualTo(RedPlugin.getModelManager().getModel().createRobotProject(project));
    }

    @Test
    public void whenProjectNotInWorkspace_coreExceptionIsThrown() throws CoreException {
        thrown.expect(CoreException.class);
        thrown.expectMessage("Project 'not_existing' cannot be found in workspace");

        final IRobotLaunchConfiguration robotConfig = getDefaultRobotLaunchConfiguration();
        robotConfig.setProjectName("not_existing");
        robotConfig.getRobotProject();
    }

    @Test
    public void configuredForRerunFailedTests_whenAskedForRerun() throws CoreException {
        final RobotLaunchConfiguration robotConfig = getDefaultRobotLaunchConfiguration();
        RobotLaunchConfiguration.fillForFailedTestsRerun(robotConfig.asWorkingCopy(), "path");
        assertThat(robotConfig.getRobotArguments()).isEqualTo("-R path");
        assertThat(robotConfig.getSuitePaths()).isEmpty();
    }

    @Test
    public void projectIsReturned_whenAskedForResourcesUnderDebug() throws CoreException {
        final RobotLaunchConfiguration robotConfig = getDefaultRobotLaunchConfiguration();
        robotConfig.setSuitePaths(Collections.emptyMap());
        assertThat(robotConfig.getResourcesUnderDebug()).containsExactly(project);
    }

    @Test
    public void resourcesAreReturned_whenAskedForResourcesUnderDebug() throws CoreException, IOException {
        final IResource res1 = projectProvider.createFile("DebugResource1");
        final IResource res2 = projectProvider.createFile("DebugResource2");
        final List<String> casesForRes1 = newArrayList("case1", "case2");
        final List<String> casesForRes2 = newArrayList("case1");

        final RobotLaunchConfiguration robotConfig = getDefaultRobotLaunchConfiguration();
        robotConfig.setSuitePaths(ImmutableMap.of(res1.getName(), casesForRes1, res2.getName(), casesForRes2));
        assertThat(robotConfig.getResourcesUnderDebug()).containsOnly(res1, res2);
    }

    private RobotLaunchConfiguration getDefaultRobotLaunchConfiguration() throws CoreException {
        final IResource res = project.getFile("Resource");
        final List<IResource> resources = newArrayList(res);
        return new RobotLaunchConfiguration(RobotLaunchConfiguration.prepareDefault(resources));
    }
}

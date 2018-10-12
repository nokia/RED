/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.launch.local;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.rf.ide.core.executor.SuiteExecutor;
import org.robotframework.ide.eclipse.main.plugin.launch.IRobotLaunchConfiguration;
import org.robotframework.red.junit.ProjectProvider;
import org.robotframework.red.junit.RunConfigurationProvider;

import com.google.common.collect.ImmutableMap;

public class RobotLaunchConfigurationTest {

    private static final String PROJECT_NAME = RobotLaunchConfigurationTest.class.getSimpleName();

    @Rule
    public ProjectProvider projectProvider = new ProjectProvider(PROJECT_NAME);

    @Rule
    public RunConfigurationProvider runConfigurationProvider = new RunConfigurationProvider(
            RobotLaunchConfiguration.TYPE_ID);

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
        assertThat(robotConfig.getConfigurationVersion())
                .isEqualTo(RobotLaunchConfiguration.CURRENT_CONFIGURATION_VERSION);

        assertThat(robotConfig.isUsingRemoteAgent()).isFalse();
        assertThat(robotConfig.getAgentConnectionHost()).isEqualTo("127.0.0.1");
        assertThat(robotConfig.getAgentConnectionPort()).isBetween(1, 65_535);
        assertThat(robotConfig.getAgentConnectionTimeout()).isEqualTo(30);

        assertThat(robotConfig.isUsingInterpreterFromProject()).isTrue();
        assertThat(robotConfig.getInterpreter()).isEqualTo(SuiteExecutor.Python);
        assertThat(robotConfig.getExecutableFilePath()).isEqualTo("");
        assertThat(robotConfig.getExecutableFileArguments()).isEqualTo("");

        assertThat(robotConfig.getEnvironmentVariables()).contains("PYTHONIOENCODING=utf8");
    }

    @Test
    public void defaultConfigurationObtained_whenCustomConfigurationIsFilledWithDefaults() throws CoreException {
        final RobotLaunchConfiguration robotConfig = getDefaultRobotLaunchConfiguration();

        robotConfig.setRobotArguments("arguments");
        robotConfig.setProjectName(PROJECT_NAME);
        robotConfig.setSuitePaths(ImmutableMap.of("key", asList("value")));
        robotConfig.setIsIncludeTagsEnabled(true);
        robotConfig.setIsExcludeTagsEnabled(true);
        robotConfig.setExcludedTags(asList("excluded"));
        robotConfig.setIncludedTags(asList("included"));

        robotConfig.setUsingRemoteAgent(true);
        robotConfig.setAgentConnectionHostValue("1.2.3.4");
        robotConfig.setAgentConnectionPortValue("987");
        robotConfig.setAgentConnectionTimeoutValue("123");
        robotConfig.setInterpreter(SuiteExecutor.PyPy);
        robotConfig.setInterpreterArguments("-a");
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
        assertThat(robotConfig.getConfigurationVersion())
                .isEqualTo(RobotLaunchConfiguration.CURRENT_CONFIGURATION_VERSION);

        assertThat(robotConfig.isUsingRemoteAgent()).isFalse();
        assertThat(robotConfig.getAgentConnectionHost()).isEqualTo("127.0.0.1");
        assertThat(robotConfig.getAgentConnectionPort()).isBetween(1, 65_535);
        assertThat(robotConfig.getAgentConnectionTimeout()).isEqualTo(30);

        assertThat(robotConfig.isUsingInterpreterFromProject()).isTrue();
        assertThat(robotConfig.getInterpreter()).isEqualTo(SuiteExecutor.Python);
        assertThat(robotConfig.getInterpreterArguments()).isEqualTo("");
        assertThat(robotConfig.getExecutableFilePath()).isEqualTo("");
        assertThat(robotConfig.getExecutableFileArguments()).isEqualTo("");
    }

    @Test
    public void onlySelectedTestCasesAreUsed_inConfigurationForSelectedTestCases() throws CoreException {
        final IResource res1 = projectProvider.getFile("Resource1");
        final IResource res2 = projectProvider.getFile("Resource2");
        final List<String> casesForRes1 = asList("case1", "case3");
        final List<String> casesForRes2 = asList("case1");

        final ILaunchConfigurationWorkingCopy configuration = RobotLaunchConfiguration
                .prepareForSelectedTestCases(ImmutableMap.of(res1, casesForRes1, res2, casesForRes2));
        final RobotLaunchConfiguration robotConfig = new RobotLaunchConfiguration(configuration);

        assertThat(robotConfig.getProjectName()).isEqualTo(PROJECT_NAME);
        assertThat(robotConfig.getSuitePaths())
                .isEqualTo(ImmutableMap.of("Resource1", casesForRes1, "Resource2", casesForRes2));
        assertThat(robotConfig.getRobotArguments()).isEqualTo("");
        assertThat(robotConfig.isIncludeTagsEnabled()).isFalse();
        assertThat(robotConfig.isExcludeTagsEnabled()).isFalse();
        assertThat(robotConfig.getIncludedTags()).isEmpty();
        assertThat(robotConfig.getExcludedTags()).isEmpty();
        assertThat(robotConfig.getInterpreter()).isEqualTo(SuiteExecutor.Python);

        assertThat(robotConfig.getEnvironmentVariables()).contains("PYTHONIOENCODING=utf8");
    }

    @Test
    public void suitesObtained_whenSuitesCollectedFromConfiguration() throws CoreException {
        final List<IResource> resources = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            final IResource res = projectProvider.getFile("Resource " + i + ".fake");
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
    public void emptySuitesObtained_whenProjectNameIsEmpty() throws CoreException {
        final List<IResource> resources = asList(projectProvider.getFile("Resource 1.fake"));
        final ILaunchConfigurationWorkingCopy configuration = RobotLaunchConfiguration.prepareDefault(resources);
        final RobotLaunchConfiguration robotConfig = new RobotLaunchConfiguration(configuration);
        robotConfig.setProjectName("");
        assertThat(robotConfig.collectSuitesToRun()).isEmpty();
    }

    @Test
    public void robotProjectObtainedFromConfiguration_whenProjectInWorkspace() throws CoreException {
        final IRobotLaunchConfiguration robotConfig = getDefaultRobotLaunchConfiguration();
        assertThat(robotConfig.getProject()).isEqualTo(projectProvider.getProject());
    }

    @Test
    public void whenProjectNotInWorkspace_coreExceptionIsThrown() throws CoreException {
        final IRobotLaunchConfiguration robotConfig = getDefaultRobotLaunchConfiguration();
        robotConfig.setProjectName("not_existing");

        assertThatExceptionOfType(CoreException.class).isThrownBy(robotConfig::getProject)
                .withMessage("Project 'not_existing' cannot be found in workspace")
                .withNoCause();
    }

    @Test
    public void whenProjectIsClosed_coreExceptionIsThrown() throws CoreException {
        projectProvider.getProject().close(null);

        final IRobotLaunchConfiguration robotConfig = getDefaultRobotLaunchConfiguration();

        assertThatExceptionOfType(CoreException.class).isThrownBy(robotConfig::getProject)
                .withMessage("Project '" + PROJECT_NAME + "' is currently closed")
                .withNoCause();
    }

    @Test
    public void whenProjectIsEmpty_coreExceptionIsThrown() throws CoreException {
        final IRobotLaunchConfiguration robotConfig = getDefaultRobotLaunchConfiguration();
        robotConfig.setProjectName("");

        assertThatExceptionOfType(CoreException.class).isThrownBy(robotConfig::getProject)
                .withMessage("Project cannot be empty")
                .withNoCause();
    }

    @Test
    public void configuredForRerunFailedTests_whenAskedForRerun() throws CoreException {
        final RobotLaunchConfiguration robotConfig = getDefaultRobotLaunchConfiguration();
        RobotLaunchConfiguration.fillForFailedTestsRerun(robotConfig.asWorkingCopy(), "path");
        assertThat(robotConfig.getRobotArguments()).isEqualTo("-R path");
        assertThat(robotConfig.getSuitePaths()).isEmpty();
    }

    private RobotLaunchConfiguration getDefaultRobotLaunchConfiguration() throws CoreException {
        final IResource res = projectProvider.getFile("Resource");
        final List<IResource> resources = asList(res);
        return new RobotLaunchConfiguration(RobotLaunchConfiguration.prepareDefault(resources));
    }
}

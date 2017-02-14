/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.launch;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyMapOf;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.HashMap;
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
import org.junit.Test;
import org.rf.ide.core.executor.SuiteExecutor;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.red.junit.ProjectProvider;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;

public class RobotLaunchConfigurationTest {

    private static ILaunchManager manager;

    private final static String PROJECT_NAME = RobotLaunchConfigurationTest.class.getSimpleName();
    private IProject project;

    @ClassRule
    public static ProjectProvider projectProvider = new ProjectProvider(PROJECT_NAME);


    @Before
    public void setup() throws CoreException {
        manager = DebugPlugin.getDefault().getLaunchManager();
        removeAllConfigurations();
        project = projectProvider.getProject();
    }

    @AfterClass
    public static void clean() throws CoreException {
        removeAllConfigurations();
    }

    private static void removeAllConfigurations() throws CoreException {
        final ILaunchConfigurationType launchConfigurationType = manager
                .getLaunchConfigurationType(RobotLaunchConfiguration.TYPE_ID);
        final ILaunchConfiguration[] launchConfigs = manager.getLaunchConfigurations(launchConfigurationType);
        for (final ILaunchConfiguration config : launchConfigs) {
            config.delete();
        }

    }

    @Test
    public void nullVariablesArrayIsReturned_whenThereAreNoVariablesDefined() throws Exception {
        // this mean that the process will inherit environment variables from parent process

        final ILaunchConfiguration config = mock(ILaunchConfiguration.class);
        when(config.getAttribute(eq(ILaunchManager.ATTR_ENVIRONMENT_VARIABLES), anyMapOf(String.class, String.class)))
                .thenReturn(null);

        final RobotLaunchConfiguration robotConfig = new RobotLaunchConfiguration(config);

        assertThat(robotConfig.getEnvironmentVariables()).isNull();
    }

    @Test
    public void onlyVariablesFromConfigAreReturned_whenTheyAreDefinedAndOverrideIsEnabled() throws Exception {
        final Map<String, String> vars = ImmutableMap.of("VAR1", "x", "VAR2", "y", "VAR3", "z");

        final ILaunchConfiguration config = mock(ILaunchConfiguration.class);
        when(config.getAttribute(eq(ILaunchManager.ATTR_ENVIRONMENT_VARIABLES), anyMapOf(String.class, String.class)))
                .thenReturn(vars);
        when(config.getAttribute(eq(ILaunchManager.ATTR_APPEND_ENVIRONMENT_VARIABLES), anyBoolean())).thenReturn(false);

        final RobotLaunchConfiguration robotConfig = new RobotLaunchConfiguration(config);

        assertThat(robotConfig.getEnvironmentVariables()).containsExactly("VAR1=x", "VAR2=y", "VAR3=z");
    }

    @Test
    public void inheritedVariablesFromCurrentProcessAndConfigVariablesAreReturned_whenTheyAreDefinedAndAppendingIsEnabled()
            throws Exception {
        final Map<String, String> vars = ImmutableMap.of("VAR1", "x", "VAR2", "y", "VAR3", "z");

        final ILaunchConfiguration config = mock(ILaunchConfiguration.class);
        when(config.getAttribute(eq(ILaunchManager.ATTR_ENVIRONMENT_VARIABLES), anyMapOf(String.class, String.class)))
                .thenReturn(vars);
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
        assertThat(robotConfig.getExecutor()).isEqualTo(SuiteExecutor.Python);
        assertThat(robotConfig.getExecutorArguments()).isEqualTo("");
        assertThat(robotConfig.getRemoteDebugHost()).isEqualTo("");
        assertThat(robotConfig.isIncludeTagsEnabled()).isFalse();
        assertThat(robotConfig.isExcludeTagsEnabled()).isFalse();
        assertThat(robotConfig.getIncludedTags()).isEmpty();
        assertThat(robotConfig.getExcludedTags()).isEmpty();
    }

    @Test
    public void defaultConfigurationObtained_whenCustomConfigurationFilledDefaults() throws CoreException {
        final RobotLaunchConfiguration robotConfig = getDefaultRobotLaunchConfiguration();
        final Map<String, List<String>> suites = new HashMap<String, List<String>>();
        suites.put("key", newArrayList("value"));
        robotConfig.setExecutor(SuiteExecutor.PyPy);
        robotConfig.setExecutorArguments("arguments");
        robotConfig.setProjectName(PROJECT_NAME);
        robotConfig.setSuitePaths(suites);
        robotConfig.setIsIncludeTagsEnabled(true);
        robotConfig.setIsExcludeTagsEnabled(true);
        robotConfig.setExcludedTags(newArrayList("excluded"));
        robotConfig.setIncludedTags(newArrayList("included"));
        robotConfig.setRemoteDebugHost("Host");
        RobotLaunchConfiguration.fillDefaults(robotConfig.asWorkingCopy());
        assertThat(robotConfig.getProjectName()).isEqualTo("");
        assertThat(robotConfig.getSuitePaths()).isEmpty();
        assertThat(robotConfig.getExecutor()).isEqualTo(SuiteExecutor.Python);
        assertThat(robotConfig.getExecutorArguments()).isEqualTo("");
        assertThat(robotConfig.getRemoteDebugHost()).isEqualTo("");
        assertThat(robotConfig.isIncludeTagsEnabled()).isFalse();
        assertThat(robotConfig.isExcludeTagsEnabled()).isFalse();
        assertThat(robotConfig.getIncludedTags()).isEmpty();
        assertThat(robotConfig.getExcludedTags()).isEmpty();
    }

    @Test
    public void onlySelectedTestCasesAreUsed_inConfigurationForSelectedTestCases() throws CoreException {
        final IResource res = project.getFile("Resource1");
        final IResource res2 = project.getFile("Resource2");
        final Map<IResource, List<String>> resourcesToTestCases = new HashMap<IResource, List<String>>();
        final List<String> casesForRes = newArrayList("case1", "case3");
        final List<String> casesForRes2 = newArrayList("case1");
        resourcesToTestCases.put(res, casesForRes);
        resourcesToTestCases.put(res2, casesForRes2);
        final ILaunchConfigurationWorkingCopy configuration = RobotLaunchConfiguration
                .prepareLaunchConfigurationForSelectedTestCases(resourcesToTestCases);
        final RobotLaunchConfiguration robotConfig = new RobotLaunchConfiguration(configuration);
        assertThat(robotConfig.getProjectName()).isEqualTo(PROJECT_NAME);
        final Map<String, List<String>> suitePaths = robotConfig.getSuitePaths();
        assertThat(suitePaths.keySet()).containsExactlyInAnyOrder("Resource1", "Resource2");
        assertThat(suitePaths).containsEntry("Resource1", casesForRes);
        assertThat(suitePaths).containsEntry("Resource2", casesForRes2);
        assertThat(robotConfig.getExecutor()).isEqualTo(SuiteExecutor.Python);
        assertThat(robotConfig.getExecutorArguments()).isEqualTo("");
        assertThat(robotConfig.getRemoteDebugHost()).isEqualTo("");
        assertThat(robotConfig.isIncludeTagsEnabled()).isFalse();
        assertThat(robotConfig.isExcludeTagsEnabled()).isFalse();
        assertThat(robotConfig.getIncludedTags()).isEmpty();
        assertThat(robotConfig.getExcludedTags()).isEmpty();
    }

    @Test
    public void suitesObtained_whenSuitesCollectedFromConfiguration() throws CoreException {
        final List<IResource> resources = newArrayList();
        for (int i = 0; i < 3; i++) {
            final IResource res = project.getFile("Resource " + i + ".fake");
            resources.add(res);
        }
        final ILaunchConfigurationWorkingCopy configuration = RobotLaunchConfiguration
                .createDefault(manager.getLaunchConfigurationType(RobotLaunchConfiguration.TYPE_ID), resources);
        final RobotLaunchConfiguration robotConfig = new RobotLaunchConfiguration(configuration);
        final Map<IResource, List<String>> obtainedSuites = robotConfig.collectSuitesToRun();
        assertThat(obtainedSuites).hasSameSizeAs(resources);
        for (int i = 0; i < resources.size(); i++) {
            assertThat(obtainedSuites).containsKey(resources.get(i));
        }
    }

    @Test
    public void robotProjectObtainedFromConfiguration_whenProjectInWorkspace() throws CoreException {
        final IResource res = project.getFile("Resource");
        final List<IResource> resources = newArrayList(res);
        final ILaunchConfigurationWorkingCopy configuration = RobotLaunchConfiguration
                .createDefault(manager.getLaunchConfigurationType(RobotLaunchConfiguration.TYPE_ID), resources);
        final RobotLaunchConfiguration robotConfig = new RobotLaunchConfiguration(configuration);
        final RobotProject projectFromConfig = robotConfig.getRobotProject();
        assertThat(projectFromConfig).isEqualTo(RedPlugin.getModelManager().getModel().createRobotProject(project));
    }

    @Test
    public void remoteDebugPortAndTimeoutAreCorrect_whenSet() throws CoreException {
        final RobotLaunchConfiguration robotConfig = getDefaultRobotLaunchConfiguration();
        robotConfig.setRemoteDebugPort("1234");
        robotConfig.setRemoteDebugTimeout("9876");
        final Optional<Integer> port = robotConfig.getRemoteDebugPort();
        final Optional<Integer> timeout = robotConfig.getRemoteDebugTimeout();
        assertThat(port.isPresent()).isTrue();
        assertThat(port.get()).isEqualTo(1234);
        assertThat(timeout.isPresent()).isTrue();
        assertThat(timeout.get()).isEqualTo(9876);
    }

    @Test
    public void remoteDebugPortAndTimeoutAreAbsent_whenNotSet() throws CoreException {
        final RobotLaunchConfiguration robotConfig = getDefaultRobotLaunchConfiguration();
        final Optional<Integer> port = robotConfig.getRemoteDebugPort();
        final Optional<Integer> timeout = robotConfig.getRemoteDebugTimeout();
        assertThat(port.isPresent()).isFalse();
        assertThat(timeout.isPresent()).isFalse();
    }

    @Test
    public void configurationSuitableForResources_whenApplicable() throws CoreException, IOException {
        final IResource res = projectProvider.createFile("Resource", "");
        final List<IResource> resources = newArrayList(res);
        final ILaunchConfigurationWorkingCopy configuration = RobotLaunchConfiguration
                .createDefault(manager.getLaunchConfigurationType(RobotLaunchConfiguration.TYPE_ID), resources);
        final RobotLaunchConfiguration robotConfig = new RobotLaunchConfiguration(configuration);
        assertThat(robotConfig.isSuitableFor(resources)).isTrue();
    }

    @Test
    public void configurationNotSuitableForResources_whenNotApplicable() throws CoreException, IOException {
        final IResource res = projectProvider.createFile("Resource", "");
        final List<IResource> resources = newArrayList(res);
        final ILaunchConfigurationWorkingCopy configuration = RobotLaunchConfiguration
                .createDefault(manager.getLaunchConfigurationType(RobotLaunchConfiguration.TYPE_ID), resources);
        final RobotLaunchConfiguration robotConfig = new RobotLaunchConfiguration(configuration);

        final IResource anotherRes = projectProvider.createFile("Another Resource", "");
        resources.add(anotherRes);
        assertThat(robotConfig.isSuitableFor(resources)).isFalse();
    }

    @Test
    public void configuredForRerunFailedTests_whenAskedForRerun() throws CoreException {
        final RobotLaunchConfiguration robotConfig = getDefaultRobotLaunchConfiguration();
        RobotLaunchConfiguration.prepareRerunFailedTestsConfiguration(robotConfig.asWorkingCopy(), "path");
        assertThat(robotConfig.getExecutorArguments()).isEqualTo("-R path");
        assertThat(robotConfig.getSuitePaths()).isEmpty();
    }

    private RobotLaunchConfiguration getDefaultRobotLaunchConfiguration() throws CoreException {
        final IResource res = project.getFile("Resource");
        final List<IResource> resources = newArrayList(res);
        final ILaunchConfigurationWorkingCopy configuration = RobotLaunchConfiguration
                .createDefault(manager.getLaunchConfigurationType(RobotLaunchConfiguration.TYPE_ID), resources);
        return new RobotLaunchConfiguration(configuration);
    }
}

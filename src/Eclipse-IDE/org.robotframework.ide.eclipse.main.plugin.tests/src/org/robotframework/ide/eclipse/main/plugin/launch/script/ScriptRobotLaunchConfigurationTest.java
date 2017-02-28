/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.launch.script;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;

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
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.rf.ide.core.executor.RedSystemProperties;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.red.junit.ProjectProvider;

public class ScriptRobotLaunchConfigurationTest {

    private final static String PROJECT_NAME = ScriptRobotLaunchConfigurationTest.class.getSimpleName();

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
        final ILaunchConfigurationType type = manager
                .getLaunchConfigurationType(ScriptRobotLaunchConfiguration.TYPE_ID);
        final ILaunchConfiguration[] launchConfigs = manager.getLaunchConfigurations(type);
        for (final ILaunchConfiguration config : launchConfigs) {
            config.delete();
        }
    }

    @Test
    public void systemDependentAttributesObtained() {
        if (RedSystemProperties.isWindowsPlatform()) {
            assertThat(ScriptRobotLaunchConfiguration.getSystemDependentScriptExtensions()).containsExactly("*.bat",
                    "*.*");
            assertThat(ScriptRobotLaunchConfiguration.getSystemDependentScriptRunCommand()).isEqualTo("cmd /c start");
        } else {
            assertThat(ScriptRobotLaunchConfiguration.getSystemDependentScriptExtensions()).containsExactly("*.sh",
                    "*.*");
            assertThat(ScriptRobotLaunchConfiguration.getSystemDependentScriptRunCommand()).isEqualTo("");
        }
    }

    @Test
    public void defaultConfigurationObtained_whenDefaultConfigurationIsCreated() throws CoreException {
        final ScriptRobotLaunchConfiguration robotConfig = getDefaultScriptRobotLaunchConfiguration();
        assertThat(robotConfig.getName()).isEqualTo("Resource");
        assertThat(robotConfig.getProjectName()).isEqualTo(PROJECT_NAME);
        assertThat(robotConfig.getSuitePaths().keySet()).containsExactly("Resource");
        assertThat(robotConfig.getScriptPath()).isEqualTo("");
        assertThat(robotConfig.getScriptArguments()).isEqualTo("");
        assertThat(robotConfig.getScriptRunCommand())
                .isEqualTo(RedSystemProperties.isWindowsPlatform() ? "cmd /c start" : "");
        assertThat(robotConfig.isIncludeTagsEnabled()).isFalse();
        assertThat(robotConfig.isExcludeTagsEnabled()).isFalse();
        assertThat(robotConfig.getIncludedTags()).isEmpty();
        assertThat(robotConfig.getExcludedTags()).isEmpty();
        assertThat(robotConfig.getRemoteDebugHost()).isEqualTo("127.0.0.1");
        assertThat(robotConfig.getRemoteDebugPort()).isEqualTo(12345);
        assertThat(robotConfig.getRemoteDebugTimeout()).isEqualTo(30_000);
    }

    @Test
    public void defaultConfigurationObtained_whenCustomConfigurationFilledDefaults() throws CoreException {
        final ScriptRobotLaunchConfiguration robotConfig = getDefaultScriptRobotLaunchConfiguration();
        final Map<String, List<String>> suites = new HashMap<>();
        suites.put("key", newArrayList("value"));
        robotConfig.setScriptPath("path");
        robotConfig.setScriptArguments("arguments");
        robotConfig.setScriptRunCommand("cmd");
        robotConfig.setProjectName(PROJECT_NAME);
        robotConfig.setSuitePaths(suites);
        robotConfig.setIsIncludeTagsEnabled(true);
        robotConfig.setIsExcludeTagsEnabled(true);
        robotConfig.setExcludedTags(newArrayList("excluded"));
        robotConfig.setIncludedTags(newArrayList("included"));
        robotConfig.setRemoteDebugHostValue("1.2.3.4");
        robotConfig.setRemoteDebugPortValue("987");
        robotConfig.setRemoteDebugTimeoutValue("123");
        robotConfig.fillDefaults();
        assertThat(robotConfig.getProjectName()).isEqualTo("");
        assertThat(robotConfig.getSuitePaths()).isEmpty();
        assertThat(robotConfig.getScriptPath()).isEqualTo("");
        assertThat(robotConfig.getScriptArguments()).isEqualTo("");
        assertThat(robotConfig.getScriptRunCommand())
                .isEqualTo(RedSystemProperties.isWindowsPlatform() ? "cmd /c start" : "");
        assertThat(robotConfig.isIncludeTagsEnabled()).isFalse();
        assertThat(robotConfig.isExcludeTagsEnabled()).isFalse();
        assertThat(robotConfig.getIncludedTags()).isEmpty();
        assertThat(robotConfig.getExcludedTags()).isEmpty();
        assertThat(robotConfig.getRemoteDebugHost()).isEqualTo("127.0.0.1");
        assertThat(robotConfig.getRemoteDebugPort()).isEqualTo(12345);
        assertThat(robotConfig.getRemoteDebugTimeout()).isEqualTo(30_000);
    }

    @Test
    public void onlySelectedTestCasesAreUsed_inConfigurationForSelectedTestCases() throws CoreException {
        final IResource res = project.getFile("Resource1");
        final IResource res2 = project.getFile("Resource2");
        final Map<IResource, List<String>> resourcesToTestCases = new HashMap<>();
        final List<String> casesForRes = newArrayList("case1", "case3");
        final List<String> casesForRes2 = newArrayList("case1");
        resourcesToTestCases.put(res, casesForRes);
        resourcesToTestCases.put(res2, casesForRes2);
        final ILaunchConfigurationWorkingCopy configuration = ScriptRobotLaunchConfiguration
                .prepareForSelectedTestCases(resourcesToTestCases);
        final ScriptRobotLaunchConfiguration robotConfig = new ScriptRobotLaunchConfiguration(configuration);
        assertThat(robotConfig.getProjectName()).isEqualTo(PROJECT_NAME);
        final Map<String, List<String>> suitePaths = robotConfig.getSuitePaths();
        assertThat(suitePaths.keySet()).containsExactlyInAnyOrder("Resource1", "Resource2");
        assertThat(suitePaths).containsEntry("Resource1", casesForRes);
        assertThat(suitePaths).containsEntry("Resource2", casesForRes2);
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
        final ILaunchConfigurationWorkingCopy configuration = ScriptRobotLaunchConfiguration.prepareDefault(resources);
        final ScriptRobotLaunchConfiguration robotConfig = new ScriptRobotLaunchConfiguration(configuration);
        final Map<IResource, List<String>> obtainedSuites = robotConfig.collectSuitesToRun();
        assertThat(obtainedSuites).hasSameSizeAs(resources);
        for (int i = 0; i < resources.size(); i++) {
            assertThat(obtainedSuites).containsKey(resources.get(i));
        }
    }

    @Test
    public void remoteProjectIsNotDefinedDirectly() throws CoreException {
        final ScriptRobotLaunchConfiguration robotConfig = getDefaultScriptRobotLaunchConfiguration();
        assertThat(robotConfig.isDefiningProjectDirectly()).isFalse();
    }

    @Test
    public void whenServerIpIsEmpty_coreExceptionIsThrown() throws CoreException {
        thrown.expect(CoreException.class);
        thrown.expectMessage("Server IP cannot be empty");

        final ScriptRobotLaunchConfiguration robotConfig = getDefaultScriptRobotLaunchConfiguration();
        robotConfig.setRemoteDebugHostValue("");
        robotConfig.getRemoteDebugHost();
    }

    @Test
    public void whenPortIsEmpty_coreExceptionIsThrown() throws CoreException {
        thrown.expect(CoreException.class);
        thrown.expectMessage("Server port '' must be an Integer between 1 and 65,535");

        final ScriptRobotLaunchConfiguration robotConfig = getDefaultScriptRobotLaunchConfiguration();
        robotConfig.setRemoteDebugPortValue("");
        robotConfig.getRemoteDebugPort();
    }

    @Test
    public void whenPortIsNotANumber_coreExceptionIsThrown() throws CoreException {
        thrown.expect(CoreException.class);
        thrown.expectMessage("Server port 'abc' must be an Integer between 1 and 65,535");

        final ScriptRobotLaunchConfiguration robotConfig = getDefaultScriptRobotLaunchConfiguration();
        robotConfig.setRemoteDebugPortValue("abc");
        robotConfig.getRemoteDebugPort();
    }

    @Test
    public void whenPortIsBelowRange_coreExceptionIsThrown() throws CoreException {
        thrown.expect(CoreException.class);
        thrown.expectMessage("Server port '0' must be an Integer between 1 and 65,535");

        final ScriptRobotLaunchConfiguration robotConfig = getDefaultScriptRobotLaunchConfiguration();
        robotConfig.setRemoteDebugPortValue("0");
        robotConfig.getRemoteDebugPort();
    }

    @Test
    public void whenPortIsAboveRange_coreExceptionIsThrown() throws CoreException {
        thrown.expect(CoreException.class);
        thrown.expectMessage("Server port '65536' must be an Integer between 1 and 65,535");

        final ScriptRobotLaunchConfiguration robotConfig = getDefaultScriptRobotLaunchConfiguration();
        robotConfig.setRemoteDebugPortValue("65536");
        robotConfig.getRemoteDebugPort();
    }

    @Test
    public void whenTimeoutIsEmpty_coreExceptionIsThrown() throws CoreException {
        thrown.expect(CoreException.class);
        thrown.expectMessage("Connection timeout '' must be an Integer between 1 and 3,600,000");

        final ScriptRobotLaunchConfiguration robotConfig = getDefaultScriptRobotLaunchConfiguration();
        robotConfig.setRemoteDebugTimeoutValue("");
        robotConfig.getRemoteDebugTimeout();
    }

    @Test
    public void whenTimeoutIsNotANumber_coreExceptionIsThrown() throws CoreException {
        thrown.expect(CoreException.class);
        thrown.expectMessage("Connection timeout 'abc' must be an Integer between 1 and 3,600,000");

        final ScriptRobotLaunchConfiguration robotConfig = getDefaultScriptRobotLaunchConfiguration();
        robotConfig.setRemoteDebugTimeoutValue("abc");
        robotConfig.getRemoteDebugTimeout();
    }

    @Test
    public void whenTimeoutIsBelowRange_coreExceptionIsThrown() throws CoreException {
        thrown.expect(CoreException.class);
        thrown.expectMessage("Connection timeout '0' must be an Integer between 1 and 3,600,000");

        final ScriptRobotLaunchConfiguration robotConfig = getDefaultScriptRobotLaunchConfiguration();
        robotConfig.setRemoteDebugTimeoutValue("0");
        robotConfig.getRemoteDebugTimeout();
    }

    @Test
    public void whenTimeoutIsAboveRange_coreExceptionIsThrown() throws CoreException {
        thrown.expect(CoreException.class);
        thrown.expectMessage("Connection timeout '3600001' must be an Integer between 1 and 3,600,000");

        final ScriptRobotLaunchConfiguration robotConfig = getDefaultScriptRobotLaunchConfiguration();
        robotConfig.setRemoteDebugTimeoutValue("3600001");
        robotConfig.getRemoteDebugTimeout();
    }

    @Test
    public void robotProjectObtainedFromConfiguration_whenProjectInWorkspace() throws CoreException {
        final ScriptRobotLaunchConfiguration robotConfig = getDefaultScriptRobotLaunchConfiguration();
        final RobotProject projectFromConfig = robotConfig.getRobotProject();
        assertThat(projectFromConfig).isEqualTo(RedPlugin.getModelManager().getModel().createRobotProject(project));
    }

    @Test
    public void whenProjectNotInWorkspace_coreExceptionIsThrown() throws CoreException {
        thrown.expect(CoreException.class);
        thrown.expectMessage("Project 'not_existing' cannot be found in workspace");

        final ScriptRobotLaunchConfiguration robotConfig = getDefaultScriptRobotLaunchConfiguration();
        robotConfig.setProjectName("not_existing");
        robotConfig.getRobotProject();
    }

    private ScriptRobotLaunchConfiguration getDefaultScriptRobotLaunchConfiguration() throws CoreException {
        final IResource res = project.getFile("Resource");
        final List<IResource> resources = newArrayList(res);
        return new ScriptRobotLaunchConfiguration(ScriptRobotLaunchConfiguration.prepareDefault(resources));
    }
}

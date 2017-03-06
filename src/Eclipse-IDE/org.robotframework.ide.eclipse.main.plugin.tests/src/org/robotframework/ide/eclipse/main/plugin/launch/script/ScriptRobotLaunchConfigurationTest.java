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
        final String[] expectedScriptExtensions = RedSystemProperties.isWindowsPlatform()
                ? new String[] { "*.bat;*.com;*.exe", "*.*" } : new String[] { "*.sh", "*.*" };
        assertThat(ScriptRobotLaunchConfiguration.getSystemDependentScriptExtensions())
                .containsExactly(expectedScriptExtensions);
    }

    @Test
    public void defaultConfigurationObtained_whenDefaultConfigurationIsCreated() throws CoreException {
        final ScriptRobotLaunchConfiguration robotConfig = getDefaultScriptRobotLaunchConfiguration();
        assertThat(robotConfig.getName()).isEqualTo("Resource");
        assertThat(robotConfig.getProjectName()).isEqualTo(PROJECT_NAME);
        assertThat(robotConfig.getSuitePaths().keySet()).containsExactly("Resource");
        assertThat(robotConfig.getScriptPath()).isEqualTo("");
        assertThat(robotConfig.getScriptArguments()).isEqualTo("");
        assertThat(robotConfig.isIncludeTagsEnabled()).isFalse();
        assertThat(robotConfig.isExcludeTagsEnabled()).isFalse();
        assertThat(robotConfig.getIncludedTags()).isEmpty();
        assertThat(robotConfig.getExcludedTags()).isEmpty();
        assertThat(robotConfig.getRemoteHost().isPresent()).isFalse();
        assertThat(robotConfig.getRemotePort().isPresent()).isFalse();
        assertThat(robotConfig.getRemoteTimeout().isPresent()).isFalse();
    }

    @Test
    public void defaultConfigurationObtained_whenCustomConfigurationIsFilledWithDefaults() throws CoreException {
        final ScriptRobotLaunchConfiguration robotConfig = getDefaultScriptRobotLaunchConfiguration();
        final Map<String, List<String>> suites = new HashMap<>();
        suites.put("key", newArrayList("value"));
        robotConfig.setScriptPath("path");
        robotConfig.setScriptArguments("arguments");
        robotConfig.setProjectName(PROJECT_NAME);
        robotConfig.setSuitePaths(suites);
        robotConfig.setIsIncludeTagsEnabled(true);
        robotConfig.setIsExcludeTagsEnabled(true);
        robotConfig.setExcludedTags(newArrayList("excluded"));
        robotConfig.setIncludedTags(newArrayList("included"));
        robotConfig.setRemoteHostValue("1.2.3.4");
        robotConfig.setRemotePortValue("987");
        robotConfig.setRemoteTimeoutValue("123");
        robotConfig.fillDefaults();
        assertThat(robotConfig.getProjectName()).isEqualTo("");
        assertThat(robotConfig.getSuitePaths()).isEmpty();
        assertThat(robotConfig.getScriptPath()).isEqualTo("");
        assertThat(robotConfig.getScriptArguments()).isEqualTo("");
        assertThat(robotConfig.isIncludeTagsEnabled()).isFalse();
        assertThat(robotConfig.isExcludeTagsEnabled()).isFalse();
        assertThat(robotConfig.getIncludedTags()).isEmpty();
        assertThat(robotConfig.getExcludedTags()).isEmpty();
        assertThat(robotConfig.getRemoteHost().isPresent()).isFalse();
        assertThat(robotConfig.getRemotePort().isPresent()).isFalse();
        assertThat(robotConfig.getRemoteTimeout().isPresent()).isFalse();
    }

    @Test
    public void remoteProjectIsNotDefinedDirectly() throws CoreException {
        final ScriptRobotLaunchConfiguration robotConfig = getDefaultScriptRobotLaunchConfiguration();
        assertThat(robotConfig.isDefiningProjectDirectly()).isFalse();
    }

    @Test
    public void whenServerIpIsEmpty_emptyOptionalIsReturned() throws CoreException {
        final ScriptRobotLaunchConfiguration robotConfig = getDefaultScriptRobotLaunchConfiguration();
        robotConfig.setRemoteHostValue("");
        assertThat(robotConfig.getRemoteHost().isPresent()).isFalse();
    }

    @Test
    public void whenPortIsEmpty_emptyOptionalIsReturned() throws CoreException {
        final ScriptRobotLaunchConfiguration robotConfig = getDefaultScriptRobotLaunchConfiguration();
        robotConfig.setRemotePortValue("");
        assertThat(robotConfig.getRemotePort().isPresent()).isFalse();
    }

    @Test
    public void whenPortIsNotANumber_coreExceptionIsThrown() throws CoreException {
        thrown.expect(CoreException.class);
        thrown.expectMessage("Server port 'abc' must be an Integer between 1 and 65,535");

        final ScriptRobotLaunchConfiguration robotConfig = getDefaultScriptRobotLaunchConfiguration();
        robotConfig.setRemotePortValue("abc");
        robotConfig.getRemotePort();
    }

    @Test
    public void whenPortIsBelowRange_coreExceptionIsThrown() throws CoreException {
        thrown.expect(CoreException.class);
        thrown.expectMessage("Server port '0' must be an Integer between 1 and 65,535");

        final ScriptRobotLaunchConfiguration robotConfig = getDefaultScriptRobotLaunchConfiguration();
        robotConfig.setRemotePortValue("0");
        robotConfig.getRemotePort();
    }

    @Test
    public void whenPortIsAboveRange_coreExceptionIsThrown() throws CoreException {
        thrown.expect(CoreException.class);
        thrown.expectMessage("Server port '65536' must be an Integer between 1 and 65,535");

        final ScriptRobotLaunchConfiguration robotConfig = getDefaultScriptRobotLaunchConfiguration();
        robotConfig.setRemotePortValue("65536");
        robotConfig.getRemotePort();
    }

    @Test
    public void whenTimeoutIsEmpty_emptyOptionalIsReturned() throws CoreException {
        final ScriptRobotLaunchConfiguration robotConfig = getDefaultScriptRobotLaunchConfiguration();
        robotConfig.setRemoteTimeoutValue("");
        assertThat(robotConfig.getRemoteTimeout().isPresent()).isFalse();
    }

    @Test
    public void whenTimeoutIsNotANumber_coreExceptionIsThrown() throws CoreException {
        thrown.expect(CoreException.class);
        thrown.expectMessage("Connection timeout 'abc' must be an Integer between 1 and 3,600");

        final ScriptRobotLaunchConfiguration robotConfig = getDefaultScriptRobotLaunchConfiguration();
        robotConfig.setRemoteTimeoutValue("abc");
        robotConfig.getRemoteTimeout();
    }

    @Test
    public void whenTimeoutIsBelowRange_coreExceptionIsThrown() throws CoreException {
        thrown.expect(CoreException.class);
        thrown.expectMessage("Connection timeout '0' must be an Integer between 1 and 3,600");

        final ScriptRobotLaunchConfiguration robotConfig = getDefaultScriptRobotLaunchConfiguration();
        robotConfig.setRemoteTimeoutValue("0");
        robotConfig.getRemoteTimeout();
    }

    @Test
    public void whenTimeoutIsAboveRange_coreExceptionIsThrown() throws CoreException {
        thrown.expect(CoreException.class);
        thrown.expectMessage("Connection timeout '3601' must be an Integer between 1 and 3,600");

        final ScriptRobotLaunchConfiguration robotConfig = getDefaultScriptRobotLaunchConfiguration();
        robotConfig.setRemoteTimeoutValue("3601");
        robotConfig.getRemoteTimeout();
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

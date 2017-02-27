/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.launch;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Collections;

import org.eclipse.core.resources.IResource;
import org.junit.ClassRule;
import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.launch.RobotLaunchConfigurationNaming.RobotLaunchConfigurationType;
import org.robotframework.red.junit.ProjectProvider;

public class RobotLaunchConfigurationNamingTest {

    private final static String PROJECT_NAME = RobotLaunchConfigurationNamingTest.class.getSimpleName();

    @ClassRule
    public static ProjectProvider projectProvider1 = new ProjectProvider(PROJECT_NAME + "1");

    @ClassRule
    public static ProjectProvider projectProvider2 = new ProjectProvider(PROJECT_NAME + "2");

    @Test
    public void newConfigurationNameIsReturned_forEmptyResources() throws Exception {
        final String namePrefix1 = RobotLaunchConfigurationNaming.getNamePrefix(Collections.emptyList(),
                RobotLaunchConfigurationType.GENERAL_PURPOSE);
        assertThat(namePrefix1).isEqualTo("New Configuration");
        final String namePrefix2 = RobotLaunchConfigurationNaming.getNamePrefix(Collections.emptyList(),
                RobotLaunchConfigurationType.SELECTED_TEST_CASES);
        assertThat(namePrefix2).isEqualTo("New Configuration");
    }

    @Test
    public void resourceConfigurationNameWithoutSuffixIsReturned_forSingleResource() throws Exception {
        final IResource resource = projectProvider1.createFile("single.robot");
        final String namePrefix = RobotLaunchConfigurationNaming.getNamePrefix(Collections.singletonList(resource),
                RobotLaunchConfigurationType.GENERAL_PURPOSE);
        assertThat(namePrefix).isEqualTo("single.robot");
    }

    @Test
    public void resourceConfigurationNameWithSuffixIsReturned_forSingleResource() throws Exception {
        final IResource resource = projectProvider1.createFile("single.robot");
        final String namePrefix = RobotLaunchConfigurationNaming.getNamePrefix(Collections.singletonList(resource),
                RobotLaunchConfigurationType.SELECTED_TEST_CASES);
        assertThat(namePrefix).isEqualTo("single.robot (Selected Test Cases)");
    }

    @Test
    public void projectConfigurationNameWithoutSuffixIsReturned_forResourcesFromOneProject() throws Exception {
        final IResource resource1 = projectProvider1.createFile("first.robot");
        final IResource resource2 = projectProvider1.createFile("second.robot");
        final String namePrefix = RobotLaunchConfigurationNaming.getNamePrefix(Arrays.asList(resource1, resource2),
                RobotLaunchConfigurationType.GENERAL_PURPOSE);
        assertThat(namePrefix).isEqualTo("RobotLaunchConfigurationNamingTest1");
    }

    @Test
    public void projectConfigurationNameWithSuffixIsReturned_forResourcesFromOneProject() throws Exception {
        final IResource resource1 = projectProvider1.createFile("first.robot");
        final IResource resource2 = projectProvider1.createFile("second.robot");
        final String namePrefix = RobotLaunchConfigurationNaming.getNamePrefix(Arrays.asList(resource1, resource2),
                RobotLaunchConfigurationType.SELECTED_TEST_CASES);
        assertThat(namePrefix).isEqualTo("RobotLaunchConfigurationNamingTest1 (Selected Test Cases)");
    }

    @Test
    public void newConfigurationNameIsReturned_forDifferentResourcesFromDifferentProjects() throws Exception {
        final IResource resource1 = projectProvider1.createFile("first.robot");
        final IResource resource2 = projectProvider2.createFile("second.robot");
        final String namePrefix1 = RobotLaunchConfigurationNaming.getNamePrefix(Arrays.asList(resource1, resource2),
                RobotLaunchConfigurationType.GENERAL_PURPOSE);
        assertThat(namePrefix1).isEqualTo("New Configuration");
        final String namePrefix2 = RobotLaunchConfigurationNaming.getNamePrefix(Arrays.asList(resource1, resource2),
                RobotLaunchConfigurationType.SELECTED_TEST_CASES);
        assertThat(namePrefix2).isEqualTo("New Configuration");
    }

}

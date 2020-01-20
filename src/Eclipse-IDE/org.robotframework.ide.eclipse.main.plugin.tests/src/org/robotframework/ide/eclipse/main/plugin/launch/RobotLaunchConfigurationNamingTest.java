/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.launch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.robotframework.red.junit.jupiter.ProjectExtension.createFile;

import java.util.Arrays;
import java.util.Collections;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.robotframework.ide.eclipse.main.plugin.launch.RobotLaunchConfigurationNaming.RobotLaunchConfigurationType;
import org.robotframework.red.junit.jupiter.Project;
import org.robotframework.red.junit.jupiter.ProjectExtension;

@ExtendWith(ProjectExtension.class)
public class RobotLaunchConfigurationNamingTest {

    @Project(nameSuffix = "1")
    static IProject project1;

    @Project(nameSuffix = "2")
    static IProject project2;

    @Test
    public void newConfigurationNameIsReturned_forEmptyResources() throws Exception {
        final String basicName1 = RobotLaunchConfigurationNaming.getBasicName(Collections.emptyList(),
                RobotLaunchConfigurationType.GENERAL_PURPOSE);
        assertThat(basicName1).isEqualTo("New Configuration");
        final String basicName2 = RobotLaunchConfigurationNaming.getBasicName(Collections.emptyList(),
                RobotLaunchConfigurationType.SELECTED_TEST_CASES);
        assertThat(basicName2).isEqualTo("New Configuration");
    }

    @Test
    public void resourceConfigurationNameWithoutSuffixIsReturned_forSingleResource() throws Exception {
        final IResource resource = createFile(project1, "single.robot");
        final String basicName = RobotLaunchConfigurationNaming.getBasicName(Collections.singletonList(resource),
                RobotLaunchConfigurationType.GENERAL_PURPOSE);
        assertThat(basicName).isEqualTo("single.robot");
    }

    @Test
    public void resourceConfigurationNameWithSuffixIsReturned_forSingleResource() throws Exception {
        final IResource resource = createFile(project1, "single.robot");
        final String basicName = RobotLaunchConfigurationNaming.getBasicName(Collections.singletonList(resource),
                RobotLaunchConfigurationType.SELECTED_TEST_CASES);
        assertThat(basicName).isEqualTo("single.robot (Selected Test Cases)");
    }

    @Test
    public void projectConfigurationNameWithoutSuffixIsReturned_forResourcesFromOneProject() throws Exception {
        final IResource resource1 = createFile(project1, "first.robot");
        final IResource resource2 = createFile(project1, "second.robot");
        final String basicName = RobotLaunchConfigurationNaming.getBasicName(Arrays.asList(resource1, resource2),
                RobotLaunchConfigurationType.GENERAL_PURPOSE);
        assertThat(basicName).isEqualTo("RobotLaunchConfigurationNamingTest1");
    }

    @Test
    public void projectConfigurationNameWithSuffixIsReturned_forResourcesFromOneProject() throws Exception {
        final IResource resource1 = createFile(project1, "first.robot");
        final IResource resource2 = createFile(project1, "second.robot");
        final String basicName = RobotLaunchConfigurationNaming.getBasicName(Arrays.asList(resource1, resource2),
                RobotLaunchConfigurationType.SELECTED_TEST_CASES);
        assertThat(basicName).isEqualTo("RobotLaunchConfigurationNamingTest1 (Selected Test Cases)");
    }

    @Test
    public void newConfigurationNameIsReturned_forDifferentResourcesFromDifferentProjects() throws Exception {
        final IResource resource1 = createFile(project1, "first.robot");
        final IResource resource2 = createFile(project2, "second.robot");
        final String basicName1 = RobotLaunchConfigurationNaming.getBasicName(Arrays.asList(resource1, resource2),
                RobotLaunchConfigurationType.GENERAL_PURPOSE);
        assertThat(basicName1).isEqualTo("New Configuration");
        final String basicName2 = RobotLaunchConfigurationNaming.getBasicName(Arrays.asList(resource1, resource2),
                RobotLaunchConfigurationType.SELECTED_TEST_CASES);
        assertThat(basicName2).isEqualTo("New Configuration");
    }

}

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
import org.robotframework.red.junit.ProjectProvider;

public class RobotLaunchConfigurationNamingTest {

    private final static String PROJECT_NAME = RobotLaunchConfigurationNamingTest.class.getSimpleName();

    @ClassRule
    public static ProjectProvider projectProvider1 = new ProjectProvider(PROJECT_NAME + "1");

    @ClassRule
    public static ProjectProvider projectProvider2 = new ProjectProvider(PROJECT_NAME + "2");

    @Test
    public void newConfigurationNameIsReturned_forEmptyResources() throws Exception {
        final String namePrefix = RobotLaunchConfigurationNaming.getNamePrefix(Collections.emptyList(), "xyz");
        assertThat(namePrefix).isEqualTo("New Configuration");
    }

    @Test
    public void resourceConfigurationNameWithSuffixIsReturned_forSingleResource() throws Exception {
        final IResource resource = projectProvider1.createFile("single.robot");
        final String suffix = RobotLaunchConfigurationNaming.SELECTED_TEST_CASES_SUFFIX;
        final String namePrefix = RobotLaunchConfigurationNaming.getNamePrefix(Collections.singletonList(resource),
                suffix);
        assertThat(namePrefix).isEqualTo("single.robot (Selected Test Cases)");
    }

    @Test
    public void projectConfigurationNameWithSuffixIsReturned_forResourcesFromOneProject() throws Exception {
        final IResource resource1 = projectProvider1.createFile("first.robot");
        final IResource resource2 = projectProvider1.createFile("second.robot");
        final String suffix = " some suffix";
        final String namePrefix = RobotLaunchConfigurationNaming.getNamePrefix(Arrays.asList(resource1, resource2),
                suffix);
        assertThat(namePrefix).isEqualTo("RobotLaunchConfigurationNamingTest1 some suffix");
    }

    @Test
    public void newConfigurationNameIsReturned_forDifferentResourcesFromDifferentProjects() throws Exception {
        final IResource resource1 = projectProvider1.createFile("first.robot");
        final IResource resource2 = projectProvider2.createFile("second.robot");
        final String namePrefix = RobotLaunchConfigurationNaming.getNamePrefix(Arrays.asList(resource1, resource2), "");
        assertThat(namePrefix).isEqualTo("New Configuration");
    }

}

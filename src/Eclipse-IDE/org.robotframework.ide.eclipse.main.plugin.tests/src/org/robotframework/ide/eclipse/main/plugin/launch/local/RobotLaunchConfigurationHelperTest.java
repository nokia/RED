/*
 * Copyright 2020 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.launch.local;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.robotframework.red.junit.jupiter.Project;
import org.robotframework.red.junit.jupiter.ProjectExtension;
import org.robotframework.red.junit.jupiter.StatefulProject;

@ExtendWith({ ProjectExtension.class })
public class RobotLaunchConfigurationHelperTest {

    @Project()
    public static StatefulProject project;

    @Test
    public void topLevelSuiteNameIsCreatedFromDataSources_whenRobotArgumentsDoNotContainNameArgument()
            throws CoreException {
        final List<IResource> dataSources = new ArrayList<>();
        final List<String> robotArguments = new ArrayList<>();
        dataSources.add(project.getProject());
        dataSources.add(project.getFile("LinkedSuite"));
        robotArguments.addAll(newArrayList("-s", "Suite.robot"));

        assertThat(RobotLaunchConfigurationHelper.createTopLevelSuiteName(dataSources, robotArguments))
                .isEqualTo(project.getProject().getName() + " & " + project.getFile("LinkedSuite").getName());
    }

    @Test
    public void topLevelSuiteNameIsCreatedFromDataSources_whenRobotArgumentsAreEmpty() throws CoreException {
        final List<IResource> dataSources = new ArrayList<>();
        final List<String> robotArguments = new ArrayList<>();
        dataSources.add(project.getProject());
        dataSources.add(project.getFile("LinkedSuite"));

        assertThat(RobotLaunchConfigurationHelper.createTopLevelSuiteName(dataSources, robotArguments))
                .isEqualTo(project.getProject().getName() + " & " + project.getFile("LinkedSuite").getName());
    }

    @Test
    public void topLevelSuiteNameIsCreatedFromDataSources_whenRobotArgumentsContainEmptyNameArgument_1()
            throws CoreException {
        final List<IResource> dataSources = new ArrayList<>();
        final List<String> robotArguments = new ArrayList<>();
        dataSources.add(project.getProject());
        dataSources.add(project.getFile("LinkedSuite.robot"));
        robotArguments.addAll(newArrayList("-s", "LinkedSuite.robot", "--name"));

        assertThat(RobotLaunchConfigurationHelper.createTopLevelSuiteName(dataSources, robotArguments))
                .isEqualTo(project.getProject().getName() + " & " + project.getFile("LinkedSuite").getName());
    }

    @Test
    public void topLevelSuiteNameIsCreatedFromDataSources_whenRobotArgumentsContainEmptyNameArgument_2()
            throws CoreException {
        final List<IResource> dataSources = new ArrayList<>();
        final List<String> robotArguments = new ArrayList<>();
        dataSources.add(project.getProject());
        dataSources.add(project.getFile("LinkedSuite.robot"));
        robotArguments.addAll(newArrayList("--name", "-s", "LinkedSuite.robot"));

        assertThat(RobotLaunchConfigurationHelper.createTopLevelSuiteName(dataSources, robotArguments))
                .isEqualTo(project.getProject().getName() + " & " + project.getFile("LinkedSuite").getName());
    }

    @Test
    public void topLevelSuiteNameIsNotCreatedFromSingleDataSource_whenRobotArgumentsDoNotContainNameArgument()
            throws CoreException {
        final List<IResource> dataSources = new ArrayList<>();
        final List<String> robotArguments = new ArrayList<>();
        dataSources.add(project.getProject());
        robotArguments.addAll(newArrayList("-s", "Suite.robot"));

        assertThat(RobotLaunchConfigurationHelper.createTopLevelSuiteName(dataSources, robotArguments)).isEmpty();
    }

    @Test
    public void topLevelSuiteNameIsCreatedFromNameArgument_whenRobotArgumentsContainNameArgument_1()
            throws CoreException {
        final List<IResource> dataSources = new ArrayList<>();
        final List<String> robotArguments = new ArrayList<>();
        dataSources.add(project.getProject());
        robotArguments.addAll(newArrayList("--name", "OtherName", "-s", "Suite.robot"));

        assertThat(RobotLaunchConfigurationHelper.createTopLevelSuiteName(dataSources, robotArguments))
                .isEqualTo("OtherName");
    }

    @Test
    public void topLevelSuiteNameIsCreatedFromNameArgument_whenRobotArgumentsContainNameArgument_2()
            throws CoreException {
        final List<IResource> dataSources = new ArrayList<>();
        final List<String> robotArguments = new ArrayList<>();
        dataSources.add(project.getProject());
        robotArguments.addAll(newArrayList("-N", "OtherName", "-s", "Suite.robot"));

        assertThat(RobotLaunchConfigurationHelper.createTopLevelSuiteName(dataSources, robotArguments))
                .isEqualTo("OtherName");
    }

    @Test
    public void topLevelSuiteNameIsCreatedFromNameArgument_whenRobotArgumentsContainNameArgumentAndThereAreTwoDataSources()
            throws CoreException {
        final List<IResource> dataSources = new ArrayList<>();
        final List<String> robotArguments = new ArrayList<>();
        dataSources.add(project.getProject());
        dataSources.add(project.getFile("LinkedSuite.robot"));
        robotArguments.addAll(newArrayList("-s", "LinkedSuite.robot", "--name", "Other Name"));

        assertThat(RobotLaunchConfigurationHelper.createTopLevelSuiteName(dataSources, robotArguments))
                .isEqualTo("Other Name");
    }

    @Test
    public void topLevelSuiteNameIsCreatedFromLastNameArgument_whenRobotArgumentsContainDuplicatedNameArgument()
            throws CoreException {
        final List<IResource> dataSources = new ArrayList<>();
        final List<String> robotArguments = new ArrayList<>();
        dataSources.add(project.getProject());
        robotArguments.addAll(newArrayList("--name", "OtherName", "--name", "SecondOtherName"));

        assertThat(RobotLaunchConfigurationHelper.createTopLevelSuiteName(dataSources, robotArguments))
                .isEqualTo("SecondOtherName");
    }
}

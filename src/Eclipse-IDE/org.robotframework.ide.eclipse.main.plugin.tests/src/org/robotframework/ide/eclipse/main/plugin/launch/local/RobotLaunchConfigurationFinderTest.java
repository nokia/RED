/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.launch.local;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.robotframework.red.junit.ProjectProvider;

public class RobotLaunchConfigurationFinderTest {

    private final static String PROJECT_NAME = RobotLaunchConfigurationFinderTest.class.getSimpleName();

    private final static List<String> resourceNames = newArrayList("Resource1.fake", "Resource2.fake",
            "Resource3.fake");

    private static IProject project;

    private static List<IResource> resources;

    @ClassRule
    public static ProjectProvider projectProvider = new ProjectProvider(PROJECT_NAME);

    @BeforeClass
    public static void createNeededResources() throws CoreException, IOException, ClassNotFoundException {
        resources = new ArrayList<>();
        project = projectProvider.getProject();
        for (final String name : resourceNames) {
            resources.add(projectProvider.createFile(name, ""));
        }
        // This line does not affect runtime, but is necessary for using breakpoints when debugging
        // this class
        // Class.forName("org.eclipse.debug.core.ILaunchConfigurationWorkingCopy");
    }

    @Before
    public void removeAllConfigurations() throws CoreException {
        final ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();
        final ILaunchConfigurationType type = manager.getLaunchConfigurationType(RobotLaunchConfiguration.TYPE_ID);
        final ILaunchConfiguration[] launchConfigs = manager.getLaunchConfigurations(type);
        for (final ILaunchConfiguration config : launchConfigs) {
            config.delete();
        }
    }

    @Test
    public void nullConfigurationIsReturned_whenThereIsNoConfiguration() throws CoreException {
        ILaunchConfiguration config = RobotLaunchConfigurationFinder.findLaunchConfiguration(resources);
        assertThat(config).isNull();
        config = RobotLaunchConfigurationFinder.findLaunchConfigurationSelectedTestCases(resources);
        assertThat(config).isNull();
    }

    @Test
    public void configurationIsReturned_whenThereIsValidConfiguration() throws CoreException {
        final ILaunchConfigurationWorkingCopy configuration = createDefault(resources);
        final ILaunchConfiguration config = RobotLaunchConfigurationFinder.findLaunchConfiguration(resources);
        assertThat(config).isNotNull();
        assertThat(config).isEqualToIgnoringGivenFields(configuration, "fOriginal");

    }

    @Test
    public void configurationForSelectedTestCasesIsReturned_whenThereIsValidConfiguration() throws CoreException {
        final IResource res = resources.get(0);
        final List<IResource> resources = newArrayList(res);
        final Map<IResource, List<String>> resourcesToTestCases = new HashMap<>();
        resourcesToTestCases.put(res, newArrayList("t1", "t3"));
        final ILaunchConfigurationWorkingCopy configuration = RobotLaunchConfiguration
                .prepareForSelectedTestCases(resourcesToTestCases);
        configuration.doSave();
        final ILaunchConfiguration config = RobotLaunchConfigurationFinder.findLaunchConfiguration(resources);
        assertThat(config).isNotNull();
        assertThat(config).isEqualToIgnoringGivenFields(configuration, "fOriginal");
    }

    @Test
    public void nullConfigurationIsReturned_whenThereIsConfigurationButNotForSelectedTestCases() throws CoreException {
        final IResource res = resources.get(0);
        final List<IResource> resources = newArrayList(res);
        createDefault(resources);
        final ILaunchConfiguration config = RobotLaunchConfigurationFinder
                .findLaunchConfigurationSelectedTestCases(resources);
        assertThat(config).isNull();
    }

    @Test
    public void configurationIsReturned_whenThereIsValidConfigurationForProject() throws CoreException {
        final IResource res = project;
        final List<IResource> resources = newArrayList(res);
        final ILaunchConfigurationWorkingCopy configuration = createDefault(resources);
        final ILaunchConfiguration config = RobotLaunchConfigurationFinder.findLaunchConfiguration(resources);
        assertThat(config).isNotNull();
        assertThat(config).isEqualToIgnoringGivenFields(configuration, "fOriginal");
    }

    @Test
    public void nullConfigurationIsReturned_whenThereIsNoConfigurationForThisProject() throws CoreException {
        final IResource res = project;
        List<IResource> resources = newArrayList(res);
        createDefault(resources);
        final IProject anotherProject = ResourcesPlugin.getWorkspace().getRoot().getProject("Another one");
        if (anotherProject.exists()) {
            anotherProject.delete(false, null);
        }
        anotherProject.create(null);
        anotherProject.open(null);
        resources = newArrayList((IResource) anotherProject);
        final ILaunchConfiguration config = RobotLaunchConfigurationFinder.findLaunchConfiguration(resources);
        assertThat(config).isNull();
        if (anotherProject.exists()) {
            anotherProject.delete(false, null);
        }
    }

    @Test
    public void configurationForProjectForSelectedTestCasesIsReturned_whenThereIsValidConfiguration()
            throws CoreException {
        final IResource res = project;
        final List<IResource> resources = newArrayList(res);
        final Map<IResource, List<String>> resourcesToTestCases = new HashMap<>();
        resourcesToTestCases.put(res, newArrayList("t1", "t3"));
        final ILaunchConfigurationWorkingCopy configTemp = RobotLaunchConfigurationFinder
                .findLaunchConfigurationSelectedTestCases(resources);
        assertThat(configTemp).isNull();
        final ILaunchConfigurationWorkingCopy configuration = RobotLaunchConfiguration
                .prepareForSelectedTestCases(resourcesToTestCases);
        configuration.doSave();
        final ILaunchConfigurationWorkingCopy config = RobotLaunchConfigurationFinder
                .findLaunchConfigurationSelectedTestCases(resources);
        assertThat(config).isNotNull();
        assertThat(config).isEqualToIgnoringGivenFields(configuration, "fOriginal");
    }

    @Test
    public void nullConfigurationIsReturned_whenThereIsOnlySelectedTestCasesConfiguration() throws CoreException {
        final IResource res = resources.get(0);
        final List<IResource> resources = newArrayList(res);
        final Map<IResource, List<String>> resourcesToTestCases = new HashMap<>();
        resourcesToTestCases.put(res, newArrayList("t1", "t3"));
        final ILaunchConfigurationWorkingCopy configuration = RobotLaunchConfiguration
                .prepareForSelectedTestCases(resourcesToTestCases);
        configuration.doSave();
        final ILaunchConfiguration config = RobotLaunchConfigurationFinder
                .findLaunchConfigurationExceptSelectedTestCases(resources);
        assertThat(config).isNull();
    }

    @Test
    public void configurationForProjectExceptSelectedTestCasesIsReturned_whenThereIsValidConfiguration()
            throws CoreException {
        final IResource res = project;
        final List<IResource> resources = newArrayList(res);
        final ILaunchConfigurationWorkingCopy configuration = createDefault(resources);
        final ILaunchConfiguration config = RobotLaunchConfigurationFinder
                .findLaunchConfigurationExceptSelectedTestCases(resources);
        assertThat(config).isNotNull();
        assertThat(config).isEqualToIgnoringGivenFields(configuration, "fOriginal");
    }

    @Test
    public void configurationForSelectedTestCasesIsReturned_whenThereIsOnlyConfigurationForSelectedTestCases()
            throws CoreException {
        final IResource res = project;
        final List<IResource> resources = newArrayList(res);
        final Map<IResource, List<String>> resourcesToTestCases = new HashMap<>();
        resourcesToTestCases.put(res, newArrayList("t1", "t3"));
        final ILaunchConfigurationWorkingCopy configuration = RobotLaunchConfiguration
                .prepareForSelectedTestCases(resourcesToTestCases);
        configuration.doSave();
        final ILaunchConfiguration config = RobotLaunchConfigurationFinder
                .findLaunchConfigurationSelectedTestCases(resources);
        assertThat(config).isNotNull();
        assertThat(config).isEqualToIgnoringGivenFields(configuration, "fOriginal");
    }

    @Test
    public void configurationReturned_whenThereIsValidConfiguration() throws CoreException {
        final IResource res = resources.get(0);
        final List<IResource> resources = newArrayList(res);
        final ILaunchConfigurationWorkingCopy configuration = createDefault(resources);
        final ILaunchConfiguration config = RobotLaunchConfigurationFinder
                .findLaunchConfigurationExceptSelectedTestCases(resources);
        assertThat(config).isNotNull();
        assertThat(config).isEqualToIgnoringGivenFields(configuration, "fOriginal");
    }

    private static ILaunchConfigurationWorkingCopy createDefault(final List<IResource> resources) throws CoreException {
        final ILaunchConfigurationWorkingCopy configuration = RobotLaunchConfiguration.prepareDefault(resources);
        configuration.doSave();
        return configuration;
    }
}

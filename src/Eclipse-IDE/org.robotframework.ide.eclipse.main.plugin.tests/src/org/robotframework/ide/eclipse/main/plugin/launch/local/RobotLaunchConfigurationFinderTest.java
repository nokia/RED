/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.launch.local;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.ArrayList;
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

import com.google.common.collect.ImmutableMap;

public class RobotLaunchConfigurationFinderTest {

    private static final String PROJECT_NAME = RobotLaunchConfigurationFinderTest.class.getSimpleName();

    private static final List<String> RESOURCE_NAMES = asList("Resource1.fake", "Resource2.fake", "Resource3.fake");

    private static final List<String> TEST_CASES = asList("t1", "t3");

    private static IProject project;

    private static List<IResource> resources;

    @ClassRule
    public static ProjectProvider projectProvider = new ProjectProvider(PROJECT_NAME);

    @BeforeClass
    public static void createNeededResources() throws CoreException, IOException, ClassNotFoundException {
        resources = new ArrayList<>();
        project = projectProvider.getProject();
        for (final String name : RESOURCE_NAMES) {
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
        final ILaunchConfiguration config = RobotLaunchConfigurationFinder.findLaunchConfiguration(resources);

        assertThat(config).isNull();
    }

    @Test
    public void nullConfigurationIsReturned_whenThereIsNoConfigurationForSelectedTestCases() throws CoreException {
        final ILaunchConfiguration config = RobotLaunchConfigurationFinder
                .findLaunchConfigurationForSelectedTestCases(resources);

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
        final List<IResource> resources = asList(res);
        final Map<IResource, List<String>> resourcesToTestCases = ImmutableMap.of(res, TEST_CASES);
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
        final List<IResource> resources = asList(res);
        createDefault(resources);
        final ILaunchConfiguration config = RobotLaunchConfigurationFinder
                .findLaunchConfigurationForSelectedTestCases(resources);

        assertThat(config).isNull();
    }

    @Test
    public void configurationIsReturned_whenThereIsValidConfigurationForProject() throws CoreException {
        final IResource res = project;
        final List<IResource> resources = asList(res);
        final ILaunchConfigurationWorkingCopy configuration = createDefault(resources);
        final ILaunchConfiguration config = RobotLaunchConfigurationFinder.findLaunchConfiguration(resources);

        assertThat(config).isNotNull();
        assertThat(config).isEqualToIgnoringGivenFields(configuration, "fOriginal");
    }

    @Test
    public void nullConfigurationIsReturned_whenThereIsNoConfigurationForThisProject() throws CoreException {
        final IResource res = project;
        List<IResource> resources = asList(res);
        createDefault(resources);
        final IProject anotherProject = ResourcesPlugin.getWorkspace().getRoot().getProject("Another one");
        if (anotherProject.exists()) {
            anotherProject.delete(false, null);
        }
        anotherProject.create(null);
        anotherProject.open(null);
        resources = asList((IResource) anotherProject);
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
        final List<IResource> resources = asList(res);
        final Map<IResource, List<String>> resourcesToTestCases = ImmutableMap.of(res, TEST_CASES);
        final ILaunchConfigurationWorkingCopy configTemp = RobotLaunchConfigurationFinder
                .findLaunchConfigurationForSelectedTestCases(resources);
        assertThat(configTemp).isNull();
        final ILaunchConfigurationWorkingCopy configuration = RobotLaunchConfiguration
                .prepareForSelectedTestCases(resourcesToTestCases);
        configuration.doSave();
        final ILaunchConfigurationWorkingCopy config = RobotLaunchConfigurationFinder
                .findLaunchConfigurationForSelectedTestCases(resources);

        assertThat(config).isNotNull();
        assertThat(config).isEqualToIgnoringGivenFields(configuration, "fOriginal");
    }

    @Test
    public void nullConfigurationIsReturned_whenThereIsOnlySelectedTestCasesConfiguration() throws CoreException {
        final IResource res = resources.get(0);
        final List<IResource> resources = asList(res);
        final Map<IResource, List<String>> resourcesToTestCases = ImmutableMap.of(res, TEST_CASES);
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
        final List<IResource> resources = asList(res);
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
        final List<IResource> resources = asList(res);
        final Map<IResource, List<String>> resourcesToTestCases = ImmutableMap.of(res, TEST_CASES);
        final ILaunchConfigurationWorkingCopy configuration = RobotLaunchConfiguration
                .prepareForSelectedTestCases(resourcesToTestCases);
        configuration.doSave();
        final ILaunchConfiguration config = RobotLaunchConfigurationFinder
                .findLaunchConfigurationForSelectedTestCases(resources);

        assertThat(config).isNotNull();
        assertThat(config).isEqualToIgnoringGivenFields(configuration, "fOriginal");
    }

    @Test
    public void configurationReturned_whenThereIsValidConfiguration() throws CoreException {
        final IResource res = resources.get(0);
        final List<IResource> resources = asList(res);
        final ILaunchConfigurationWorkingCopy configuration = createDefault(resources);
        final ILaunchConfiguration config = RobotLaunchConfigurationFinder
                .findLaunchConfigurationExceptSelectedTestCases(resources);

        assertThat(config).isNotNull();
        assertThat(config).isEqualToIgnoringGivenFields(configuration, "fOriginal");
    }

    @Test
    public void configurationSuitableForResources_whenApplicable() throws CoreException, IOException {
        final IResource res = resources.get(0);
        final List<IResource> resources = asList(res);
        final ILaunchConfigurationWorkingCopy configuration = RobotLaunchConfiguration.prepareDefault(resources);
        final RobotLaunchConfiguration robotConfig = new RobotLaunchConfiguration(configuration);

        assertThat(RobotLaunchConfigurationFinder.isSuitableFor(robotConfig, resources)).isTrue();
    }

    @Test
    public void configurationNotSuitableForResources_whenNotApplicable() throws CoreException, IOException {
        final IResource res = resources.get(0);
        final IResource anotherRes = resources.get(1);
        final List<IResource> resources = asList(res);
        final List<IResource> anotherResources = asList(res, anotherRes);
        final ILaunchConfigurationWorkingCopy configuration = RobotLaunchConfiguration.prepareDefault(resources);
        final RobotLaunchConfiguration robotConfig = new RobotLaunchConfiguration(configuration);

        assertThat(RobotLaunchConfigurationFinder.isSuitableFor(robotConfig, anotherResources)).isFalse();
    }

    @Test
    public void defaultConfigurationReturned_whenThereIsNoConfiguration() throws CoreException, IOException {
        final ILaunchConfigurationWorkingCopy configuration = RobotLaunchConfigurationFinder
                .getLaunchConfiguration(resources);
        final ILaunchConfigurationWorkingCopy defaultConfig = RobotLaunchConfiguration.prepareDefault(resources);

        assertThat(configuration).isNotNull();
        assertThat(configuration.exists()).isFalse();
        assertThat(configuration).isEqualToComparingFieldByField(defaultConfig);
    }

    @Test
    public void configurationReturned_whenThereIsApplicableConfiguration() throws CoreException {
        final ILaunchConfigurationWorkingCopy configuration = createDefault(resources);
        new RobotLaunchConfiguration(configuration).setInterpreterArguments("custom");
        configuration.doSave();
        final ILaunchConfigurationWorkingCopy config = RobotLaunchConfigurationFinder.getLaunchConfiguration(resources);

        assertThat(configuration).isEqualToIgnoringGivenFields(config, "fOriginal");
    }

    @Test
    public void defaultConfigurationReturned_whenThereIsOnlyConfigurationForSelectedTestCases() throws CoreException {
        final IResource res = project;
        final List<IResource> resources = asList(res);
        final Map<IResource, List<String>> resourcesToTestCases = ImmutableMap.of(res, TEST_CASES);
        final ILaunchConfigurationWorkingCopy configuration = RobotLaunchConfiguration
                .prepareForSelectedTestCases(resourcesToTestCases);
        configuration.doSave();
        final ILaunchConfiguration config = RobotLaunchConfigurationFinder
                .getLaunchConfigurationExceptSelectedTestCases(resources);
        final ILaunchConfigurationWorkingCopy defaultConfig = RobotLaunchConfiguration.prepareDefault(resources);

        assertThat(config).isNotNull();
        assertThat(config).isNotEqualTo(configuration);
        assertThat(config).isEqualToComparingFieldByField(defaultConfig);
    }

    @Test
    public void configurationReturned_whenThereIsApplicableConfigurationForGeneralPurpose() throws CoreException {
        final ILaunchConfigurationWorkingCopy configuration = createDefault(resources);
        new RobotLaunchConfiguration(configuration).setInterpreterArguments("custom");
        configuration.doSave();
        final ILaunchConfiguration config = RobotLaunchConfigurationFinder
                .getLaunchConfigurationExceptSelectedTestCases(resources);

        assertThat(config).isEqualToIgnoringGivenFields(configuration, "fOriginal");
    }

    @Test
    public void defaultConfigurationReturned_whenThereIsNoConfigurationForSelectedTestCases() throws CoreException {
        final IResource res = project;
        final Map<IResource, List<String>> resourcesToTestCases = ImmutableMap.of(res, TEST_CASES);
        final ILaunchConfigurationWorkingCopy configuration = RobotLaunchConfigurationFinder
                .getLaunchConfigurationForSelectedTestCases(resourcesToTestCases);
        final ILaunchConfigurationWorkingCopy defaultConfig = RobotLaunchConfiguration
                .prepareForSelectedTestCases(resourcesToTestCases);

        assertThat(configuration).isEqualToComparingFieldByField(defaultConfig);
    }

    @Test
    public void configurationReturned_whenThereIsApplicableConfigurationForSelectedTestCases() throws CoreException {
        final IResource res = project;
        final Map<IResource, List<String>> resourcesToTestCases = ImmutableMap.of(res, TEST_CASES);
        final ILaunchConfigurationWorkingCopy config = RobotLaunchConfiguration
                .prepareForSelectedTestCases(resourcesToTestCases);
        new RobotLaunchConfiguration(config).setInterpreterArguments("custom");
        config.doSave();
        final ILaunchConfigurationWorkingCopy configuration = RobotLaunchConfigurationFinder
                .getLaunchConfigurationForSelectedTestCases(resourcesToTestCases);

        assertThat(configuration).isEqualToIgnoringGivenFields(config, "fOriginal");
    }

    @Test
    public void configurationReturned_whenThereIsExactlySameConfiguration() throws CoreException {
        final ILaunchConfigurationWorkingCopy configuration = createDefault(resources);
        new RobotLaunchConfiguration(configuration).setInterpreterArguments("custom");
        configuration.doSave();
        final ILaunchConfigurationWorkingCopy foundConfig = RobotLaunchConfigurationFinder.findSameAs(configuration);

        assertThat(foundConfig).isEqualToIgnoringGivenFields(configuration, "fOriginal");
    }

    @Test
    public void noConfigurationReturned_whenThereIsNoExactlySameConfiguration() throws CoreException {
        final ILaunchConfigurationWorkingCopy configuration = createDefault(resources);
        new RobotLaunchConfiguration(configuration).setInterpreterArguments("custom");
        final ILaunchConfigurationWorkingCopy foundConfig = RobotLaunchConfigurationFinder.findSameAs(configuration);

        assertThat(foundConfig).isNull();
    }

    private static ILaunchConfigurationWorkingCopy createDefault(final List<IResource> resources) throws CoreException {
        final ILaunchConfigurationWorkingCopy configuration = RobotLaunchConfiguration.prepareDefault(resources);
        configuration.doSave();
        return configuration;
    }
}

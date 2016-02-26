/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.launch;

import static com.google.common.collect.Lists.newArrayList;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.rf.ide.core.executor.SuiteExecutor;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

@SuppressWarnings("PMD.GodClass")
public class RobotLaunchConfiguration {

    static final String TYPE_ID = "org.robotframework.ide.robotLaunchConfiguration";

    private static final String EXECUTOR_NAME = "Executor";
    private static final String EXECUTOR_ARGUMENTS_ATTRIBUTE = "Executor arguments";
    private static final String INCLUDE_TAGS_OPTION_ENABLED_ATTRIBUTE = "Include option enabled";
    private static final String INCLUDED_TAGS_ATTRIBUTE = "Included tags";
    private static final String EXCLUDE_TAGS_OPTION_ENABLED_ATTRIBUTE = "Exclude option enabled";
    private static final String EXCLUDED_TAGS_ATTRIBUTE = "Excluded tags";
    private static final String PROJECT_NAME_ATTRIBUTE = "Project name";
    private static final String TEST_SUITES_ATTRIBUTE = "Test suites";
    private static final String TEST_CASES_ATTRIBUTE = "Test cases";
    private static final String REMOTE_DEBUG_HOST_ATTRIBUTE = "Remote debug host";
    private static final String REMOTE_DEBUG_PORT_ATTRIBUTE = "Remote debug port";
    private static final String REMOTE_DEBUG_TIMEOUT_ATTRIBUTE = "Remote debug timeout";

    private final ILaunchConfiguration configuration;
    
    static ILaunchConfigurationWorkingCopy createDefault(final ILaunchConfigurationType launchConfigurationType,
            final List<IResource> resources) throws CoreException {

        final String name = resources.size() == 1 ? resources.get(0).getName() : resources.get(0).getProject()
                .getName();
        final String configurationName = DebugPlugin.getDefault().getLaunchManager()
                .generateLaunchConfigurationName(name);
        final ILaunchConfigurationWorkingCopy configuration = launchConfigurationType.newInstance(null,
                configurationName);
        fillDefaults(configuration, resources);
        configuration.doSave();
        return configuration;
    }

    public static void fillDefaults(final ILaunchConfigurationWorkingCopy launchConfig) {
        final RobotLaunchConfiguration robotConfig = new RobotLaunchConfiguration(launchConfig);
        robotConfig.setExecutor(SuiteExecutor.Python);
        robotConfig.setExecutorArguments("");
        robotConfig.setProjectName("");
        robotConfig.setSuitePaths(new ArrayList<String>());
        robotConfig.setTestCasesNames(new ArrayList<String>());
        robotConfig.setIsIncludeTagsEnabled(false);
        robotConfig.setIsExcludeTagsEnabled(false);
        robotConfig.setIncludedTags(new ArrayList<String>());
        robotConfig.setExcludedTags(new ArrayList<String>());
        robotConfig.setRemoteDebugHost("");
        robotConfig.setRemoteDebugPort("");
        robotConfig.setRemoteDebugTimeout("");
    }

    private static void fillDefaults(final ILaunchConfigurationWorkingCopy launchConfig, final List<IResource> resources) {
        final RobotLaunchConfiguration robotConfig = new RobotLaunchConfiguration(launchConfig);
        final IProject project = resources.get(0).getProject();
        final RobotProject robotProject = RedPlugin.getModelManager().getModel().createRobotProject(project);
        final SuiteExecutor interpreter = robotProject.getRuntimeEnvironment().getInterpreter();
        robotConfig.setExecutor(interpreter);
        robotConfig.setExecutorArguments("");
        robotConfig.setProjectName(project.getName());
        
        robotConfig.setSuitePaths(newArrayList(Lists.transform(resources, new Function<IResource, String>() {
            @Override
            public String apply(final IResource resource) {
                return resource.getProjectRelativePath().toPortableString();
            }
        })));
        robotConfig.setTestCasesNames(new ArrayList<String>());
        
        robotConfig.setIsIncludeTagsEnabled(false);
        robotConfig.setIsExcludeTagsEnabled(false);
        robotConfig.setIncludedTags(new ArrayList<String>());
        robotConfig.setExcludedTags(new ArrayList<String>());
        robotConfig.setRemoteDebugHost("");
        robotConfig.setRemoteDebugPort("");
        robotConfig.setRemoteDebugTimeout("");
    }
    
    public RobotLaunchConfiguration(final ILaunchConfiguration config) {
        this.configuration = config;
    }

    public String getName() {
        return configuration.getName();
    }

    private ILaunchConfigurationWorkingCopy asWorkingCopy() {
        return configuration instanceof ILaunchConfigurationWorkingCopy ? (ILaunchConfigurationWorkingCopy) configuration
                : null;
    }
    
    public void setExecutor(final SuiteExecutor executor) {
        final ILaunchConfigurationWorkingCopy launchCopy = asWorkingCopy();
        if (launchCopy != null) {
            launchCopy.setAttribute(EXECUTOR_NAME, executor.name());
        }
    }

    public void setExecutorArguments(final String arguments) {
        final ILaunchConfigurationWorkingCopy launchCopy = asWorkingCopy();
        if (launchCopy != null) {
            launchCopy.setAttribute(EXECUTOR_ARGUMENTS_ATTRIBUTE, arguments);
        }
    }

    public void setProjectName(final String projectName) {
        final ILaunchConfigurationWorkingCopy launchCopy = asWorkingCopy();
        if (launchCopy != null) {
            launchCopy.setAttribute(PROJECT_NAME_ATTRIBUTE, projectName);
        }
    }

    public void setSuitePaths(final List<String> names) {
        final ILaunchConfigurationWorkingCopy launchCopy = asWorkingCopy();
        if (launchCopy != null) {
            launchCopy.setAttribute(TEST_SUITES_ATTRIBUTE, names);
        }
    }
    
    public void setTestCasesNames(final List<String> names) {
        final ILaunchConfigurationWorkingCopy launchCopy = asWorkingCopy();
        if (launchCopy != null) {
            launchCopy.setAttribute(TEST_CASES_ATTRIBUTE, names);
        }
    }
    
    public void setIsIncludeTagsEnabled(final boolean isIncludeTagsEnabled) {
        final ILaunchConfigurationWorkingCopy launchCopy = asWorkingCopy();
        if (launchCopy != null) {
            launchCopy.setAttribute(INCLUDE_TAGS_OPTION_ENABLED_ATTRIBUTE, isIncludeTagsEnabled);
        }
    }
    
    public void setIncludedTags(final List<String> tags) {
        final ILaunchConfigurationWorkingCopy launchCopy = asWorkingCopy();
        if (launchCopy != null) {
            launchCopy.setAttribute(INCLUDED_TAGS_ATTRIBUTE, tags);
        }
    }
    
    public void setIsExcludeTagsEnabled(final boolean isExcludeTagsEnabled) {
        final ILaunchConfigurationWorkingCopy launchCopy = asWorkingCopy();
        if (launchCopy != null) {
            launchCopy.setAttribute(EXCLUDE_TAGS_OPTION_ENABLED_ATTRIBUTE, isExcludeTagsEnabled);
        }
    }
    
    public void setExcludedTags(final List<String> tags) {
        final ILaunchConfigurationWorkingCopy launchCopy = asWorkingCopy();
        if (launchCopy != null) {
            launchCopy.setAttribute(EXCLUDED_TAGS_ATTRIBUTE, tags);
        }
    }
    
    public void setRemoteDebugHost(final String host) {
        final ILaunchConfigurationWorkingCopy launchCopy = asWorkingCopy();
        if (launchCopy != null) {
            launchCopy.setAttribute(REMOTE_DEBUG_HOST_ATTRIBUTE, host);
        }
    }
    
    public void setRemoteDebugPort(final String port) {
        final ILaunchConfigurationWorkingCopy launchCopy = asWorkingCopy();
        if (launchCopy != null) {
            launchCopy.setAttribute(REMOTE_DEBUG_PORT_ATTRIBUTE, port);
        }
    }
    
    public void setRemoteDebugTimeout(final String timeout) {
        final ILaunchConfigurationWorkingCopy launchCopy = asWorkingCopy();
        if (launchCopy != null) {
            launchCopy.setAttribute(REMOTE_DEBUG_TIMEOUT_ATTRIBUTE, timeout);
        }
    }

    public SuiteExecutor getExecutor() throws CoreException {
        return SuiteExecutor.fromName(configuration.getAttribute(EXECUTOR_NAME, SuiteExecutor.Python.name()));
    }

    public String getExecutorArguments() throws CoreException {
        return configuration.getAttribute(EXECUTOR_ARGUMENTS_ATTRIBUTE, "");
    }

    public String getProjectName() throws CoreException {
        return configuration.getAttribute(PROJECT_NAME_ATTRIBUTE, "");
    }

    public List<String> getSuitePaths() throws CoreException {
        return configuration.getAttribute(TEST_SUITES_ATTRIBUTE, new ArrayList<String>());
    }
    
    public List<String> getTestCasesNames() throws CoreException {
        return configuration.getAttribute(TEST_CASES_ATTRIBUTE, new ArrayList<String>());
    }
    
    public boolean isIncludeTagsEnabled() throws CoreException {
        return configuration.getAttribute(INCLUDE_TAGS_OPTION_ENABLED_ATTRIBUTE, false);
    }
    
    public List<String> getIncludedTags() throws CoreException {
        return configuration.getAttribute(INCLUDED_TAGS_ATTRIBUTE, new ArrayList<String>());
    }
    
    public boolean isExcludeTagsEnabled() throws CoreException {
        return configuration.getAttribute(EXCLUDE_TAGS_OPTION_ENABLED_ATTRIBUTE, false);
    }
    
    public List<String> getExcludedTags() throws CoreException {
        return configuration.getAttribute(EXCLUDED_TAGS_ATTRIBUTE, new ArrayList<String>());
    }
    
    public String getRemoteDebugHost() throws CoreException {
        return configuration.getAttribute(REMOTE_DEBUG_HOST_ATTRIBUTE, "");
    }
    
    public String getRemoteDebugPort() throws CoreException {
        return configuration.getAttribute(REMOTE_DEBUG_PORT_ATTRIBUTE, "");
    }
    
    public String getRemoteDebugTimeout() throws CoreException {
        return configuration.getAttribute(REMOTE_DEBUG_TIMEOUT_ATTRIBUTE, "");
    }

    public RobotProject getRobotProject() throws CoreException {
        final IProject project = getProject();
        return RedPlugin.getModelManager().getModel().createRobotProject(project);
    }

    private IProject getProject() throws CoreException {
        final String projectName = getProjectName();
        final IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
        if (!project.exists()) {
            throw newCoreException("Project '" + projectName + "' cannot be found in workspace", null);
        }
        return project;
    }

    private static CoreException newCoreException(final String message, final Throwable cause) {
        return new CoreException(new Status(IStatus.ERROR, RedPlugin.PLUGIN_ID, message, cause));
    }

    public boolean isSuitableFor(final List<IResource> resources) {
        try {
            for (final IResource resource : resources) {
                final IProject project = resource.getProject();
                if (!getProjectName().equals(project.getName())) {
                    return false;
                }
                boolean exists = false;
                for (final String path : getSuitePaths()) {
                    final IResource res = project.findMember(Path.fromPortableString(path));
                    if (res != null && res.equals(resource)) {
                        exists = true;
                        break;
                    }
                }
                if (!exists) {
                    return false;
                }
            }
            return true;
        } catch (final CoreException e) {
            return false;
        }
    }
    
    public static void prepareRerunFailedTestsConfiguration(final ILaunchConfigurationWorkingCopy launchCopy,
            final String outputFilePath) throws CoreException {
        launchCopy.setAttribute(EXECUTOR_ARGUMENTS_ATTRIBUTE, "-R" + " " + outputFilePath);
        launchCopy.setAttribute(TEST_SUITES_ATTRIBUTE, new ArrayList<String>());
        launchCopy.setAttribute(TEST_CASES_ATTRIBUTE, new ArrayList<String>());
    }
    
    public static ILaunchConfigurationWorkingCopy createLaunchConfigurationForSelectedTestCases(
            final List<IResource> resources, final List<String> testCasesNames) throws CoreException {

        final ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();
        final String configurationName = manager.generateLaunchConfigurationName("New Configuration");
        final ILaunchConfigurationWorkingCopy configuration = manager.getLaunchConfigurationType(TYPE_ID).newInstance(
                null, configurationName);
        fillDefaults(configuration, resources);

        configuration.setAttribute(TEST_CASES_ATTRIBUTE, testCasesNames);

        return configuration;
    }
}

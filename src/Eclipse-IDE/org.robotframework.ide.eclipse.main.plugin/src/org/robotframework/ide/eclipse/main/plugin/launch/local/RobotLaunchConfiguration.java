/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.launch.local;

import static com.google.common.collect.Iterables.getFirst;
import static com.google.common.collect.Lists.newArrayList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.rf.ide.core.executor.RobotRuntimeEnvironment;
import org.rf.ide.core.executor.RobotRuntimeEnvironment.RobotEnvironmentException;
import org.rf.ide.core.executor.SuiteExecutor;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences;
import org.robotframework.ide.eclipse.main.plugin.launch.AbstractRobotLaunchConfiguration;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;

public class RobotLaunchConfiguration extends AbstractRobotLaunchConfiguration {

    public static final String TYPE_ID = "org.robotframework.ide.robotLaunchConfiguration";

    private static final String USE_PROJECT_EXECUTOR = "Project executor";

    private static final String EXECUTOR_NAME = "Executor";

    private static final String EXECUTOR_ARGUMENTS_ATTRIBUTE = "Executor arguments";

    private static final String INTERPRETER_ARGUMENTS_ATTRIBUTE = "Interpreter arguments";

    private static final String GENERAL_PURPOSE_OPTION_ENABLED_ATTRIBUTE = "General purpose option enabled";

    static ILaunchConfigurationWorkingCopy prepareDefault(final ILaunchConfigurationType launchConfigurationType,
            final List<IResource> resources) throws CoreException {
        final ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();
        final String namePrefix = getLaunchConfigurationNamePrefix(resources, "");
        final String name = manager.generateLaunchConfigurationName(namePrefix);
        final Map<IResource, List<String>> suitesMapping = new HashMap<>();
        for (final IResource resource : resources) {
            suitesMapping.put(resource, new ArrayList<String>());
        }

        final ILaunchConfigurationWorkingCopy configuration = launchConfigurationType.newInstance(null, name);
        fillDefaults(configuration, suitesMapping, true);
        return configuration;
    }

    static ILaunchConfigurationWorkingCopy prepareForSelectedTestCases(final Map<IResource, List<String>> suitesMapping)
            throws CoreException {
        final ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();
        final String namePrefix = getLaunchConfigurationNamePrefix(suitesMapping.keySet(),
                RobotLaunchConfigurationFinder.SELECTED_TESTS_CONFIG_SUFFIX);
        final String name = manager.generateLaunchConfigurationName(namePrefix);

        final ILaunchConfigurationWorkingCopy configuration = manager.getLaunchConfigurationType(TYPE_ID)
                .newInstance(null, name);
        fillDefaults(configuration, suitesMapping, false);
        return configuration;
    }

    private static void fillDefaults(final ILaunchConfigurationWorkingCopy launchConfig,
            final Map<IResource, List<String>> suitesMapping, final boolean isGeneralPurposeEnabled)
            throws CoreException {
        final RobotLaunchConfiguration robotConfig = new RobotLaunchConfiguration(launchConfig);
        final IProject project = getFirst(suitesMapping.keySet(), null).getProject();
        final RobotProject robotProject = RedPlugin.getModelManager().getModel().createRobotProject(project);
        robotConfig.fillDefaults();
        if (robotProject.getRuntimeEnvironment() != null) {
            final SuiteExecutor interpreter = robotProject.getRuntimeEnvironment().getInterpreter();
            robotConfig.setExecutor(interpreter);
        }
        robotConfig.setProjectName(project.getName());
        robotConfig.updateTestCases(suitesMapping);
        robotConfig.setIsGeneralPurposeEnabled(isGeneralPurposeEnabled);
    }

    public static void fillForFailedTestsRerun(final ILaunchConfigurationWorkingCopy launchCopy,
            final String outputFilePath) throws CoreException {
        final RobotLaunchConfiguration robotConfig = new RobotLaunchConfiguration(launchCopy);
        robotConfig.setExecutorArguments("-R " + outputFilePath);
        robotConfig.setSuitePaths(new HashMap<String, List<String>>());
    }

    static String getLaunchConfigurationNamePrefix(final Collection<IResource> resources, final String suffix) {
        if (resources.size() == 1) {
            return getFirst(resources, null).getName() + suffix;
        }
        final Set<IProject> projects = new HashSet<>();
        for (final IResource res : resources) {
            if (projects.add(res.getProject())) {
                if (projects.size() > 1) {
                    break;
                }
            }
        }
        if (projects.size() == 1) {
            return getFirst(projects, null).getName() + suffix;
        }
        return "New Configuration";
    }

    static boolean contentEquals(final ILaunchConfiguration config1, final ILaunchConfiguration config2)
            throws CoreException {
        final RobotLaunchConfiguration rConfig1 = new RobotLaunchConfiguration(config1);
        final RobotLaunchConfiguration rConfig2 = new RobotLaunchConfiguration(config2);
        return rConfig1.getExecutor().equals(rConfig2.getExecutor())
                && rConfig1.getExecutorArguments().equals(rConfig2.getExecutorArguments())
                && rConfig1.getProjectName().equals(rConfig2.getProjectName())
                && rConfig1.isUsingInterpreterFromProject() == rConfig2.isUsingInterpreterFromProject()
                && rConfig1.getInterpreterArguments().equals(rConfig2.getInterpreterArguments())
                && rConfig1.isExcludeTagsEnabled() == rConfig2.isExcludeTagsEnabled()
                && rConfig1.isIncludeTagsEnabled() == rConfig2.isIncludeTagsEnabled()
                && rConfig1.isGeneralPurposeConfiguration() == rConfig2.isGeneralPurposeConfiguration()
                && rConfig1.getExcludedTags().equals(rConfig2.getExcludedTags())
                && rConfig1.getIncludedTags().equals(rConfig2.getIncludedTags())
                && rConfig1.getSuitePaths().equals(rConfig2.getSuitePaths());
    }

    public RobotLaunchConfiguration(final ILaunchConfiguration config) {
        super(config);
    }

    @Override
    public void fillDefaults() throws CoreException {
        final RedPreferences preferences = RedPlugin.getDefault().getPreferences();
        setExecutor(SuiteExecutor.Python);
        setExecutorArguments(preferences.getLaunchAdditionalRobotArguments());
        setInterpreterArguments(preferences.getLaunchAdditionalInterpreterArguments());
        setIsGeneralPurposeEnabled(true);
        super.fillDefaults();
    }

    public void setUsingInterpreterFromProject(final boolean usesProjectExecutor) throws CoreException {
        final ILaunchConfigurationWorkingCopy launchCopy = asWorkingCopy();
        launchCopy.setAttribute(USE_PROJECT_EXECUTOR, usesProjectExecutor);
    }

    public void setExecutor(final SuiteExecutor executor) throws CoreException {
        final ILaunchConfigurationWorkingCopy launchCopy = asWorkingCopy();
        launchCopy.setAttribute(EXECUTOR_NAME, executor == null ? "" : executor.name());
    }

    public void setExecutorArguments(final String arguments) throws CoreException {
        final ILaunchConfigurationWorkingCopy launchCopy = asWorkingCopy();
        launchCopy.setAttribute(EXECUTOR_ARGUMENTS_ATTRIBUTE, arguments);
    }

    public void setInterpreterArguments(final String arguments) throws CoreException {
        final ILaunchConfigurationWorkingCopy launchCopy = asWorkingCopy();
        launchCopy.setAttribute(INTERPRETER_ARGUMENTS_ATTRIBUTE, arguments);
    }

    public void setIsGeneralPurposeEnabled(final boolean isGeneralPurposeEnabled) throws CoreException {
        final ILaunchConfigurationWorkingCopy launchCopy = asWorkingCopy();
        launchCopy.setAttribute(GENERAL_PURPOSE_OPTION_ENABLED_ATTRIBUTE, isGeneralPurposeEnabled);
    }

    public boolean isGeneralPurposeConfiguration() throws CoreException {
        return configuration.getAttribute(GENERAL_PURPOSE_OPTION_ENABLED_ATTRIBUTE, false);
    }

    public boolean isUsingInterpreterFromProject() throws CoreException {
        return configuration.getAttribute(USE_PROJECT_EXECUTOR, true);
    }

    public SuiteExecutor getExecutor() throws CoreException {
        return SuiteExecutor.fromName(configuration.getAttribute(EXECUTOR_NAME, SuiteExecutor.Python.name()));
    }

    public String getExecutorArguments() throws CoreException {
        return configuration.getAttribute(EXECUTOR_ARGUMENTS_ATTRIBUTE, "");
    }

    public String getInterpreterArguments() throws CoreException {
        return configuration.getAttribute(INTERPRETER_ARGUMENTS_ATTRIBUTE, "");
    }

    boolean isSuitableFor(final List<IResource> resources) {
        try {
            for (final IResource resource : resources) {
                final IProject project = resource.getProject();
                if (!getProjectName().equals(project.getName())) {
                    return false;
                }
                boolean exists = false;
                for (final String path : getSuitePaths().keySet()) {
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

    boolean isSuitableForOnly(final List<IResource> resources) {
        try {
            final List<IResource> toCall = newArrayList();
            toCall.addAll(resources);
            final Set<String> canCall = getSuitePaths().keySet();
            if (toCall.size() != canCall.size()) {
                return false;
            }
            for (final IResource resource : resources) {
                final IProject project = resource.getProject();
                if (!getProjectName().equals(project.getName())) {
                    return false;
                }
                boolean exists = false;
                for (final String path : getSuitePaths().keySet()) {
                    final IResource res = project.findMember(Path.fromPortableString(path));
                    if (res != null && res.equals(resource)) {
                        exists = true;
                        toCall.remove(res);
                        canCall.remove(path);
                        break;
                    }
                }
                if (!exists) {
                    return false;
                }
            }
            if (toCall.size() == 0 && canCall.size() == 0) {
                return true;
            } else {
                return false;
            }
        } catch (final CoreException e) {
            return false;
        }
    }

    String createConsoleDescription(final RobotRuntimeEnvironment env) throws CoreException {
        return isUsingInterpreterFromProject() ? env.getPythonExecutablePath() : getExecutor().executableName();
    }

    String createExecutorVersion(final RobotRuntimeEnvironment env) throws RobotEnvironmentException, CoreException {
        return isUsingInterpreterFromProject() ? env.getVersion() : RobotRuntimeEnvironment.getVersion(getExecutor());
    }

}

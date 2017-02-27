/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.launch.local;

import static com.google.common.collect.Iterables.getFirst;
import static com.google.common.collect.Lists.newArrayList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.rf.ide.core.executor.RobotRuntimeEnvironment;
import org.rf.ide.core.executor.RobotRuntimeEnvironment.RobotEnvironmentException;
import org.rf.ide.core.executor.SuiteExecutor;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences;
import org.robotframework.ide.eclipse.main.plugin.launch.AbstractTaggedSuitesRobotLaunchConfiguration;
import org.robotframework.ide.eclipse.main.plugin.launch.RobotLaunchConfigurationNaming;
import org.robotframework.ide.eclipse.main.plugin.launch.RobotLaunchConfigurationNaming.RobotLaunchConfigurationType;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;

public class RobotLaunchConfiguration extends AbstractTaggedSuitesRobotLaunchConfiguration {

    public static final String TYPE_ID = "org.robotframework.ide.robotLaunchConfiguration";

    private static final String USE_PROJECT_EXECUTOR = "Project executor";

    private static final String EXECUTOR_NAME = "Executor";

    private static final String EXECUTOR_ARGUMENTS_ATTRIBUTE = "Executor arguments";

    private static final String INTERPRETER_ARGUMENTS_ATTRIBUTE = "Interpreter arguments";

    static ILaunchConfigurationWorkingCopy prepareDefault(final List<IResource> resources) throws CoreException {
        final Map<IResource, List<String>> suitesMapping = new HashMap<>();
        for (final IResource resource : resources) {
            suitesMapping.put(resource, new ArrayList<String>());
        }
        return prepareCopy(suitesMapping, RobotLaunchConfigurationType.GENERAL_PURPOSE);
    }

    static ILaunchConfigurationWorkingCopy prepareForSelectedTestCases(final Map<IResource, List<String>> suitesMapping)
            throws CoreException {
        return prepareCopy(suitesMapping, RobotLaunchConfigurationType.SELECTED_TEST_CASES);
    }

    private static ILaunchConfigurationWorkingCopy prepareCopy(final Map<IResource, List<String>> suitesMapping,
            final RobotLaunchConfigurationType type) throws CoreException {
        final ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();
        final String namePrefix = RobotLaunchConfigurationNaming.getNamePrefix(suitesMapping.keySet(), type);
        final String name = manager.generateLaunchConfigurationName(namePrefix);

        final ILaunchConfigurationWorkingCopy configuration = manager.getLaunchConfigurationType(TYPE_ID)
                .newInstance(null, name);
        fillDefaults(configuration, suitesMapping, type);
        return configuration;
    }

    private static void fillDefaults(final ILaunchConfigurationWorkingCopy launchConfig,
            final Map<IResource, List<String>> suitesMapping, final RobotLaunchConfigurationType type)
            throws CoreException {
        final RobotLaunchConfiguration robotConfig = new RobotLaunchConfiguration(launchConfig);
        robotConfig.fillDefaults();
        final IProject project = getFirst(suitesMapping.keySet(), null).getProject();
        final RobotProject robotProject = RedPlugin.getModelManager().getModel().createRobotProject(project);
        if (robotProject.getRuntimeEnvironment() != null) {
            final SuiteExecutor interpreter = robotProject.getRuntimeEnvironment().getInterpreter();
            robotConfig.setExecutor(interpreter);
        }
        robotConfig.setProjectName(project.getName());
        robotConfig.updateTestCases(suitesMapping);
        robotConfig.setIsGeneralPurposeEnabled(type == RobotLaunchConfigurationType.GENERAL_PURPOSE);
    }

    public static void fillForFailedTestsRerun(final ILaunchConfigurationWorkingCopy launchConfig,
            final String outputFilePath) throws CoreException {
        final RobotLaunchConfiguration robotConfig = new RobotLaunchConfiguration(launchConfig);
        robotConfig.setExecutorArguments("-R " + outputFilePath);
        robotConfig.setSuitePaths(new HashMap<String, List<String>>());
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

    public String[] getEnvironmentVariables() throws CoreException {
        final Map<String, String> vars = configuration.getAttribute(ILaunchManager.ATTR_ENVIRONMENT_VARIABLES,
                (Map<String, String>) null);
        if (vars == null) {
            return null;
        }
        final boolean shouldAppendVars = configuration.getAttribute(ILaunchManager.ATTR_APPEND_ENVIRONMENT_VARIABLES,
                true);
        final List<String> varMappings = new ArrayList<>();
        if (shouldAppendVars) {
            appendVariables(varMappings, System.getenv());
        }
        appendVariables(varMappings, vars);
        return varMappings.toArray(new String[0]);
    }

    private void appendVariables(final List<String> varMappings, final Map<String, String> vars) {
        for (final Entry<String, String> entry : vars.entrySet()) {
            varMappings.add(entry.getKey() + "=" + entry.getValue());
        }
    }

}

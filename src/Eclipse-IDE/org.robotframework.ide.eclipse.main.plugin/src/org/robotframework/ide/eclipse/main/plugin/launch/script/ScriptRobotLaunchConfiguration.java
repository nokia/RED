/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.launch.script;

import static com.google.common.collect.Iterables.getFirst;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.rf.ide.core.executor.RedSystemProperties;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences;
import org.robotframework.ide.eclipse.main.plugin.launch.AbstractTaggedSuitesRobotLaunchConfiguration;
import org.robotframework.ide.eclipse.main.plugin.launch.IRemoteRobotLaunchConfiguration;
import org.robotframework.ide.eclipse.main.plugin.launch.RobotLaunchConfigurationNaming;
import org.robotframework.ide.eclipse.main.plugin.launch.RobotLaunchConfigurationNaming.RobotLaunchConfigurationType;

import com.google.common.collect.Range;
import com.google.common.primitives.Ints;

public class ScriptRobotLaunchConfiguration extends AbstractTaggedSuitesRobotLaunchConfiguration
        implements IRemoteRobotLaunchConfiguration {

    public static final String TYPE_ID = "org.robotframework.ide.scriptRobotLaunchConfiguration";

    private static final String SCRIPT_PATH_ATTRIBUTE = "Script path";

    private static final String SCRIPT_ARGUMENTS_ATTRIBUTE = "Script arguments";

    private static final String SCRIPT_RUN_COMMAND_ATTRIBUTE = "Script run command";

    public static String[] getSystemDependentScriptExtensions() {
        return RedSystemProperties.isWindowsPlatform() ? new String[] { "*.bat", "*.*" }
                : new String[] { "*.sh", "*.*" };
    }

    public static String getSystemDependentScriptRunCommand() {
        return RedSystemProperties.isWindowsPlatform() ? "cmd /c start" : "";
    }

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
        final ScriptRobotLaunchConfiguration robotConfig = new ScriptRobotLaunchConfiguration(launchConfig);
        robotConfig.fillDefaults();
        final IProject project = getFirst(suitesMapping.keySet(), null).getProject();
        robotConfig.setProjectName(project.getName());
        robotConfig.updateTestCases(suitesMapping);
        robotConfig.setIsGeneralPurposeEnabled(type == RobotLaunchConfigurationType.GENERAL_PURPOSE);
    }

    public ScriptRobotLaunchConfiguration(final ILaunchConfiguration config) {
        super(config);
    }

    @Override
    public void fillDefaults() throws CoreException {
        final RedPreferences preferences = RedPlugin.getDefault().getPreferences();
        setScriptPath(preferences.getLaunchScriptPath());
        setScriptArguments(preferences.getLaunchAdditionalScriptArguments());
        setScriptRunCommand(preferences.getLaunchScriptRunCommand());
        setRemoteDebugHostValue(preferences.getLaunchRemoteHost());
        setRemoteDebugPortValue(preferences.getLaunchRemotePort());
        setRemoteDebugTimeoutValue(preferences.getLaunchRemoteTimeout());
        super.fillDefaults();
    }

    @Override
    public boolean isDefiningProjectDirectly() {
        return false;
    }

    @Override
    public String getRemoteDebugHost() throws CoreException {
        final String host = getRemoteDebugHostValue();
        if (host.isEmpty()) {
            throw newCoreException("Server IP cannot be empty");
        }
        return host;
    }

    @Override
    public int getRemoteDebugPort() throws CoreException {
        final String port = getRemoteDebugPortValue();
        final Integer portAsInt = Ints.tryParse(port);
        if (portAsInt == null || !Range.closed(1, MAX_PORT).contains(portAsInt)) {
            throw newCoreException("Server port '" + port + "' must be an Integer between 1 and 65,535");
        }
        return portAsInt;
    }

    @Override
    public int getRemoteDebugTimeout() throws CoreException {
        final String timeout = getRemoteDebugTimeoutValue();
        final Integer timeoutAsInt = Ints.tryParse(timeout);
        if (timeoutAsInt == null || !Range.closed(1, MAX_TIMEOUT).contains(timeoutAsInt)) {
            throw newCoreException("Connection timeout '" + timeout + "' must be an Integer between 1 and 3,600,000");
        }
        return timeoutAsInt;
    }

    @Override
    public String getRemoteDebugHostValue() throws CoreException {
        return configuration.getAttribute(REMOTE_HOST_ATTRIBUTE, "");
    }

    @Override
    public String getRemoteDebugPortValue() throws CoreException {
        return configuration.getAttribute(REMOTE_PORT_ATTRIBUTE, "");
    }

    @Override
    public String getRemoteDebugTimeoutValue() throws CoreException {
        return configuration.getAttribute(REMOTE_TIMEOUT_ATTRIBUTE, "");
    }

    @Override
    public void setRemoteDebugHostValue(final String host) throws CoreException {
        final ILaunchConfigurationWorkingCopy launchCopy = asWorkingCopy();
        launchCopy.setAttribute(REMOTE_HOST_ATTRIBUTE, host);
    }

    @Override
    public void setRemoteDebugPortValue(final String port) throws CoreException {
        final ILaunchConfigurationWorkingCopy launchCopy = asWorkingCopy();
        launchCopy.setAttribute(REMOTE_PORT_ATTRIBUTE, port);
    }

    @Override
    public void setRemoteDebugTimeoutValue(final String timeout) throws CoreException {
        final ILaunchConfigurationWorkingCopy launchCopy = asWorkingCopy();
        launchCopy.setAttribute(REMOTE_TIMEOUT_ATTRIBUTE, timeout);
    }

    public String getScriptPath() throws CoreException {
        return configuration.getAttribute(SCRIPT_PATH_ATTRIBUTE, "");
    }

    public String getScriptArguments() throws CoreException {
        return configuration.getAttribute(SCRIPT_ARGUMENTS_ATTRIBUTE, "");
    }

    public String getScriptRunCommand() throws CoreException {
        return configuration.getAttribute(SCRIPT_RUN_COMMAND_ATTRIBUTE, "");
    }

    public void setScriptPath(final String path) throws CoreException {
        final ILaunchConfigurationWorkingCopy launchCopy = asWorkingCopy();
        launchCopy.setAttribute(SCRIPT_PATH_ATTRIBUTE, path);
    }

    public void setScriptArguments(final String arguments) throws CoreException {
        final ILaunchConfigurationWorkingCopy launchCopy = asWorkingCopy();
        launchCopy.setAttribute(SCRIPT_ARGUMENTS_ATTRIBUTE, arguments);
    }

    public void setScriptRunCommand(final String command) throws CoreException {
        final ILaunchConfigurationWorkingCopy launchCopy = asWorkingCopy();
        launchCopy.setAttribute(SCRIPT_RUN_COMMAND_ATTRIBUTE, command);
    }

}

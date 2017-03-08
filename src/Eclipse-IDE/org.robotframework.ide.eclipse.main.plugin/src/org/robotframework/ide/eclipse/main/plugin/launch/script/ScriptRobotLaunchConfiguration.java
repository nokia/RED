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
import org.robotframework.ide.eclipse.main.plugin.launch.IRemoteRobotLaunchConfiguration;
import org.robotframework.ide.eclipse.main.plugin.launch.RobotLaunchConfigurationNaming;
import org.robotframework.ide.eclipse.main.plugin.launch.RobotLaunchConfigurationNaming.RobotLaunchConfigurationType;
import org.robotframework.ide.eclipse.main.plugin.launch.local.RobotLaunchConfiguration;

import com.google.common.collect.Range;
import com.google.common.primitives.Ints;

public class ScriptRobotLaunchConfiguration extends RobotLaunchConfiguration
        implements IRemoteRobotLaunchConfiguration {

    public static final String TYPE_ID = "org.robotframework.ide.scriptRobotLaunchConfiguration";

    private static final String REMOTE_AGENT = "Remote agent";

    private static final String REMOTE_HOST_ATTRIBUTE = "Remote host";

    private static final String REMOTE_PORT_ATTRIBUTE = "Remote port";

    private static final String REMOTE_TIMEOUT_ATTRIBUTE = "Remote timeout";

    private static final String SCRIPT_PATH_ATTRIBUTE = "Script path";

    private static final String SCRIPT_ARGUMENTS_ATTRIBUTE = "Script arguments";

    public static String[] getSystemDependentScriptExtensions() {
        return RedSystemProperties.isWindowsPlatform() ? new String[] { "*.bat;*.com;*.exe", "*.*" }
                : new String[] { "*.sh", "*.*" };
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
        final String namePrefix = RobotLaunchConfigurationNaming.getBasicName(suitesMapping.keySet(), type);
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
        setRemoteAgentValue(String.valueOf(preferences.isLaunchRemoteEnabled()));
        setRemoteHostValue(preferences.getLaunchRemoteHost());
        setRemotePortValue(preferences.getLaunchRemotePort());
        setRemoteTimeoutValue(preferences.getLaunchRemoteTimeout());
        super.fillDefaults();
    }

    @Override
    public boolean isDefiningProjectDirectly() {
        return false;
    }

    @Override
    public boolean isRemoteAgent() throws CoreException {
        return Boolean.valueOf(configuration.getAttribute(REMOTE_AGENT, "false"));
    }

    @Override
    public String getRemoteHost() throws CoreException {
        final String host = getRemoteHostValue();
        if (host.isEmpty()) {
            throw newCoreException("Server IP cannot be empty");
        }
        return host;
    }

    @Override
    public int getRemotePort() throws CoreException {
        final String port = getRemotePortValue();
        final Integer portAsInt = Ints.tryParse(port);
        if (portAsInt == null || !Range.closed(1, MAX_PORT).contains(portAsInt)) {
            throw newCoreException(
                    String.format("Server port '%s' must be an Integer between 1 and %,d", port, MAX_PORT));
        }
        return portAsInt;
    }

    @Override
    public int getRemoteTimeout() throws CoreException {
        final String timeout = getRemoteTimeoutValue();
        final Integer timeoutAsInt = Ints.tryParse(timeout);
        if (timeoutAsInt == null || !Range.closed(1, MAX_TIMEOUT).contains(timeoutAsInt)) {
            throw newCoreException(String.format("Connection timeout '%s' must be an Integer between 1 and %,d",
                    timeout, MAX_TIMEOUT));
        }
        return timeoutAsInt;
    }

    @Override
    public String getRemoteHostValue() throws CoreException {
        return configuration.getAttribute(REMOTE_HOST_ATTRIBUTE, "");
    }

    @Override
    public String getRemotePortValue() throws CoreException {
        return configuration.getAttribute(REMOTE_PORT_ATTRIBUTE, "");
    }

    @Override
    public String getRemoteTimeoutValue() throws CoreException {
        return configuration.getAttribute(REMOTE_TIMEOUT_ATTRIBUTE, "");
    }

    @Override
    public void setRemoteAgentValue(final String isRemote) throws CoreException {
        final ILaunchConfigurationWorkingCopy launchCopy = asWorkingCopy();
        launchCopy.setAttribute(REMOTE_AGENT, isRemote);
    }

    @Override
    public void setRemoteHostValue(final String host) throws CoreException {
        final ILaunchConfigurationWorkingCopy launchCopy = asWorkingCopy();
        launchCopy.setAttribute(REMOTE_HOST_ATTRIBUTE, host);
    }

    @Override
    public void setRemotePortValue(final String port) throws CoreException {
        final ILaunchConfigurationWorkingCopy launchCopy = asWorkingCopy();
        launchCopy.setAttribute(REMOTE_PORT_ATTRIBUTE, port);
    }

    @Override
    public void setRemoteTimeoutValue(final String timeout) throws CoreException {
        final ILaunchConfigurationWorkingCopy launchCopy = asWorkingCopy();
        launchCopy.setAttribute(REMOTE_TIMEOUT_ATTRIBUTE, timeout);
    }

    public String getScriptPath() throws CoreException {
        return configuration.getAttribute(SCRIPT_PATH_ATTRIBUTE, "");
    }

    public String getScriptArguments() throws CoreException {
        return configuration.getAttribute(SCRIPT_ARGUMENTS_ATTRIBUTE, "");
    }

    public void setScriptPath(final String path) throws CoreException {
        final ILaunchConfigurationWorkingCopy launchCopy = asWorkingCopy();
        launchCopy.setAttribute(SCRIPT_PATH_ATTRIBUTE, path);
    }

    public void setScriptArguments(final String arguments) throws CoreException {
        final ILaunchConfigurationWorkingCopy launchCopy = asWorkingCopy();
        launchCopy.setAttribute(SCRIPT_ARGUMENTS_ATTRIBUTE, arguments);
    }

}

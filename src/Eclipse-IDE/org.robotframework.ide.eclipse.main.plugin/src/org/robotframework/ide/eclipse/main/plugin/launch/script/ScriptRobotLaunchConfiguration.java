/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.launch.script;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.rf.ide.core.executor.RedSystemProperties;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences;
import org.robotframework.ide.eclipse.main.plugin.launch.AbstractRobotLaunchConfiguration;
import org.robotframework.ide.eclipse.main.plugin.launch.IRemoteRobotLaunchConfiguration;

import com.google.common.collect.Range;
import com.google.common.primitives.Ints;

public class ScriptRobotLaunchConfiguration extends AbstractRobotLaunchConfiguration
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
        if (portAsInt == null || !Range.closed(1, 65_535).contains(portAsInt)) {
            throw newCoreException("Server port '" + port + "' should be a value from range [1, 65 535]");
        }
        return portAsInt;
    }

    @Override
    public int getRemoteDebugTimeout() throws CoreException {
        final String timeout = getRemoteDebugTimeoutValue();
        final Integer timeoutAsInt = Ints.tryParse(timeout);
        if (timeoutAsInt == null || !Range.atLeast(1).contains(timeoutAsInt)) {
            throw newCoreException("Connection timeout '" + timeout + "' should be a positive integer value");
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

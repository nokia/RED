/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.launch;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;

import com.google.common.collect.Range;
import com.google.common.primitives.Ints;

public class ScriptRobotLaunchConfiguration extends AbstractRobotLaunchConfiguration
        implements IRemoteRobotLaunchConfiguration {

    static final String TYPE_ID = "org.robotframework.ide.scriptRobotLaunchConfiguration";

    public ScriptRobotLaunchConfiguration(final ILaunchConfiguration config) {
        super(config);
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
        return configuration.getAttribute(REMOTE_HOST_ATTRIBUTE, "127.0.0.1");
    }

    @Override
    public String getRemoteDebugPortValue() throws CoreException {
        return configuration.getAttribute(REMOTE_PORT_ATTRIBUTE, "12354");
    }

    @Override
    public String getRemoteDebugTimeoutValue() throws CoreException {
        return configuration.getAttribute(REMOTE_TIMEOUT_ATTRIBUTE, "30000");
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

}

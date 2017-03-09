/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.launch.remote;

import static com.google.common.collect.Lists.newArrayList;
import static org.robotframework.ide.eclipse.main.plugin.RedPlugin.newCoreException;

import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences;
import org.robotframework.ide.eclipse.main.plugin.launch.AbstractRobotLaunchConfiguration;
import org.robotframework.ide.eclipse.main.plugin.launch.IRemoteRobotLaunchConfiguration;

import com.google.common.collect.Range;
import com.google.common.primitives.Ints;

public class RemoteRobotLaunchConfiguration extends AbstractRobotLaunchConfiguration
        implements IRemoteRobotLaunchConfiguration {

    public static final String TYPE_ID = "org.robotframework.ide.remoteRobotLaunchConfiguration";

    private static final String REMOTE_AGENT = "Remote agent";

    private static final String REMOTE_HOST_ATTRIBUTE = "Remote host";

    private static final String REMOTE_PORT_ATTRIBUTE = "Remote port";

    private static final String REMOTE_TIMEOUT_ATTRIBUTE = "Remote timeout";

    public RemoteRobotLaunchConfiguration(final ILaunchConfiguration config) {
        super(config);
    }

    @Override
    public void fillDefaults() throws CoreException {
        final RedPreferences preferences = RedPlugin.getDefault().getPreferences();
        setRemoteAgentValue(String.valueOf(preferences.isLaunchRemoteEnabled()));
        setRemoteHostValue(preferences.getLaunchRemoteHost());
        setRemotePortValue(preferences.getLaunchRemotePort());
        setRemoteTimeoutValue(preferences.getLaunchRemoteTimeout());
        super.fillDefaults();
    }

    @Override
    public List<IResource> getResourcesUnderDebug() throws CoreException {
        return newArrayList(getProject());
    }

    @Override
    public boolean isDefiningProjectDirectly() {
        return true;
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
            throw newCoreException(String
                    .format("Connection timeout '%s' must be an Integer between 1 and %,d", timeout, MAX_TIMEOUT));
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
    public void setRemoteAgentValue(final String customRemote) throws CoreException {
        final ILaunchConfigurationWorkingCopy launchCopy = asWorkingCopy();
        launchCopy.setAttribute(REMOTE_AGENT, customRemote);
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

}

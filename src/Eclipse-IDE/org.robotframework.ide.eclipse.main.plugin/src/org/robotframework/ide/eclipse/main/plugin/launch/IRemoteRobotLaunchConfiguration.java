/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.launch;

import org.eclipse.core.runtime.CoreException;

public interface IRemoteRobotLaunchConfiguration extends IRobotLaunchConfiguration {

    int MAX_PORT = 65_535;

    int MAX_TIMEOUT = 3_600;

    String REMOTE_HOST_ATTRIBUTE = "Remote host";

    String REMOTE_PORT_ATTRIBUTE = "Remote port";

    String REMOTE_TIMEOUT_ATTRIBUTE = "Remote timeout";

    boolean isDefiningProjectDirectly();

    int getRemotePort() throws CoreException;

    int getRemoteTimeout() throws CoreException;

    String getRemoteHost() throws CoreException;

    String getRemotePortValue() throws CoreException;

    String getRemoteTimeoutValue() throws CoreException;

    String getRemoteHostValue() throws CoreException;

    void setRemoteHostValue(String host) throws CoreException;

    void setRemotePortValue(String port) throws CoreException;

    void setRemoteTimeoutValue(String timeout) throws CoreException;

}

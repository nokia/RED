/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.launch;

import org.eclipse.core.runtime.CoreException;

public interface IRemoteRobotLaunchConfiguration extends IRobotLaunchConfiguration {

    String REMOTE_HOST_ATTRIBUTE = "Remote host";

    String REMOTE_PORT_ATTRIBUTE = "Remote port";

    String REMOTE_TIMEOUT_ATTRIBUTE = "Remote timeout";

    boolean isDefiningProjectDirectly();

    int getRemoteDebugPort() throws CoreException;

    int getRemoteDebugTimeout() throws CoreException;

    String getRemoteDebugHost() throws CoreException;

    String getRemoteDebugPortValue() throws CoreException;

    String getRemoteDebugTimeoutValue() throws CoreException;

    String getRemoteDebugHostValue() throws CoreException;

    void setRemoteDebugHostValue(String host) throws CoreException;

    void setRemoteDebugPortValue(String port) throws CoreException;

    void setRemoteDebugTimeoutValue(String timeout) throws CoreException;
}
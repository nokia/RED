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

    boolean isDefiningProjectDirectly();

    boolean isRemoteAgent() throws CoreException;

    String getRemoteHost() throws CoreException;

    int getRemotePort() throws CoreException;

    int getRemoteTimeout() throws CoreException;

    String getRemotePortValue() throws CoreException;

    String getRemoteTimeoutValue() throws CoreException;

    String getRemoteHostValue() throws CoreException;

    void setRemoteAgentValue(String isRemote) throws CoreException;

    void setRemoteHostValue(String host) throws CoreException;

    void setRemotePortValue(String port) throws CoreException;

    void setRemoteTimeoutValue(String timeout) throws CoreException;

}

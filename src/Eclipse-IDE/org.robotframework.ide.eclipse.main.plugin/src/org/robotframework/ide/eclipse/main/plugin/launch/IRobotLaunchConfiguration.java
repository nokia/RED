/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.launch;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;

public interface IRobotLaunchConfiguration {

    AtomicBoolean IS_CONFIGURATION_RUNNING = new AtomicBoolean(false);

    String getName();

    String getTypeName();

    void fillDefaults() throws CoreException;

    String getProjectName() throws CoreException;

    void setProjectName(String projectName) throws CoreException;

    RobotProject getRobotProject() throws CoreException;

    List<IResource> getResourcesUnderDebug() throws CoreException;

    boolean isDefiningProjectDirectly();

    boolean isRemoteAgent() throws CoreException;

    String getAgentConnectionHost() throws CoreException;

    int getAgentConnectionPort() throws CoreException;

    int getAgentConnectionTimeout() throws CoreException;

    String getAgentConnectionHostValue() throws CoreException;

    String getAgentConnectionPortValue() throws CoreException;

    String getAgentConnectionTimeoutValue() throws CoreException;

    void setRemoteAgentValue(String isRemote) throws CoreException;

    void setAgentConnectionHostValue(String host) throws CoreException;

    void setAgentConnectionPortValue(String port) throws CoreException;

    void setAgentConnectionTimeoutValue(String timeout) throws CoreException;

    static boolean lockConfigurationLaunches() {
        return IS_CONFIGURATION_RUNNING.getAndSet(true);
    }

    static void unlockConfigurationLaunches() {
        IS_CONFIGURATION_RUNNING.set(false);
    }

}

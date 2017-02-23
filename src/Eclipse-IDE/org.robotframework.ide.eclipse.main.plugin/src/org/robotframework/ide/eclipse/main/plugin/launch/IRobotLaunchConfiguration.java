/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.launch;

import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.core.runtime.CoreException;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;

public interface IRobotLaunchConfiguration {

    static final AtomicBoolean IS_CONFIGURATION_RUNNING = new AtomicBoolean(false);

    String PROJECT_NAME_ATTRIBUTE = "Project name";

    String getName();

    String getTypeName();

    void fillDefaults() throws CoreException;

    String getProjectName() throws CoreException;

    void setProjectName(String projectName) throws CoreException;

    RobotProject getRobotProject() throws CoreException;

    public static boolean lockConfigurationLaunches() {
        return IS_CONFIGURATION_RUNNING.getAndSet(true);
    }

    public static void unlockConfigurationLaunches() {
        IS_CONFIGURATION_RUNNING.set(false);
    }

}
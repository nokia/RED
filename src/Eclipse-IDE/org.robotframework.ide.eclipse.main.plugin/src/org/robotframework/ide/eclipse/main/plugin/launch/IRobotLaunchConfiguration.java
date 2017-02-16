/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.launch;

import org.eclipse.core.runtime.CoreException;

public interface IRobotLaunchConfiguration {

    String getName();

    String getProjectName() throws CoreException;

    void setProjectName(String text) throws CoreException;

}
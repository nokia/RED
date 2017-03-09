/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.launch.script;

import java.io.IOException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.robotframework.ide.eclipse.main.plugin.launch.AbstractRobotLaunchConfigurationDelegate;
import org.robotframework.ide.eclipse.main.plugin.launch.RobotTestExecutionService.RobotTestsLaunch;

public class ScriptRobotLaunchConfigurationDelegate extends AbstractRobotLaunchConfigurationDelegate {

    @Override
    protected void doLaunch(final ILaunchConfiguration configuration, final String mode, final ILaunch launch,
            final RobotTestsLaunch testsLaunchContext, final IProgressMonitor monitor)
            throws CoreException, IOException {

        final ScriptRobotLaunchConfiguration robotConfig = new ScriptRobotLaunchConfiguration(configuration);

        if (ILaunchManager.RUN_MODE.equals(mode)) {
            new ScriptLaunchInRunMode(testsLaunchContext).launch(robotConfig, launch, monitor);
        } else if (ILaunchManager.DEBUG_MODE.equals(mode)) {
            new ScriptLaunchInDebugMode(testsLaunchContext).launch(robotConfig, launch, monitor);
        }
    }

}

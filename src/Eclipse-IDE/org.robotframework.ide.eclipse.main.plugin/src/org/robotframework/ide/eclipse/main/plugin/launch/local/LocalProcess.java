/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.launch.local;

import java.util.Map;

import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.RuntimeProcess;
import org.robotframework.ide.eclipse.main.plugin.launch.IRobotProcess;
import org.robotframework.ide.eclipse.main.plugin.launch.RobotConsoleFacade;

public class LocalProcess extends RuntimeProcess implements IRobotProcess {

    public LocalProcess(final ILaunch launch, final Process process, final String name,
            final Map<String, String> attributes) {
        super(launch, process, name, attributes);
    }

    @Override
    public RobotConsoleFacade provideConsoleFacade(final String consoleDescription) {
        return RobotConsoleFacade.provide(getLaunch().getLaunchConfiguration(), consoleDescription);
    }
}

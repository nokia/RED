/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.launch;

import java.util.Optional;

import org.eclipse.debug.core.ILaunch;
import org.robotframework.ide.eclipse.main.plugin.debug.model.RobotDebugTarget;


public class ProcessConnectingInDebugServerListener extends ProcessConnectingInRunServerListener {

    public ProcessConnectingInDebugServerListener(final ILaunch launch) {
        super(launch);
    }

    @Override
    public void clientConnected(final int clientId) {
        super.clientConnected(clientId);

        Optional<RobotDebugTarget> debugTarget = getDebugTarget();
        while (!debugTarget.isPresent()) {
            try {
                Thread.sleep(100);
            } catch (final InterruptedException e) {
                // retry
            }
            debugTarget = getDebugTarget();
        }

        debugTarget.get().connected();
    }

    private Optional<RobotDebugTarget> getDebugTarget() {
        return Optional.ofNullable(launch.getDebugTarget())
                .filter(RobotDebugTarget.class::isInstance)
                .map(RobotDebugTarget.class::cast);

    }
}

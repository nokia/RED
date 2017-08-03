/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.launch;

import java.io.IOException;

import org.rf.ide.core.execution.agent.RobotAgentEventListener.RobotAgentEventsListenerException;
import org.rf.ide.core.execution.server.DefaultAgentServerStatusListener;
import org.robotframework.ide.eclipse.main.plugin.debug.model.RobotBreakpoints;


public class BreakpointsEnabler extends DefaultAgentServerStatusListener {

    @Override
    public void clientConnectionClosed(final int clientId) {
        new RobotBreakpoints().enableBreakpointsDisabledByHitCounter();
    }

    @Override
    public void clientConnectionError(final IOException e) {
        new RobotBreakpoints().enableBreakpointsDisabledByHitCounter();
    }

    @Override
    public void clientEventHandlingError(final RobotAgentEventsListenerException e) {
        new RobotBreakpoints().enableBreakpointsDisabledByHitCounter();
    }
}

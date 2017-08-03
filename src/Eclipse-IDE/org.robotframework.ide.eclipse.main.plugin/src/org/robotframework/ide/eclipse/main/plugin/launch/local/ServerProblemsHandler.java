/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.launch.local;

import org.rf.ide.core.execution.agent.RobotAgentEventListener.RobotAgentEventsListenerException;
import org.rf.ide.core.execution.server.DefaultAgentServerStatusListener;


class ServerProblemsHandler extends DefaultAgentServerStatusListener {

    @Override
    public void clientEventHandlingError(final RobotAgentEventsListenerException e) {
        throw new RedServerException("Server error: problem handling agent event", e);
    }

    public static class RedServerException extends RuntimeException {

        private static final long serialVersionUID = 1L;

        public RedServerException(final String message, final Throwable cause) {
            super(message, cause);
        }
    }
}

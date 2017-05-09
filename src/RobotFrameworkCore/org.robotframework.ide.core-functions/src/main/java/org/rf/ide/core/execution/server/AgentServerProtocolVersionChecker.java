/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.execution.server;

import java.io.IOException;

import org.rf.ide.core.execution.agent.RobotDefaultAgentEventListener;
import org.rf.ide.core.execution.server.response.ProtocolVersion;
import org.rf.ide.core.execution.server.response.ServerResponse.ResponseException;


class AgentServerProtocolVersionChecker extends RobotDefaultAgentEventListener {

    private AgentClient client;

    @Override
    public void setClient(final AgentClient client) {
        this.client = client;
    }

    @Override
    public void handleVersions(final String pythonVersion, final String robotVersion, final int protocolVersion) {
        final boolean isCorrect = protocolVersion == AgentConnectionServer.RED_AGENT_PROTOCOL_VERSION;
        try {
            client.send(new ProtocolVersion(isCorrect));
        } catch (ResponseException | IOException e) {
            throw new RobotAgentEventsListenerException("Unable to send response to client", e);
        }
        
        if (!isCorrect) {
            throw new RobotAgentEventsListenerException("RED & Agent protocol mismatch.\n" + 
                    "\tRED version: " + AgentConnectionServer.RED_AGENT_PROTOCOL_VERSION + "\n" + 
                    "\tAgent version: " + protocolVersion);
        }
    }
}

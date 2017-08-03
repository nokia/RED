/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.execution.server;

import java.util.Optional;
import java.util.stream.Stream;

import org.rf.ide.core.execution.agent.RobotDefaultAgentEventListener;
import org.rf.ide.core.execution.agent.event.VersionsEvent;
import org.rf.ide.core.execution.server.response.ServerResponse.ResponseException;


public class AgentServerVersionsChecker extends RobotDefaultAgentEventListener {

    @Override
    public void handleVersions(final VersionsEvent event) {
        final Optional<String> error =
                Stream.of(
                        Optional.of(event.getPythonVersion()).flatMap(this::checkPython),
                        Optional.of(event.getRobotVersion()).flatMap(this::checkRobot),
                        Optional.of(event.getProtocolVersion()).flatMap(this::checkProtocol))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst();

        try {
            if (error.isPresent()) {
                event.responder().versionsError(error.get());
            } else {
                event.responder().versionsCorrect();
            }
        } catch (final ResponseException e) {
            throw new RobotAgentEventsListenerException("Unable to send response to client", e);
        }
        
        if (error.isPresent()) {
            throw new RobotAgentEventsListenerException(error.get());
        }
    }

    protected Optional<String> checkPython(@SuppressWarnings("unused") final String pythonVersion) {
        return Optional.empty();
    }

    protected Optional<String> checkRobot(@SuppressWarnings("unused") final String robotVersion) {
        return Optional.empty();
    }

    protected Optional<String> checkProtocol(final int protocolVersion) {
        if (protocolVersion != AgentConnectionServer.RED_AGENT_PROTOCOL_VERSION) {
            return Optional.of("RED & Agent protocol mismatch.\n" + "\tRED version: "
                    + AgentConnectionServer.RED_AGENT_PROTOCOL_VERSION + "\n" + "\tAgent version: " + protocolVersion);
        }
        return Optional.empty();
    }
}

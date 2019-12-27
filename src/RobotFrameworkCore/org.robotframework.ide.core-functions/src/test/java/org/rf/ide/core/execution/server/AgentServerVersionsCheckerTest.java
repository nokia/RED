/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.execution.server;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.rf.ide.core.execution.agent.RobotAgentEventListener.RobotAgentEventsListenerException;
import org.rf.ide.core.execution.agent.event.VersionsEvent;
import org.rf.ide.core.execution.agent.event.VersionsEvent.VersionsEventResponder;
import org.rf.ide.core.execution.server.response.ProtocolVersion;
import org.rf.ide.core.execution.server.response.ServerResponse.ResponseException;

import com.google.common.collect.ImmutableMap;

public class AgentServerVersionsCheckerTest {

    @Test
    public void correctVersionMsgIsSendToClient_whenVersionsAreMatching() throws Exception {
        final AgentClient client = mock(AgentClient.class);

        final AgentServerVersionsChecker checker = new AgentServerVersionsChecker();

        final List<Object> attributes = newArrayList(
                ImmutableMap.of("cmd_line", "cmd", "python", "", "robot", "", "protocol", getCurrentVersion()));
        final Map<String, Object> eventMap = ImmutableMap.of("version", attributes);
        checker.handleVersions(VersionsEvent.from(client, eventMap));

        verify(client).send(any(ProtocolVersion.class));
    }

    @Test
    public void incorrectVersionMsgIsSendToClientAndExceptionIsThrown_whenVersionUsedByClientIsOlderThanCurrent()
            throws Exception {
        final AgentClient client = mock(AgentClient.class);

        final AgentServerVersionsChecker checker = new AgentServerVersionsChecker();

        final List<Object> attributes = newArrayList(
                ImmutableMap.of("cmd_line", "cmd", "python", "", "robot", "", "protocol", getOlderVersion()));
        final Map<String, Object> eventMap = ImmutableMap.of("version", attributes);

        assertThatExceptionOfType(RobotAgentEventsListenerException.class)
                .isThrownBy(() -> checker.handleVersions(VersionsEvent.from(client, eventMap)))
                .withMessage("RED & Agent protocol mismatch.\n" + "\tRED version: "
                        + AgentConnectionServer.RED_AGENT_PROTOCOL_VERSION + "\n" + "\tAgent version: "
                        + getOlderVersion())
                .withNoCause();
        verify(client).send(any(ProtocolVersion.class));
    }

    @Test
    public void incorrectVersionMsgIsSendToClientAndExceptionIsThrown_whenVersionUsedByClientIsNewerThanCurrent()
            throws Exception {
        final AgentClient client = mock(AgentClient.class);

        final AgentServerVersionsChecker checker = new AgentServerVersionsChecker();

        final List<Object> attributes = newArrayList(
                ImmutableMap.of("cmd_line", "cmd", "python", "", "robot", "", "protocol", getNewerVersion()));
        final Map<String, Object> eventMap = ImmutableMap.of("version", attributes);

        assertThatExceptionOfType(RobotAgentEventsListenerException.class)
                .isThrownBy(() -> checker.handleVersions(VersionsEvent.from(client, eventMap)))
                .withMessage("RED & Agent protocol mismatch.\n" + "\tRED version: "
                        + AgentConnectionServer.RED_AGENT_PROTOCOL_VERSION + "\n" + "\tAgent version: "
                        + getNewerVersion())
                .withNoCause();
        verify(client).send(any(ProtocolVersion.class));
    }

    @Test
    public void exceptionIsThrown_whenVersionCheckerResponseToClient() {
        final AgentServerVersionsChecker checker = new AgentServerVersionsChecker();

        final VersionsEventResponder responder = mock(VersionsEventResponder.class);
        doThrow(ResponseException.class).when(responder).versionsCorrect();

        final VersionsEvent event = new VersionsEvent(responder, "cmd_line", "3.6", "3.0.2", getCurrentVersion(),
                Optional.of(42L));

        assertThatExceptionOfType(RobotAgentEventsListenerException.class)
                .isThrownBy(() -> checker.handleVersions(event));
    }

    private static int getOlderVersion() {
        return AgentConnectionServer.RED_AGENT_PROTOCOL_VERSION - 2;
    }

    private static int getCurrentVersion() {
        return AgentConnectionServer.RED_AGENT_PROTOCOL_VERSION;
    }

    private static int getNewerVersion() {
        return AgentConnectionServer.RED_AGENT_PROTOCOL_VERSION + 2;
    }
}

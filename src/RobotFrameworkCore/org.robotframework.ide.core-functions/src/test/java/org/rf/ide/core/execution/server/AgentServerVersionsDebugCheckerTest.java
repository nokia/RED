/*
* Copyright 2017 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.rf.ide.core.execution.server;

import static com.google.common.collect.Lists.newArrayList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.rf.ide.core.execution.agent.RobotAgentEventListener.RobotAgentEventsListenerException;
import org.rf.ide.core.execution.agent.event.VersionsEvent;
import org.rf.ide.core.execution.agent.event.VersionsEvent.VersionsEventResponder;
import org.rf.ide.core.execution.server.response.ProtocolVersion;
import org.rf.ide.core.execution.server.response.ServerResponse.ResponseException;

import com.google.common.collect.ImmutableMap;

public class AgentServerVersionsDebugCheckerTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void correctVersionMsgIsSendToClient_whenVersionsAreMatching() throws Exception {
        final AgentClient client = mock(AgentClient.class);

        final AgentServerVersionsDebugChecker checker = new AgentServerVersionsDebugChecker();

        final List<Object> attributes = newArrayList(
                ImmutableMap.of("cmd_line", "cmd", "python", "", "robot", "2.9", "protocol",
                        getCurrentProtocolVersion()));
        final Map<String, Object> eventMap = ImmutableMap.of("version", attributes);
        checker.handleVersions(VersionsEvent.from(client, eventMap));

        verify(client).send(any(ProtocolVersion.class));
    }

    @Test
    public void incorrectVersionMsgIsSendToClientAndExceptionIsThrown_whenProtocolVersionUsedByClientIsOlderThanCurrent()
            throws Exception {
        expectedException.expect(RobotAgentEventsListenerException.class);
        expectedException.expectMessage("RED & Agent protocol mismatch.\n" + "\tRED version: "
                + AgentConnectionServer.RED_AGENT_PROTOCOL_VERSION + "\n" + "\tAgent version: "
                + getOlderProtocolVersion());

        final AgentClient client = mock(AgentClient.class);

        final AgentServerVersionsDebugChecker checker = new AgentServerVersionsDebugChecker();

        final List<Object> attributes = newArrayList(
                ImmutableMap.of("cmd_line", "cmd", "python", "", "robot", "3.0", "protocol",
                        getOlderProtocolVersion()));
        final Map<String, Object> eventMap = ImmutableMap.of("version", attributes);
        checker.handleVersions(VersionsEvent.from(client, eventMap));

        verify(client).send(any(ProtocolVersion.class));
    }

    @Test
    public void incorrectVersionMsgIsSendToClientAndExceptionIsThrown_whenProtocolVersionUsedByClientIsNewerThanCurrent()
            throws Exception {
        expectedException.expect(RobotAgentEventsListenerException.class);
        expectedException.expectMessage("RED & Agent protocol mismatch.\n" + "\tRED version: "
                + AgentConnectionServer.RED_AGENT_PROTOCOL_VERSION + "\n" + "\tAgent version: "
                + getNewerProtocolVersion());

        final AgentClient client = mock(AgentClient.class);

        final AgentServerVersionsDebugChecker checker = new AgentServerVersionsDebugChecker();

        final List<Object> attributes = newArrayList(
                ImmutableMap.of("cmd_line", "cmd", "python", "", "robot", "3.0", "protocol",
                        getNewerProtocolVersion()));
        final Map<String, Object> eventMap = ImmutableMap.of("version", attributes);
        checker.handleVersions(VersionsEvent.from(client, eventMap));

        verify(client).send(any(ProtocolVersion.class));
    }

    @Test
    public void incorrectVersionMsgIsSendToClientAndExceptionIsThrown_whenRobotVersionUsedByClientIsOlderThan29()
            throws Exception {
        expectedException.expect(RobotAgentEventsListenerException.class);
        expectedException.expectMessage(
                "RED debugger requires Robot Framework in version 2.9 or newer.\n\tRobot Framework: 2.8");

        final AgentClient client = mock(AgentClient.class);

        final AgentServerVersionsDebugChecker checker = new AgentServerVersionsDebugChecker();

        final List<Object> attributes = newArrayList(ImmutableMap.of("cmd_line", "cmd", "python", "", "robot", "2.8",
                "protocol", getCurrentProtocolVersion()));
        final Map<String, Object> eventMap = ImmutableMap.of("version", attributes);
        checker.handleVersions(VersionsEvent.from(client, eventMap));

        verify(client).send(any(ProtocolVersion.class));
    }

    @Test(expected = RobotAgentEventsListenerException.class)
    public void exceptionIsThrown_whenVersionCheckerResponseToClient() {
        final AgentServerVersionsDebugChecker checker = new AgentServerVersionsDebugChecker();

        final VersionsEventResponder responder = mock(VersionsEventResponder.class);
        doThrow(ResponseException.class).when(responder).versionsCorrect();

        final VersionsEvent event = new VersionsEvent(responder, "cmd_line", "3.6", "3.0.2",
                getCurrentProtocolVersion(), Optional.of(42L));

        checker.handleVersions(event);
    }

    private static int getOlderProtocolVersion() {
        return AgentConnectionServer.RED_AGENT_PROTOCOL_VERSION - 2;
    }

    private static int getCurrentProtocolVersion() {
        return AgentConnectionServer.RED_AGENT_PROTOCOL_VERSION;
    }

    private static int getNewerProtocolVersion() {
        return AgentConnectionServer.RED_AGENT_PROTOCOL_VERSION + 2;
    }
}

/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.execution.server;

import static com.google.common.collect.Lists.newArrayList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Map;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.rf.ide.core.execution.agent.RobotAgentEventListener.RobotAgentEventsListenerException;
import org.rf.ide.core.execution.agent.event.VersionsEvent;
import org.rf.ide.core.execution.server.response.ProtocolVersion;

import com.google.common.collect.ImmutableMap;

public class AgentServerVersionsCheckerTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void correctVersionMsgIsSendToClient_whenVersionsAreMatching() throws Exception {
        final AgentClient client = mock(AgentClient.class);

        final AgentServerVersionsChecker checker = new AgentServerVersionsChecker();

        final List<Object> attributes = newArrayList(
                ImmutableMap.of("python", "", "robot", "", "protocol", getCurrentVersion()));
        final Map<String, Object> eventMap = ImmutableMap.of("version", attributes);
        checker.handleVersions(VersionsEvent.from(client, eventMap));

        verify(client).send(new ProtocolVersion(null));
    }

    @Test
    public void incorrectVersionMsgIsSendToClientAndExceptionIsThrown_whenVersionUsedByClientIsOlderThanCurrent()
            throws Exception {
        expectedException.expect(RobotAgentEventsListenerException.class);
        expectedException.expectMessage("RED & Agent protocol mismatch.\n" +
                "\tRED version: " + AgentConnectionServer.RED_AGENT_PROTOCOL_VERSION + "\n" +
                "\tAgent version: " + getOlderVersion());

        final AgentClient client = mock(AgentClient.class);

        final AgentServerVersionsChecker checker = new AgentServerVersionsChecker();

        final List<Object> attributes = newArrayList(
                ImmutableMap.of("python", "", "robot", "", "protocol", getOlderVersion()));
        final Map<String, Object> eventMap = ImmutableMap.of("version", attributes);
        checker.handleVersions(VersionsEvent.from(client, eventMap));

        verify(client).send(new ProtocolVersion("error"));
    }

    @Test
    public void incorrectVersionMsgIsSendToClientAndExceptionIsThrown_whenVersionUsedByClientIsNewerThanCurrent()
            throws Exception {
        expectedException.expect(RobotAgentEventsListenerException.class);
        expectedException.expectMessage("RED & Agent protocol mismatch.\n" +
                "\tRED version: " + AgentConnectionServer.RED_AGENT_PROTOCOL_VERSION + "\n" +
                "\tAgent version: " + getNewerVersion());

        final AgentClient client = mock(AgentClient.class);

        final AgentServerVersionsChecker checker = new AgentServerVersionsChecker();

        final List<Object> attributes = newArrayList(
                ImmutableMap.of("python", "", "robot", "", "protocol", getNewerVersion()));
        final Map<String, Object> eventMap = ImmutableMap.of("version", attributes);
        checker.handleVersions(VersionsEvent.from(client, eventMap));

        verify(client).send(new ProtocolVersion("error"));
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

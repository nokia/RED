/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.execution.server;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.rf.ide.core.execution.agent.RobotAgentEventListener.RobotAgentEventsListenerException;
import org.rf.ide.core.execution.server.response.ProtocolVersion;

public class AgentServerProtocolVersionCheckerTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void correctVersionMsgIsSendToClient_whenVersionsAreMatching() throws Exception {
        final AgentClient client = mock(AgentClient.class);

        final AgentServerProtocolVersionChecker checker = new AgentServerProtocolVersionChecker();
        checker.setClient(client);

        checker.handleVersions("", "", getCurrentVersion());

        verify(client).send(new ProtocolVersion(true));
    }

    @Test
    public void incorrectVersionMsgIsSendToClientAndExceptionIsThrown_whenVersionUsedByClientIsOlderThanCurrent()
            throws Exception {
        expectedException.expect(RobotAgentEventsListenerException.class);
        expectedException.expectMessage("RED & Agent protocol mismatch.\n" + 
                "\tRED version: " + AgentConnectionServer.RED_AGENT_PROTOCOL_VERSION + "\n" + 
                "\tAgent version: " + getOlderVersion());

        final AgentClient client = mock(AgentClient.class);

        final AgentServerProtocolVersionChecker checker = new AgentServerProtocolVersionChecker();
        checker.setClient(client);

        checker.handleVersions("", "", getOlderVersion());

        verify(client).send(new ProtocolVersion(false));
    }

    @Test
    public void incorrectVersionMsgIsSendToClientAndExceptionIsThrown_whenVersionUsedByClientIsNewerThanCurrent()
            throws Exception {
        expectedException.expect(RobotAgentEventsListenerException.class);
        expectedException.expectMessage("RED & Agent protocol mismatch.\n" + 
                "\tRED version: " + AgentConnectionServer.RED_AGENT_PROTOCOL_VERSION + "\n" + 
                "\tAgent version: " + getNewerVersion());

        final AgentClient client = mock(AgentClient.class);

        final AgentServerProtocolVersionChecker checker = new AgentServerProtocolVersionChecker();
        checker.setClient(client);

        checker.handleVersions("", "", getNewerVersion());

        verify(client).send(new ProtocolVersion(false));
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

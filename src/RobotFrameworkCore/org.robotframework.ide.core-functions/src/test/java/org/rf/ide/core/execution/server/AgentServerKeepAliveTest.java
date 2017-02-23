/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.execution.server;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class AgentServerKeepAliveTest {

    @Test
    public void newlyCreateKeepAliveObjectIsHandlingEvents() {
        assertThat(new AgentServerKeepAlive().isHandlingEvents()).isTrue();
    }

    @Test
    public void keepAliveObjectDoesNotHandleEventsAnymore_whenItIsStopped() {
        final AgentServerKeepAlive keepAlive = new AgentServerKeepAlive();
        keepAlive.stopHandlingEvents();
        assertThat(keepAlive.isHandlingEvents()).isFalse();
    }

    @Test
    public void keepAliveObjectDoesNotHandleEventsAnymore_whenCloseEventsIsHandled() {
        final AgentServerKeepAlive keepAlive = new AgentServerKeepAlive();
        keepAlive.handleClosed();
        assertThat(keepAlive.isHandlingEvents()).isFalse();
    }
}

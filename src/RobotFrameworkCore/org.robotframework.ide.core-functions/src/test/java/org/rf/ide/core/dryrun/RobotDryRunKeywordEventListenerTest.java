/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.dryrun;

import static com.google.common.collect.Lists.newArrayList;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.net.URI;
import java.util.function.Consumer;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.rf.ide.core.execution.agent.LogLevel;
import org.rf.ide.core.execution.agent.event.MessageEvent;
import org.rf.ide.core.execution.agent.event.SuiteStartedEvent;

public class RobotDryRunKeywordEventListenerTest {

    @Mock
    private Consumer<String> startSuiteHandler;

    @Mock
    private RobotDryRunKeywordSourceCollector kwSourceCollector;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void suiteStartingEventIsHandled() throws Exception {
        final RobotDryRunKeywordEventListener listener = new RobotDryRunKeywordEventListener(kwSourceCollector,
                startSuiteHandler);

        listener.handleSuiteStarted(
                new SuiteStartedEvent("abc", new URI("file:///path"), true, 5, newArrayList(), newArrayList()));

        verify(startSuiteHandler).accept("abc");
        verifyNoMoreInteractions(startSuiteHandler);
        verifyZeroInteractions(kwSourceCollector);
    }

    @Test
    public void keywordMessageEventIsHandled() throws Exception {
        final RobotDryRunKeywordEventListener listener = new RobotDryRunKeywordEventListener(kwSourceCollector,
                startSuiteHandler);

        listener.handleMessage(new MessageEvent("kw_message_789", LogLevel.NONE, null));

        verify(kwSourceCollector).collectFromMessageEvent("kw_message_789");
        verifyNoMoreInteractions(kwSourceCollector);
        verifyZeroInteractions(startSuiteHandler);
    }

    @Test
    public void unsupportedLevelMessageEventsAreIgnored() throws Exception {
        final RobotDryRunKeywordEventListener listener = new RobotDryRunKeywordEventListener(kwSourceCollector,
                startSuiteHandler);

        listener.handleMessage(new MessageEvent("msg", LogLevel.TRACE, null));
        listener.handleMessage(new MessageEvent("msg", LogLevel.DEBUG, null));
        listener.handleMessage(new MessageEvent("msg", LogLevel.INFO, null));
        listener.handleMessage(new MessageEvent("msg", LogLevel.WARN, null));
        listener.handleMessage(new MessageEvent("msg", LogLevel.ERROR, null));
        listener.handleMessage(new MessageEvent("msg", LogLevel.FAIL, null));

        verifyZeroInteractions(startSuiteHandler);
        verifyZeroInteractions(kwSourceCollector);
    }

    @Test
    public void multipleEventsAreHandledInRightOrder() throws Exception {
        final RobotDryRunKeywordEventListener listener = new RobotDryRunKeywordEventListener(kwSourceCollector,
                startSuiteHandler);

        listener.handleMessage(new MessageEvent("kw_2", LogLevel.NONE, null));
        listener.handleMessage(new MessageEvent("kw_1", LogLevel.NONE, null));
        listener.handleMessage(new MessageEvent("kw_3", LogLevel.NONE, null));

        final InOrder inOrder = inOrder(kwSourceCollector);

        inOrder.verify(kwSourceCollector).collectFromMessageEvent("kw_2");
        inOrder.verify(kwSourceCollector).collectFromMessageEvent("kw_1");
        inOrder.verify(kwSourceCollector).collectFromMessageEvent("kw_3");
        verifyNoMoreInteractions(kwSourceCollector);
        verifyZeroInteractions(startSuiteHandler);
    }

}

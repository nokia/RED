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
                new SuiteStartedEvent("abc", new URI("file:///path"), 5, newArrayList(), newArrayList()));

        verify(startSuiteHandler).accept("abc");
        verifyNoMoreInteractions(startSuiteHandler);
        verifyZeroInteractions(kwSourceCollector);
    }

    @Test
    public void keywordMessageEventIsHandled() throws Exception {
        final RobotDryRunKeywordEventListener listener = new RobotDryRunKeywordEventListener(kwSourceCollector,
                startSuiteHandler);

        listener.handleMessage("kw_message_789", LogLevel.NONE);

        verify(kwSourceCollector).collectFromMessageEvent("kw_message_789");
        verifyNoMoreInteractions(kwSourceCollector);
        verifyZeroInteractions(startSuiteHandler);
    }

    @Test
    public void unsupportedLevelMessageEventsAreIgnored() throws Exception {
        final RobotDryRunKeywordEventListener listener = new RobotDryRunKeywordEventListener(kwSourceCollector,
                startSuiteHandler);

        listener.handleMessage("msg", LogLevel.TRACE);
        listener.handleMessage("msg", LogLevel.DEBUG);
        listener.handleMessage("msg", LogLevel.INFO);
        listener.handleMessage("msg", LogLevel.WARN);
        listener.handleMessage("msg", LogLevel.ERROR);
        listener.handleMessage("msg", LogLevel.FAIL);

        verifyZeroInteractions(startSuiteHandler);
        verifyZeroInteractions(kwSourceCollector);
    }

    @Test
    public void multipleEventsAreHandledInRightOrder() throws Exception {
        final RobotDryRunKeywordEventListener listener = new RobotDryRunKeywordEventListener(kwSourceCollector,
                startSuiteHandler);

        listener.handleMessage("kw_2", LogLevel.NONE);
        listener.handleMessage("kw_1", LogLevel.NONE);
        listener.handleMessage("kw_3", LogLevel.NONE);

        final InOrder inOrder = inOrder(kwSourceCollector);

        inOrder.verify(kwSourceCollector).collectFromMessageEvent("kw_2");
        inOrder.verify(kwSourceCollector).collectFromMessageEvent("kw_1");
        inOrder.verify(kwSourceCollector).collectFromMessageEvent("kw_3");
        verifyNoMoreInteractions(kwSourceCollector);
        verifyZeroInteractions(startSuiteHandler);
    }

}

/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.dryrun;

import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.CoreMatchers.isA;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.net.URI;
import java.util.function.Consumer;

import org.codehaus.jackson.map.JsonMappingException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.rf.ide.core.execution.agent.LogLevel;
import org.rf.ide.core.execution.agent.event.MessageEvent;
import org.rf.ide.core.execution.agent.event.SuiteStartedEvent;

public class RobotDryRunKeywordEventListenerTest {

    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    @Mock
    private RobotDryRunKeywordSourceCollector kwSourceCollector;

    @Mock
    private Consumer<String> startSuiteHandler;

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
    public void keywordSourceEventMappingExceptionIsHandled() throws Exception {
        thrown.expect(JsonMessageMapper.JsonMessageMapperException.class);
        thrown.expectMessage("Problem with mapping keyword source message");
        thrown.expectCause(isA(JsonMappingException.class));

        final RobotDryRunKeywordEventListener listener = new RobotDryRunKeywordEventListener(
                new RobotDryRunKeywordSourceCollector(), startSuiteHandler);
        listener.handleMessage(new MessageEvent("{\"keyword\":\"incorrect\"}", LogLevel.NONE, null));
    }

    @Test
    public void keywordMessageEventIsHandled() throws Exception {
        final RobotDryRunKeywordEventListener listener = new RobotDryRunKeywordEventListener(kwSourceCollector,
                startSuiteHandler);

        final MessageEvent event = new MessageEvent("kw_message_789", LogLevel.NONE, null);

        listener.handleMessage(event);

        verify(kwSourceCollector).collectFromMessageEvent(event);
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

        final MessageEvent event1 = new MessageEvent("kw_1", LogLevel.NONE, null);
        final MessageEvent event2 = new MessageEvent("kw_2", LogLevel.NONE, null);
        final MessageEvent event3 = new MessageEvent("kw_3", LogLevel.NONE, null);

        listener.handleMessage(event1);
        listener.handleMessage(event2);
        listener.handleMessage(event3);

        final InOrder inOrder = inOrder(kwSourceCollector);

        inOrder.verify(kwSourceCollector).collectFromMessageEvent(event1);
        inOrder.verify(kwSourceCollector).collectFromMessageEvent(event2);
        inOrder.verify(kwSourceCollector).collectFromMessageEvent(event3);
        verifyNoMoreInteractions(kwSourceCollector);
        verifyZeroInteractions(startSuiteHandler);
    }

}

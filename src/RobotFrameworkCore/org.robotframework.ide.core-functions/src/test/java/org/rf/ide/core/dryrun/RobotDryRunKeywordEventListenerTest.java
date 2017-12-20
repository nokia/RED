/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.dryrun;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.net.URI;
import java.util.Arrays;
import java.util.function.Consumer;

import org.codehaus.jackson.map.JsonMappingException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.rf.ide.core.execution.agent.LogLevel;
import org.rf.ide.core.execution.agent.event.LibraryImportEvent;
import org.rf.ide.core.execution.agent.event.MessageEvent;

@RunWith(MockitoJUnitRunner.class)
public class RobotDryRunKeywordEventListenerTest {

    @Mock
    private RobotDryRunKeywordSourceCollector kwSourceCollector;

    @Mock
    private Consumer<String> libNameHandler;

    @Test
    public void libraryImportIsHandled() throws Exception {
        final RobotDryRunKeywordEventListener listener = new RobotDryRunKeywordEventListener(kwSourceCollector,
                libNameHandler);

        final LibraryImportEvent event = new LibraryImportEvent("String", new URI("file:///suite.robot"),
                new URI("file:///String.py"), Arrays.asList("a1", "a2"));

        listener.handleLibraryImport(event);

        verifyZeroInteractions(kwSourceCollector);
        verify(libNameHandler).accept("String");
        verifyNoMoreInteractions(libNameHandler);
    }

    @Test
    public void keywordSourceEventMappingExceptionIsHandled() throws Exception {
        final RobotDryRunKeywordEventListener listener = new RobotDryRunKeywordEventListener(
                new RobotDryRunKeywordSourceCollector(), libNameHandler);

        final MessageEvent event = new MessageEvent("{\"keyword\":\"incorrect\"}", LogLevel.NONE, null);

        assertThatExceptionOfType(JsonMessageMapper.JsonMessageMapperException.class)
                .isThrownBy(() -> listener.handleMessage(event))
                .withMessage("Problem with mapping keyword source message")
                .withCauseInstanceOf(JsonMappingException.class);

        verifyZeroInteractions(libNameHandler);
    }

    @Test
    public void keywordMessageEventIsHandled() throws Exception {
        final RobotDryRunKeywordEventListener listener = new RobotDryRunKeywordEventListener(kwSourceCollector,
                libNameHandler);

        final MessageEvent event = new MessageEvent("kw_message_789", LogLevel.NONE, null);

        listener.handleMessage(event);

        verify(kwSourceCollector).collectFromMessageEvent(event);
        verifyNoMoreInteractions(kwSourceCollector);
        verifyZeroInteractions(libNameHandler);
    }

    @Test
    public void unsupportedLevelMessageEventsAreIgnored() throws Exception {
        final RobotDryRunKeywordEventListener listener = new RobotDryRunKeywordEventListener(kwSourceCollector,
                libNameHandler);

        listener.handleMessage(new MessageEvent("msg", LogLevel.TRACE, null));
        listener.handleMessage(new MessageEvent("msg", LogLevel.DEBUG, null));
        listener.handleMessage(new MessageEvent("msg", LogLevel.INFO, null));
        listener.handleMessage(new MessageEvent("msg", LogLevel.WARN, null));
        listener.handleMessage(new MessageEvent("msg", LogLevel.ERROR, null));
        listener.handleMessage(new MessageEvent("msg", LogLevel.FAIL, null));

        verifyZeroInteractions(kwSourceCollector);
        verifyZeroInteractions(libNameHandler);
    }

    @Test
    public void multipleEventsAreHandledInRightOrder() throws Exception {
        final RobotDryRunKeywordEventListener listener = new RobotDryRunKeywordEventListener(kwSourceCollector,
                libNameHandler);

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
        verifyZeroInteractions(libNameHandler);
    }

}

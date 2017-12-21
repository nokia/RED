/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.dryrun;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.net.URI;
import java.util.Arrays;
import java.util.function.Consumer;

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
    public void libraryImportEventIsHandled() throws Exception {
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
    public void keywordSourceMessageEventIsHandled() throws Exception {
        final RobotDryRunKeywordEventListener listener = new RobotDryRunKeywordEventListener(kwSourceCollector,
                libNameHandler);

        final MessageEvent event = new MessageEvent("kw_message", LogLevel.NONE, null);

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
        final LibraryImportEvent event2 = new LibraryImportEvent("lib2", new URI("file:///suite1.robot"),
                new URI("file:///lib2.py"), Arrays.asList("a", "b"));
        final MessageEvent event3 = new MessageEvent("kw_2", LogLevel.NONE, null);
        final MessageEvent event4 = new MessageEvent("kw_3", LogLevel.NONE, null);
        final LibraryImportEvent event5 = new LibraryImportEvent("lib1", new URI("file:///suite1.robot"),
                new URI("file:///lib1.py"), Arrays.asList("x"));

        listener.handleMessage(event1);
        listener.handleLibraryImport(event2);
        listener.handleMessage(event3);
        listener.handleMessage(event4);
        listener.handleLibraryImport(event5);

        final InOrder kwSourceCollectorOrder = inOrder(kwSourceCollector);
        kwSourceCollectorOrder.verify(kwSourceCollector).collectFromMessageEvent(event1);
        kwSourceCollectorOrder.verify(kwSourceCollector).collectFromMessageEvent(event3);
        kwSourceCollectorOrder.verify(kwSourceCollector).collectFromMessageEvent(event4);
        verifyNoMoreInteractions(kwSourceCollector);

        final InOrder libNameHandlerOrder = inOrder(libNameHandler);
        libNameHandlerOrder.verify(libNameHandler).accept("lib2");
        libNameHandlerOrder.verify(libNameHandler).accept("lib1");
        verifyNoMoreInteractions(libNameHandler);
    }

}

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
public class RobotDryRunLibraryEventListenerTest {

    @Mock
    private RobotDryRunLibraryImportCollector libImportCollector;

    @Mock
    private Consumer<String> libNameHandler;

    @Test
    public void libraryImportEventIsHandled() throws Exception {
        final RobotDryRunLibraryEventListener listener = new RobotDryRunLibraryEventListener(libImportCollector,
                libNameHandler);

        final LibraryImportEvent event = new LibraryImportEvent("String", new URI("file:///suite.robot"),
                new URI("file:///String.py"), Arrays.asList("a1", "a2"));

        listener.handleLibraryImport(event);

        verify(libImportCollector).collectFromLibraryImportEvent(event);
        verifyNoMoreInteractions(libImportCollector);
        verify(libNameHandler).accept("String");
        verifyNoMoreInteractions(libNameHandler);
    }

    @Test
    public void libraryErrorMessageEventIsHandled() throws Exception {
        final RobotDryRunLibraryEventListener listener = new RobotDryRunLibraryEventListener(libImportCollector,
                libNameHandler);

        final MessageEvent event = new MessageEvent("import_error", LogLevel.NONE, null);

        listener.handleMessage(event);

        verify(libImportCollector).collectFromMessageEvent(event);
        verifyNoMoreInteractions(libImportCollector);
        verifyZeroInteractions(libNameHandler);
    }

    @Test
    public void unsupportedLevelMessageEventsAreIgnored() throws Exception {
        final RobotDryRunLibraryEventListener listener = new RobotDryRunLibraryEventListener(libImportCollector,
                libNameHandler);

        listener.handleMessage(new MessageEvent("msg", LogLevel.TRACE, null));
        listener.handleMessage(new MessageEvent("msg", LogLevel.DEBUG, null));
        listener.handleMessage(new MessageEvent("msg", LogLevel.INFO, null));
        listener.handleMessage(new MessageEvent("msg", LogLevel.WARN, null));
        listener.handleMessage(new MessageEvent("msg", LogLevel.ERROR, null));
        listener.handleMessage(new MessageEvent("msg", LogLevel.FAIL, null));

        verifyZeroInteractions(libImportCollector);
        verifyZeroInteractions(libNameHandler);
    }

    @Test
    public void multipleEventsAreHandledInRightOrder() throws Exception {
        final RobotDryRunLibraryEventListener listener = new RobotDryRunLibraryEventListener(libImportCollector,
                libNameHandler);

        final LibraryImportEvent event1 = new LibraryImportEvent("lib2", new URI("file:///suite1.robot"),
                new URI("file:///lib2.py"), Arrays.asList("a", "b"));
        final MessageEvent event2 = new MessageEvent("import_error_1", LogLevel.NONE, null);
        final MessageEvent event3 = new MessageEvent("import_error_2", LogLevel.NONE, null);
        final LibraryImportEvent event4 = new LibraryImportEvent("lib3", new URI("file:///suite1.robot"),
                new URI("file:///lib3.py"), Arrays.asList("c", "d"));
        final MessageEvent event5 = new MessageEvent("import_error_3", LogLevel.NONE, null);
        final LibraryImportEvent event6 = new LibraryImportEvent("lib1", new URI("file:///other.robot"),
                new URI("file:///lib1.py"), Arrays.asList("x"));

        listener.handleLibraryImport(event1);
        listener.handleMessage(event2);
        listener.handleMessage(event3);
        listener.handleLibraryImport(event4);
        listener.handleMessage(event5);
        listener.handleLibraryImport(event6);

        final InOrder libImportCollectorOrder = inOrder(libImportCollector);
        libImportCollectorOrder.verify(libImportCollector).collectFromLibraryImportEvent(event1);
        libImportCollectorOrder.verify(libImportCollector).collectFromMessageEvent(event2);
        libImportCollectorOrder.verify(libImportCollector).collectFromMessageEvent(event3);
        libImportCollectorOrder.verify(libImportCollector).collectFromLibraryImportEvent(event4);
        libImportCollectorOrder.verify(libImportCollector).collectFromMessageEvent(event5);
        libImportCollectorOrder.verify(libImportCollector).collectFromLibraryImportEvent(event6);
        verifyNoMoreInteractions(libImportCollector);

        final InOrder libNameHandlerOrder = inOrder(libNameHandler);
        libNameHandlerOrder.verify(libNameHandler).accept("lib2");
        libNameHandlerOrder.verify(libNameHandler).accept("lib3");
        libNameHandlerOrder.verify(libNameHandler).accept("lib1");
        verifyNoMoreInteractions(libNameHandler);
    }

}

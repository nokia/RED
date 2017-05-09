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
import java.util.Arrays;
import java.util.function.Consumer;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.rf.ide.core.execution.agent.LogLevel;
import org.rf.ide.core.execution.agent.event.LibraryImportEvent;
import org.rf.ide.core.execution.agent.event.SuiteStartedEvent;

public class RobotDryRunEventListenerTest {

    @Mock
    private Consumer<String> startSuiteHandler;

    @Mock
    private RobotDryRunLibraryImportCollector libImportCollector;

    @Mock
    private RobotDryRunKeywordSourceCollector kwSourceCollector;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void suiteStartingEventIsHandled() throws Exception {
        final RobotDryRunEventListener listener = new RobotDryRunEventListener(libImportCollector, kwSourceCollector,
                startSuiteHandler);

        listener.handleSuiteStarted(
                new SuiteStartedEvent("abc", new URI("file:///path"), 5, newArrayList(), newArrayList()));

        verify(startSuiteHandler).accept("abc");
        verifyNoMoreInteractions(startSuiteHandler);
        verifyZeroInteractions(libImportCollector);
        verifyZeroInteractions(kwSourceCollector);
    }

    @Test
    public void libraryImportIsHandled() throws Exception {
        final RobotDryRunEventListener listener = new RobotDryRunEventListener(libImportCollector, kwSourceCollector,
                startSuiteHandler);

        listener.handleLibraryImport(new LibraryImportEvent("String", new URI("file:///suite.robot"),
                new URI("file:///String.py"), Arrays.asList("a1", "a2")));

        verify(libImportCollector).collectFromLibraryImportEvent(new LibraryImportEvent("String",
                new URI("file:///suite.robot"), new URI("file:///String.py"), Arrays.asList("a1", "a2")));
        verifyNoMoreInteractions(libImportCollector);
        verifyZeroInteractions(startSuiteHandler);
        verifyZeroInteractions(kwSourceCollector);
    }

    @Test
    public void failMessageEventIsHandled() throws Exception {
        final RobotDryRunEventListener listener = new RobotDryRunEventListener(libImportCollector, kwSourceCollector,
                startSuiteHandler);

        listener.handleMessage("fail_message_123", LogLevel.FAIL);

        verify(libImportCollector).collectFromFailMessageEvent("fail_message_123");
        verifyNoMoreInteractions(libImportCollector);
        verifyZeroInteractions(startSuiteHandler);
        verifyZeroInteractions(kwSourceCollector);
    }

    @Test
    public void errorMessageEventIsHandled() throws Exception {
        final RobotDryRunEventListener listener = new RobotDryRunEventListener(libImportCollector, kwSourceCollector,
                startSuiteHandler);

        listener.handleMessage("error_message_456", LogLevel.ERROR);

        verify(libImportCollector).collectFromErrorMessageEvent("error_message_456");
        verifyNoMoreInteractions(libImportCollector);
        verifyZeroInteractions(startSuiteHandler);
        verifyZeroInteractions(kwSourceCollector);
    }

    @Test
    public void keywordMessageEventIsHandled() throws Exception {
        final RobotDryRunEventListener listener = new RobotDryRunEventListener(libImportCollector, kwSourceCollector,
                startSuiteHandler);

        listener.handleMessage("kw_message_789", LogLevel.NONE);

        verify(kwSourceCollector).collectFromMessageEvent("kw_message_789");
        verifyNoMoreInteractions(kwSourceCollector);
        verifyZeroInteractions(startSuiteHandler);
        verifyZeroInteractions(libImportCollector);
    }

    @Test
    public void unsupportedLevelMessageEventsAreIgnored() throws Exception {
        final RobotDryRunEventListener listener = new RobotDryRunEventListener(libImportCollector, kwSourceCollector,
                startSuiteHandler);

        listener.handleMessage("msg", LogLevel.TRACE);
        listener.handleMessage("msg", LogLevel.DEBUG);
        listener.handleMessage("msg", LogLevel.INFO);
        listener.handleMessage("msg", LogLevel.WARN);

        verifyZeroInteractions(startSuiteHandler);
        verifyZeroInteractions(libImportCollector);
        verifyZeroInteractions(kwSourceCollector);
    }

    @Test
    public void multipleEventsAreHandledInRightOrder() throws Exception {
        final RobotDryRunEventListener listener = new RobotDryRunEventListener(libImportCollector, kwSourceCollector,
                startSuiteHandler);

        listener.handleLibraryImport(new LibraryImportEvent("lib2", new URI("file:///suite1.robot"),
                new URI("file:///lib1.py"), Arrays.asList("a", "b")));
        listener.handleMessage("err_1", LogLevel.ERROR);
        listener.handleMessage("fail_2", LogLevel.FAIL);
        listener.handleMessage("fail_1", LogLevel.FAIL);
        listener.handleMessage("err_3", LogLevel.ERROR);
        listener.handleMessage("kw_2", LogLevel.NONE);
        listener.handleMessage("err_2", LogLevel.ERROR);
        listener.handleLibraryImport(new LibraryImportEvent("lib3", new URI("file:///suite1.robot"),
                new URI("file:///lib6.py"), Arrays.asList("c", "d")));
        listener.handleLibraryImport(new LibraryImportEvent("lib1", new URI("file:///other.robot"),
                new URI("file:///lib6.py"), Arrays.asList("x")));
        listener.handleMessage("kw_1", LogLevel.NONE);

        final InOrder inOrder = inOrder(libImportCollector, kwSourceCollector);

        inOrder.verify(libImportCollector).collectFromLibraryImportEvent(new LibraryImportEvent("lib2",
                new URI("file:///suite1.robot"), new URI("file:///lib1.py"), Arrays.asList("a", "b")));
        inOrder.verify(libImportCollector).collectFromErrorMessageEvent("err_1");
        inOrder.verify(libImportCollector).collectFromFailMessageEvent("fail_2");
        inOrder.verify(libImportCollector).collectFromFailMessageEvent("fail_1");
        inOrder.verify(libImportCollector).collectFromErrorMessageEvent("err_3");
        inOrder.verify(kwSourceCollector).collectFromMessageEvent("kw_2");
        inOrder.verify(libImportCollector).collectFromErrorMessageEvent("err_2");
        inOrder.verify(libImportCollector).collectFromLibraryImportEvent(new LibraryImportEvent("lib3",
                new URI("file:///suite1.robot"), new URI("file:///lib6.py"), Arrays.asList("c", "d")));
        inOrder.verify(libImportCollector).collectFromLibraryImportEvent(new LibraryImportEvent("lib1",
                new URI("file:///other.robot"), new URI("file:///lib6.py"), Arrays.asList("x")));
        inOrder.verify(kwSourceCollector).collectFromMessageEvent("kw_1");
        verifyNoMoreInteractions(libImportCollector);
        verifyNoMoreInteractions(kwSourceCollector);
        verifyZeroInteractions(startSuiteHandler);
    }

}

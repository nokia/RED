/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.execution;

import static com.google.common.collect.Lists.newArrayList;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;
import org.rf.ide.core.execution.server.AgentClient;

import com.google.common.collect.ImmutableMap;

public class RobotAgentEventDispatcherTest {

    @Test(expected = IOException.class)
    public void exceptionIsRethrown_whenReaderThrowsIOException() throws Exception {
        final RobotAgentEventDispatcher dispatcher = new RobotAgentEventDispatcher(null);

        final BufferedReader reader = mock(BufferedReader.class);
        when(reader.readLine()).thenThrow(IOException.class);

        dispatcher.runEventsLoop(reader);
    }

    @Test
    public void listenerIsNotNotified_whenThereAreNoEvents() throws Exception {
        final RobotAgentEventListener listener = mock(RobotAgentEventListener.class);
        final RobotAgentEventDispatcher dispatcher = new RobotAgentEventDispatcher(null, listener);

        dispatcher.runEventsLoop(readerFor(""));

        verify(listener).setClient(nullable(AgentClient.class));
        verifyNoMoreInteractions(listener);
    }

    @Test
    public void listenerIsNotNotified_whenUnknownEventComes() throws Exception {
        final RobotAgentEventListener listener = mock(RobotAgentEventListener.class);
        when(listener.isHandlingEvents()).thenReturn(true);

        final RobotAgentEventDispatcher dispatcher = new RobotAgentEventDispatcher(null, listener);

        final String json = toJson(ImmutableMap.of("some_event", "val"));
        dispatcher.runEventsLoop(readerFor(json));

        verify(listener).setClient(nullable(AgentClient.class));
        verify(listener, atLeast(1)).isHandlingEvents();
        verifyNoMoreInteractions(listener);
    }

    @Test
    public void listenerIsNotNotified_whenValidEventComesButListenerDoesNotHandleEventsAnymore() throws Exception {
        final RobotAgentEventListener listener = mock(RobotAgentEventListener.class);
        when(listener.isHandlingEvents()).thenReturn(false);

        final RobotAgentEventDispatcher dispatcher = new RobotAgentEventDispatcher(null, listener);

        final String json = toJson(ImmutableMap.of("pid", 1));
        dispatcher.runEventsLoop(readerFor(json));

        verify(listener).setClient(nullable(AgentClient.class));
        verify(listener, atLeast(1)).isHandlingEvents();
        verifyNoMoreInteractions(listener);
    }

    @Test
    public void whenAtLeastOneListenerIsHandlingEvents_allAreNotifiedAboutIt() throws Exception {
        final RobotAgentEventListener listener1 = mock(RobotAgentEventListener.class);
        final RobotAgentEventListener listener2 = mock(RobotAgentEventListener.class);
        final RobotAgentEventListener listener3 = mock(RobotAgentEventListener.class);
        when(listener1.isHandlingEvents()).thenReturn(false);
        when(listener2.isHandlingEvents()).thenReturn(true);
        when(listener3.isHandlingEvents()).thenReturn(false);

        final RobotAgentEventDispatcher dispatcher = new RobotAgentEventDispatcher(null, listener1, listener2,
                listener3);

        final String json = toJson(ImmutableMap.of("pid", 1));
        dispatcher.runEventsLoop(readerFor(json));

        verify(listener1).setClient(nullable(AgentClient.class));
        verify(listener1, atLeast(0)).isHandlingEvents();
        verify(listener1).handlePid();
        verifyNoMoreInteractions(listener1);

        verify(listener2).setClient(nullable(AgentClient.class));
        verify(listener2, atLeast(1)).isHandlingEvents();
        verify(listener2).handlePid();
        verifyNoMoreInteractions(listener2);

        verify(listener3).setClient(nullable(AgentClient.class));
        verify(listener3, atLeast(0)).isHandlingEvents();
        verify(listener3).handlePid();
        verifyNoMoreInteractions(listener3);
    }

    @Test
    public void listenerIsNotifiedAboutReadyToStartEvent() throws Exception {
        final RobotAgentEventListener listener = mock(RobotAgentEventListener.class);
        when(listener.isHandlingEvents()).thenReturn(true);

        final RobotAgentEventDispatcher dispatcher = new RobotAgentEventDispatcher(null, listener);

        final String json = toJson(ImmutableMap.of("ready_to_start", 0));
        dispatcher.runEventsLoop(readerFor(json));

        verify(listener).setClient(nullable(AgentClient.class));
        verify(listener, atLeast(1)).isHandlingEvents();
        verify(listener).handleAgentIsReadyToStart();
        verifyNoMoreInteractions(listener);
    }

    @Test
    public void listenerIsNotifiedAboutPidEvent() throws Exception {
        final RobotAgentEventListener listener = mock(RobotAgentEventListener.class);
        when(listener.isHandlingEvents()).thenReturn(true);

        final RobotAgentEventDispatcher dispatcher = new RobotAgentEventDispatcher(null, listener);

        final String json = toJson(ImmutableMap.of("pid", 1));
        dispatcher.runEventsLoop(readerFor(json));

        verify(listener).setClient(nullable(AgentClient.class));
        verify(listener, atLeast(1)).isHandlingEvents();
        verify(listener).handlePid();
        verifyNoMoreInteractions(listener);
    }

    @Test
    public void listenerIsNotifiedAboutVersionEvent() throws Exception {
        final RobotAgentEventListener listener = mock(RobotAgentEventListener.class);
        when(listener.isHandlingEvents()).thenReturn(true);

        final RobotAgentEventDispatcher dispatcher = new RobotAgentEventDispatcher(null, listener);

        final Map<String, String> attributes = ImmutableMap.of("python", "py3", "robot", "1.2.3");
        final String json = toJson(ImmutableMap.of("version", newArrayList(attributes)));
        dispatcher.runEventsLoop(readerFor(json));

        verify(listener).setClient(nullable(AgentClient.class));
        verify(listener, atLeast(1)).isHandlingEvents();
        verify(listener).handleVersions("py3", "1.2.3");
        verifyNoMoreInteractions(listener);
    }

    @Test
    public void listenerIsNotifiedAboutResourceImportEvent() throws Exception {
        final RobotAgentEventListener listener = mock(RobotAgentEventListener.class);
        when(listener.isHandlingEvents()).thenReturn(true);

        final RobotAgentEventDispatcher dispatcher = new RobotAgentEventDispatcher(null, listener);

        final Map<String, String> attributes = ImmutableMap.of("source", "/a/b/file.robot");
        final String json = toJson(ImmutableMap.of("resource_import", newArrayList("file", attributes)));
        dispatcher.runEventsLoop(readerFor(json));

        verify(listener).setClient(nullable(AgentClient.class));
        verify(listener, atLeast(1)).isHandlingEvents();
        verify(listener).handleResourceImport(new File("/a/b/file.robot"));
        verifyNoMoreInteractions(listener);
    }

    @Test
    public void listenerIsNotifiedAboutSuiteStartEvent() throws Exception {
        final RobotAgentEventListener listener = mock(RobotAgentEventListener.class);
        when(listener.isHandlingEvents()).thenReturn(true);

        final RobotAgentEventDispatcher dispatcher = new RobotAgentEventDispatcher(null, listener);

        final Map<String, String> attributes = ImmutableMap.of("source", "/a/b/suite.robot");
        final String json = toJson(ImmutableMap.of("start_suite", newArrayList("suite", attributes)));
        dispatcher.runEventsLoop(readerFor(json));

        verify(listener).setClient(nullable(AgentClient.class));
        verify(listener, atLeast(1)).isHandlingEvents();
        verify(listener).handleSuiteStarted("suite", new File("/a/b/suite.robot"));
        verifyNoMoreInteractions(listener);
    }

    @Test
    public void listenerIsNotifiedAboutSuiteEndEvent() throws Exception {
        final RobotAgentEventListener listener = mock(RobotAgentEventListener.class);
        when(listener.isHandlingEvents()).thenReturn(true);

        final RobotAgentEventDispatcher dispatcher = new RobotAgentEventDispatcher(null, listener);

        final Map<String, Object> attributes = ImmutableMap.<String, Object> of("elapsedtime", 10, "message", "msg",
                "status", "PASS");
        final String json = toJson(ImmutableMap.of("end_suite", newArrayList("suite", attributes)));
        dispatcher.runEventsLoop(readerFor(json));

        verify(listener).setClient(nullable(AgentClient.class));
        verify(listener, atLeast(1)).isHandlingEvents();
        verify(listener).handleSuiteEnded("suite", 10, Status.PASS, "msg");
        verifyNoMoreInteractions(listener);
    }

    @Test
    public void listenerIsNotifiedAboutTestStartEvent() throws Exception {
        final RobotAgentEventListener listener = mock(RobotAgentEventListener.class);
        when(listener.isHandlingEvents()).thenReturn(true);

        final RobotAgentEventDispatcher dispatcher = new RobotAgentEventDispatcher(null, listener);

        final Map<String, String> attributes = ImmutableMap.of("longname", "suite-a-b-test");
        final String json = toJson(ImmutableMap.of("start_test", newArrayList("test", attributes)));
        dispatcher.runEventsLoop(readerFor(json));

        verify(listener).setClient(nullable(AgentClient.class));
        verify(listener, atLeast(1)).isHandlingEvents();
        verify(listener).handleTestStarted("test", "suite-a-b-test");
        verifyNoMoreInteractions(listener);
    }

    @Test
    public void listenerIsNotifiedAboutTestEndEvent() throws Exception {
        final RobotAgentEventListener listener = mock(RobotAgentEventListener.class);
        when(listener.isHandlingEvents()).thenReturn(true);

        final RobotAgentEventDispatcher dispatcher = new RobotAgentEventDispatcher(null, listener);

        final Map<String, Object> attributes = ImmutableMap.<String, Object> of("longname", "suite-a-b-test",
                "elapsedtime", 10, "message", "msg", "status", "FAIL");
        final String json = toJson(ImmutableMap.of("end_test", newArrayList("test", attributes)));
        dispatcher.runEventsLoop(readerFor(json));

        verify(listener).setClient(nullable(AgentClient.class));
        verify(listener, atLeast(1)).isHandlingEvents();
        verify(listener).handleTestEnded("test", "suite-a-b-test", 10, Status.FAIL, "msg");
        verifyNoMoreInteractions(listener);
    }

    @Test
    public void listenerIsNotifiedAboutKeywordStartEvent() throws Exception {
        final RobotAgentEventListener listener = mock(RobotAgentEventListener.class);
        when(listener.isHandlingEvents()).thenReturn(true);

        final RobotAgentEventDispatcher dispatcher = new RobotAgentEventDispatcher(null, listener);

        final Map<String, Object> attributes = ImmutableMap.<String, Object> of("type", "Keyword",
                "args", newArrayList("1", "2"));
        final String json = toJson(ImmutableMap.of("start_keyword", newArrayList("kw", attributes)));
        dispatcher.runEventsLoop(readerFor(json));

        verify(listener).setClient(nullable(AgentClient.class));
        verify(listener, atLeast(1)).isHandlingEvents();
        verify(listener).handleKeywordStarted("kw", "Keyword", newArrayList("1", "2"));
        verifyNoMoreInteractions(listener);
    }

    @Test
    public void listenerIsNotifiedAboutKeywordEndEvent() throws Exception {
        final RobotAgentEventListener listener = mock(RobotAgentEventListener.class);
        when(listener.isHandlingEvents()).thenReturn(true);

        final RobotAgentEventDispatcher dispatcher = new RobotAgentEventDispatcher(null, listener);

        final Map<String, Object> attributes = ImmutableMap.<String, Object> of("type", "Setup");
        final String json = toJson(ImmutableMap.of("end_keyword", newArrayList("kw", attributes)));
        dispatcher.runEventsLoop(readerFor(json));

        verify(listener).setClient(nullable(AgentClient.class));
        verify(listener, atLeast(1)).isHandlingEvents();
        verify(listener).handleKeywordEnded("kw", "Setup");
        verifyNoMoreInteractions(listener);
    }

    @Test
    public void listenerIsNotifiedAboutVariablesEvent() throws Exception {
        final RobotAgentEventListener listener = mock(RobotAgentEventListener.class);
        when(listener.isHandlingEvents()).thenReturn(true);

        final RobotAgentEventDispatcher dispatcher = new RobotAgentEventDispatcher(null, listener);

        final Map<String, Object> attributes = ImmutableMap.<String, Object> of("a", "value", "b",
                newArrayList("1", "2"));
        final String json = toJson(ImmutableMap.of("vars", newArrayList("_", attributes)));
        dispatcher.runEventsLoop(readerFor(json));

        verify(listener).setClient(nullable(AgentClient.class));
        verify(listener, atLeast(1)).isHandlingEvents();
        verify(listener).handleVariables(ImmutableMap.<String, Object> of("a", "value", "b", newArrayList("1", "2")));
        verifyNoMoreInteractions(listener);
    }

    @Test
    public void listenerIsNotifiedAboutGlobalVariablesEvent() throws Exception {
        final RobotAgentEventListener listener = mock(RobotAgentEventListener.class);
        when(listener.isHandlingEvents()).thenReturn(true);

        final RobotAgentEventDispatcher dispatcher = new RobotAgentEventDispatcher(null, listener);

        final Map<String, String> attributes = ImmutableMap.of("a", "value", "b", "other_value");
        final String json = toJson(ImmutableMap.of("global_vars", newArrayList("_", attributes)));
        dispatcher.runEventsLoop(readerFor(json));

        verify(listener).setClient(nullable(AgentClient.class));
        verify(listener, atLeast(1)).isHandlingEvents();
        verify(listener).handleGlobalVariables(ImmutableMap.of("a", "value", "b", "other_value"));
        verifyNoMoreInteractions(listener);
    }

    @Test
    public void listenerIsNotifiedAboutCheckConditionEvent() throws Exception {
        final RobotAgentEventListener listener = mock(RobotAgentEventListener.class);
        when(listener.isHandlingEvents()).thenReturn(true);

        final RobotAgentEventDispatcher dispatcher = new RobotAgentEventDispatcher(null, listener);

        final String json = toJson(ImmutableMap.of("check_condition", 0));
        dispatcher.runEventsLoop(readerFor(json));

        verify(listener).setClient(nullable(AgentClient.class));
        verify(listener, atLeast(1)).isHandlingEvents();
        verify(listener).handleCheckCondition();
        verifyNoMoreInteractions(listener);
    }

    @Test
    public void listenerIsNotifiedAboutConditionResultEvent() throws Exception {
        final RobotAgentEventListener listener = mock(RobotAgentEventListener.class);
        when(listener.isHandlingEvents()).thenReturn(true);

        final RobotAgentEventDispatcher dispatcher = new RobotAgentEventDispatcher(null, listener);

        final String json = toJson(ImmutableMap.of("condition_result", newArrayList(true)));
        dispatcher.runEventsLoop(readerFor(json));

        verify(listener).setClient(nullable(AgentClient.class));
        verify(listener, atLeast(1)).isHandlingEvents();
        verify(listener).handleConditionResult(true);
        verifyNoMoreInteractions(listener);
    }

    @Test
    public void listenerIsNotifiedAboutConditionErrorEvent() throws Exception {
        final RobotAgentEventListener listener = mock(RobotAgentEventListener.class);
        when(listener.isHandlingEvents()).thenReturn(true);

        final RobotAgentEventDispatcher dispatcher = new RobotAgentEventDispatcher(null, listener);

        final String json = toJson(ImmutableMap.of("condition_error", newArrayList("error message")));
        dispatcher.runEventsLoop(readerFor(json));

        verify(listener).setClient(nullable(AgentClient.class));
        verify(listener, atLeast(1)).isHandlingEvents();
        verify(listener).handleConditionError("error message");
        verifyNoMoreInteractions(listener);
    }

    @Test
    public void listenerIsNotifiedAboutCheckedConditionEvent() throws Exception {
        final RobotAgentEventListener listener = mock(RobotAgentEventListener.class);
        when(listener.isHandlingEvents()).thenReturn(true);

        final RobotAgentEventDispatcher dispatcher = new RobotAgentEventDispatcher(null, listener);

        final String json = toJson(ImmutableMap.of("condition_checked", 0));
        dispatcher.runEventsLoop(readerFor(json));

        verify(listener).setClient(nullable(AgentClient.class));
        verify(listener, atLeast(1)).isHandlingEvents();
        verify(listener).handleConditionChecked();
        verifyNoMoreInteractions(listener);
    }

    @Test
    public void listenerIsNotifiedAboutPausedEvent() throws Exception {
        final RobotAgentEventListener listener = mock(RobotAgentEventListener.class);
        when(listener.isHandlingEvents()).thenReturn(true);

        final RobotAgentEventDispatcher dispatcher = new RobotAgentEventDispatcher(null, listener);

        final String json = toJson(ImmutableMap.of("paused", 0));
        dispatcher.runEventsLoop(readerFor(json));

        verify(listener).setClient(nullable(AgentClient.class));
        verify(listener, atLeast(1)).isHandlingEvents();
        verify(listener).handlePaused();
        verifyNoMoreInteractions(listener);
    }

    @Test
    public void listenerIsNotifiedAboutClosedEvent() throws Exception {
        final RobotAgentEventListener listener = mock(RobotAgentEventListener.class);
        when(listener.isHandlingEvents()).thenReturn(true);

        final RobotAgentEventDispatcher dispatcher = new RobotAgentEventDispatcher(null, listener);

        final String json = toJson(ImmutableMap.of("close", 0));
        dispatcher.runEventsLoop(readerFor(json));

        verify(listener).setClient(nullable(AgentClient.class));
        verify(listener, atLeast(1)).isHandlingEvents();
        verify(listener).handleClosed();
        verifyNoMoreInteractions(listener);
    }

    @Test
    public void listenerIsNotifiedAboutLogMessageEvent() throws Exception {
        final RobotAgentEventListener listener = mock(RobotAgentEventListener.class);
        when(listener.isHandlingEvents()).thenReturn(true);

        final RobotAgentEventDispatcher dispatcher = new RobotAgentEventDispatcher(null, listener);

        final Object attributes = ImmutableMap.of("message", "msg", "timestamp", "time", "level", "INFO");
        final String json = toJson(ImmutableMap.of("log_message", newArrayList(attributes)));
        dispatcher.runEventsLoop(readerFor(json));

        verify(listener).setClient(nullable(AgentClient.class));
        verify(listener, atLeast(1)).isHandlingEvents();
        verify(listener).handleLogMessage("msg", LogLevel.INFO, "time");
        verifyNoMoreInteractions(listener);
    }

    @Test
    public void listenerIsNotifiedAboutOutputFileEvent() throws Exception {
        final RobotAgentEventListener listener = mock(RobotAgentEventListener.class);
        when(listener.isHandlingEvents()).thenReturn(true);

        final RobotAgentEventDispatcher dispatcher = new RobotAgentEventDispatcher(null, listener);

        final String json = toJson(ImmutableMap.of("output_file", newArrayList("/a/b/file.xml")));
        dispatcher.runEventsLoop(readerFor(json));

        verify(listener).setClient(nullable(AgentClient.class));
        verify(listener, atLeast(1)).isHandlingEvents();
        verify(listener).handleOutputFile(new File("/a/b/file.xml"));
        verifyNoMoreInteractions(listener);
    }

    private static String toJson(final Object object) throws Exception {
        return new ObjectMapper().writeValueAsString(object);
    }

    private static BufferedReader readerFor(final String content) {
        return new BufferedReader(new StringReader(content));
    }
}

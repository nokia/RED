/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.execution.server;

import static com.google.common.collect.Lists.newArrayList;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;
import org.rf.ide.core.execution.agent.LogLevel;
import org.rf.ide.core.execution.agent.PausingPoint;
import org.rf.ide.core.execution.agent.RobotAgentEventListener;
import org.rf.ide.core.execution.agent.Status;
import org.rf.ide.core.execution.agent.event.AgentInitializingEvent;
import org.rf.ide.core.execution.agent.event.ConditionEvaluatedEvent;
import org.rf.ide.core.execution.agent.event.KeywordEndedEvent;
import org.rf.ide.core.execution.agent.event.KeywordStartedEvent;
import org.rf.ide.core.execution.agent.event.LibraryImportEvent;
import org.rf.ide.core.execution.agent.event.MessageEvent;
import org.rf.ide.core.execution.agent.event.OutputFileEvent;
import org.rf.ide.core.execution.agent.event.PausedEvent;
import org.rf.ide.core.execution.agent.event.ReadyToStartEvent;
import org.rf.ide.core.execution.agent.event.ResourceImportEvent;
import org.rf.ide.core.execution.agent.event.ShouldContinueEvent;
import org.rf.ide.core.execution.agent.event.SuiteEndedEvent;
import org.rf.ide.core.execution.agent.event.SuiteStartedEvent;
import org.rf.ide.core.execution.agent.event.TestEndedEvent;
import org.rf.ide.core.execution.agent.event.TestStartedEvent;
import org.rf.ide.core.execution.agent.event.Variable;
import org.rf.ide.core.execution.agent.event.VariableTypedValue;
import org.rf.ide.core.execution.agent.event.VariablesEvent;
import org.rf.ide.core.execution.agent.event.VersionsEvent;
import org.rf.ide.core.testdata.model.table.variables.AVariable.VariableScope;

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
    public void listenerIsNotifiedOnlyAboutFinishedProcessing_whenExceptionIsThrown() throws Exception {
        final RobotAgentEventListener listener = mock(RobotAgentEventListener.class);
        when(listener.isHandlingEvents()).thenReturn(true);
        doThrow(RuntimeException.class).when(listener).handleResumed();

        final RobotAgentEventDispatcher dispatcher = new RobotAgentEventDispatcher(null, listener);

        final String json = toJson(ImmutableMap.of("resumed", 0));

        try {
            dispatcher.runEventsLoop(readerFor(json));
        } catch (final RuntimeException e) {
            // that's expected
        }

        verify(listener).eventsProcessingAboutToStart();
        verify(listener, atLeast(1)).isHandlingEvents();
        verify(listener).handleResumed();
        verify(listener).eventsProcessingFinished();
        verifyZeroInteractions(listener);
    }

    @Test
    public void listenerIsNotNotified_whenThereAreNoEvents() throws Exception {
        final RobotAgentEventListener listener = mock(RobotAgentEventListener.class);
        final RobotAgentEventDispatcher dispatcher = new RobotAgentEventDispatcher(null, listener);

        dispatcher.runEventsLoop(readerFor(""));

        verify(listener).eventsProcessingAboutToStart();
        verify(listener).eventsProcessingFinished();
        verifyZeroInteractions(listener);
    }

    @Test
    public void listenerIsNotNotified_whenThereIsOnlyNullEventRead() throws Exception {
        final RobotAgentEventListener listener = mock(RobotAgentEventListener.class);
        when(listener.isHandlingEvents()).thenReturn(true);

        final RobotAgentEventDispatcher dispatcher = new RobotAgentEventDispatcher(null, listener);

        dispatcher.runEventsLoop(readerFor(toJson(null)));

        verify(listener).eventsProcessingAboutToStart();
        verify(listener, atLeast(1)).isHandlingEvents();
        verify(listener).eventsProcessingFinished();
        verifyNoMoreInteractions(listener);
    }

    @Test
    public void listenerIsNotNotified_whenUnknownEventComes() throws Exception {
        final RobotAgentEventListener listener = mock(RobotAgentEventListener.class);
        when(listener.isHandlingEvents()).thenReturn(true);

        final RobotAgentEventDispatcher dispatcher = new RobotAgentEventDispatcher(null, listener);

        final String json = toJson(ImmutableMap.of("some_event", "val"));
        dispatcher.runEventsLoop(readerFor(json));

        verify(listener).eventsProcessingAboutToStart();
        verify(listener, atLeast(1)).isHandlingEvents();
        verify(listener).eventsProcessingFinished();
        verifyNoMoreInteractions(listener);
    }

    @Test
    public void listenerIsNotNotified_whenValidEventComesButListenerDoesNotHandleEventsAnymore() throws Exception {
        final RobotAgentEventListener listener = mock(RobotAgentEventListener.class);
        when(listener.isHandlingEvents()).thenReturn(false);

        final RobotAgentEventDispatcher dispatcher = new RobotAgentEventDispatcher(null, listener);

        final String json = toJson(ImmutableMap.of("pid", 1));
        dispatcher.runEventsLoop(readerFor(json));

        verify(listener).eventsProcessingAboutToStart();
        verify(listener, atLeast(1)).isHandlingEvents();
        verify(listener).eventsProcessingFinished();
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

        final String json = toJson(ImmutableMap.of("ready_to_start", 1));
        dispatcher.runEventsLoop(readerFor(json));

        verify(listener1).eventsProcessingAboutToStart();
        verify(listener1, atLeast(0)).isHandlingEvents();
        verify(listener1).handleAgentIsReadyToStart(new ReadyToStartEvent(null));
        verify(listener1).eventsProcessingFinished();
        verifyNoMoreInteractions(listener1);

        verify(listener2).eventsProcessingAboutToStart();
        verify(listener2, atLeast(1)).isHandlingEvents();
        verify(listener2).handleAgentIsReadyToStart(new ReadyToStartEvent(null));
        verify(listener2).eventsProcessingFinished();
        verifyNoMoreInteractions(listener2);

        verify(listener3).eventsProcessingAboutToStart();
        verify(listener3, atLeast(0)).isHandlingEvents();
        verify(listener3).handleAgentIsReadyToStart(new ReadyToStartEvent(null));
        verify(listener3).eventsProcessingFinished();
        verifyNoMoreInteractions(listener3);
    }

    @Test
    public void listenerIsNotifiedAboutReadyToStartEvent() throws Exception {
        final RobotAgentEventListener listener = mock(RobotAgentEventListener.class);
        when(listener.isHandlingEvents()).thenReturn(true);

        final RobotAgentEventDispatcher dispatcher = new RobotAgentEventDispatcher(null, listener);

        final String json = toJson(ImmutableMap.of("ready_to_start", 0));
        dispatcher.runEventsLoop(readerFor(json));

        verify(listener).eventsProcessingAboutToStart();
        verify(listener, atLeast(1)).isHandlingEvents();
        verify(listener).handleAgentIsReadyToStart(new ReadyToStartEvent(null));
        verify(listener).eventsProcessingFinished();
        verifyNoMoreInteractions(listener);
    }

    @Test
    public void listenerIsNotifiedAboutAgentInitializingEvent() throws Exception {
        final RobotAgentEventListener listener = mock(RobotAgentEventListener.class);
        when(listener.isHandlingEvents()).thenReturn(true);

        final RobotAgentEventDispatcher dispatcher = new RobotAgentEventDispatcher(null, listener);

        final String json = toJson(ImmutableMap.of("agent_initializing", newArrayList()));
        dispatcher.runEventsLoop(readerFor(json));

        verify(listener).eventsProcessingAboutToStart();
        verify(listener, atLeast(1)).isHandlingEvents();
        verify(listener).handleAgentInitializing(new AgentInitializingEvent(null));
        verify(listener).eventsProcessingFinished();
        verifyNoMoreInteractions(listener);
    }

    @Test
    public void listenerIsNotifiedAboutVersionEvent() throws Exception {
        final RobotAgentEventListener listener = mock(RobotAgentEventListener.class);
        when(listener.isHandlingEvents()).thenReturn(true);

        final RobotAgentEventDispatcher dispatcher = new RobotAgentEventDispatcher(mock(AgentClient.class), listener);

        final Map<String, Object> attributes = ImmutableMap.of("cmd_line", "cmdLine", "python", "py3", "robot", "1.2.3",
                "protocol", 2, "pid", 42);
        final String json = toJson(ImmutableMap.of("version", newArrayList(attributes)));
        dispatcher.runEventsLoop(readerFor(json));

        verify(listener).eventsProcessingAboutToStart();
        verify(listener, atLeast(1)).isHandlingEvents();
        verify(listener).handleVersions(new VersionsEvent(null, "cmdLine", "py3", "1.2.3", 2, Optional.of(42L)));
        verify(listener).eventsProcessingFinished();
        verifyNoMoreInteractions(listener);
    }

    @Test
    public void listenerIsNotifiedAboutResourceImportEvent() throws Exception {
        final RobotAgentEventListener listener = mock(RobotAgentEventListener.class);
        when(listener.isHandlingEvents()).thenReturn(true);

        final RobotAgentEventDispatcher dispatcher = new RobotAgentEventDispatcher(null, listener);

        final Map<String, String> attributes = ImmutableMap.of("source", "/a/b/file.robot", "importer",
                "/a/b/suite.robot");
        final String json = toJson(ImmutableMap.of("resource_import", newArrayList("file", attributes)));
        dispatcher.runEventsLoop(readerFor(json));

        verify(listener).eventsProcessingAboutToStart();
        verify(listener, atLeast(1)).isHandlingEvents();
        verify(listener).handleResourceImport(
                new ResourceImportEvent(new URI("file:///a/b/file.robot"), new URI("file:///a/b/suite.robot")));
        verify(listener).eventsProcessingFinished();
        verifyNoMoreInteractions(listener);
    }

    @Test
    public void listenerIsNotifiedAboutSuiteStartEvent() throws Exception {
        final RobotAgentEventListener listener = mock(RobotAgentEventListener.class);
        when(listener.isHandlingEvents()).thenReturn(true);

        final RobotAgentEventDispatcher dispatcher = new RobotAgentEventDispatcher(null, listener);

        final Map<String, Object> attributes = new HashMap<>();
        attributes.put("source", "/a/b/suite.robot");
        attributes.put("is_dir", false);
        attributes.put("suites", newArrayList("s1", "s2"));
        attributes.put("tests", newArrayList("t1", "t2"));
        attributes.put("totaltests", 7);
        attributes.put("vars_scopes",
                newArrayList(ImmutableMap.of("a", newArrayList("t", 1, "global"), "b", newArrayList("t", 2, "local"))));
        final String json = toJson(ImmutableMap.of("start_suite", newArrayList("suite", attributes)));
        dispatcher.runEventsLoop(readerFor(json));

        verify(listener).eventsProcessingAboutToStart();
        verify(listener, atLeast(1)).isHandlingEvents();
        verify(listener).handleSuiteStarted(new SuiteStartedEvent("suite", new URI("file:///a/b/suite.robot"), false, 7,
                newArrayList("s1", "s2"), newArrayList("t1", "t2")));
        verify(listener).eventsProcessingFinished();
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

        verify(listener).eventsProcessingAboutToStart();
        verify(listener, atLeast(1)).isHandlingEvents();
        verify(listener).handleSuiteEnded(new SuiteEndedEvent("suite", 10, Status.PASS, "msg"));
        verify(listener).eventsProcessingFinished();
        verifyNoMoreInteractions(listener);
    }

    @Test
    public void listenerIsNotifiedAboutTestStartEvent() throws Exception {
        final RobotAgentEventListener listener = mock(RobotAgentEventListener.class);
        when(listener.isHandlingEvents()).thenReturn(true);

        final RobotAgentEventDispatcher dispatcher = new RobotAgentEventDispatcher(null, listener);

        final Map<String, Object> attributes = ImmutableMap.of("longname", "suite-a-b-test", "vars_scopes",
                newArrayList(ImmutableMap.of("a", newArrayList("t", 1, "test"), "b", newArrayList("t", 2, "local"))),
                "template", "");
        final String json = toJson(ImmutableMap.of("start_test", newArrayList("test", attributes)));
        dispatcher.runEventsLoop(readerFor(json));

        verify(listener).eventsProcessingAboutToStart();
        verify(listener, atLeast(1)).isHandlingEvents();
        verify(listener).handleTestStarted(new TestStartedEvent("test", "suite-a-b-test", ""));
        verify(listener).eventsProcessingFinished();
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

        verify(listener).eventsProcessingAboutToStart();
        verify(listener, atLeast(1)).isHandlingEvents();
        verify(listener).handleTestEnded(new TestEndedEvent("test", "suite-a-b-test", 10, Status.FAIL, "msg"));
        verify(listener).eventsProcessingFinished();
        verifyNoMoreInteractions(listener);
    }

    @Test
    public void listenerIsNotifiedAboutPreKeywordStartEvent() throws Exception {
        final RobotAgentEventListener listener = mock(RobotAgentEventListener.class);
        when(listener.isHandlingEvents()).thenReturn(true);

        final RobotAgentEventDispatcher dispatcher = new RobotAgentEventDispatcher(null, listener);

        final Map<String, Object> attributes = ImmutableMap.<String, Object> of("kwname", "kw", "type", "Keyword",
                "libname", "lib", "vars_scopes",
                newArrayList(ImmutableMap.of("a", newArrayList("t", 1, "global"), "b", newArrayList("t", 2, "suite"))));
        final String json = toJson(ImmutableMap.of("pre_start_keyword", newArrayList("kw", attributes)));
        dispatcher.runEventsLoop(readerFor(json));

        verify(listener).eventsProcessingAboutToStart();
        verify(listener, atLeast(1)).isHandlingEvents();
        verify(listener).handleKeywordAboutToStart(new KeywordStartedEvent("kw", "Keyword", "lib"));
        verify(listener).eventsProcessingFinished();
        verifyNoMoreInteractions(listener);
    }

    @Test
    public void listenerIsNotifiedAboutKeywordStartEvent() throws Exception {
        final RobotAgentEventListener listener = mock(RobotAgentEventListener.class);
        when(listener.isHandlingEvents()).thenReturn(true);

        final RobotAgentEventDispatcher dispatcher = new RobotAgentEventDispatcher(null, listener);

        final Map<String, Object> attributes = ImmutableMap.<String, Object> of("kwname", "kw", "type", "Keyword",
                "libname", "lib", "vars_scopes",
                newArrayList(ImmutableMap.of("a", newArrayList("t", 1, "global"), "b", newArrayList("t", 2, "suite"))));
        final String json = toJson(ImmutableMap.of("start_keyword", newArrayList("kw", attributes)));
        dispatcher.runEventsLoop(readerFor(json));

        verify(listener).eventsProcessingAboutToStart();
        verify(listener, atLeast(1)).isHandlingEvents();
        verify(listener).handleKeywordStarted(new KeywordStartedEvent("kw", "Keyword", "lib"));
        verify(listener).eventsProcessingFinished();
        verifyNoMoreInteractions(listener);
    }

    @Test
    public void listenerIsNotifiedAboutPreKeywordEndEvent() throws Exception {
        final RobotAgentEventListener listener = mock(RobotAgentEventListener.class);
        when(listener.isHandlingEvents()).thenReturn(true);

        final RobotAgentEventDispatcher dispatcher = new RobotAgentEventDispatcher(null, listener);

        final Map<String, Object> attributes = ImmutableMap.<String, Object> of("kwname", "kw", "type", "Setup");
        final String json = toJson(ImmutableMap.of("pre_end_keyword", newArrayList("_", attributes)));
        dispatcher.runEventsLoop(readerFor(json));

        verify(listener).eventsProcessingAboutToStart();
        verify(listener, atLeast(1)).isHandlingEvents();
        verify(listener).handleKeywordAboutToEnd(new KeywordEndedEvent("kw", "Setup"));
        verify(listener).eventsProcessingFinished();
        verifyNoMoreInteractions(listener);
    }

    @Test
    public void listenerIsNotifiedAboutKeywordEndEvent() throws Exception {
        final RobotAgentEventListener listener = mock(RobotAgentEventListener.class);
        when(listener.isHandlingEvents()).thenReturn(true);

        final RobotAgentEventDispatcher dispatcher = new RobotAgentEventDispatcher(null, listener);

        final Map<String, Object> attributes = ImmutableMap.<String, Object> of("kwname", "kw", "type", "Setup");
        final String json = toJson(ImmutableMap.of("end_keyword", newArrayList("_", attributes)));
        dispatcher.runEventsLoop(readerFor(json));

        verify(listener).eventsProcessingAboutToStart();
        verify(listener, atLeast(1)).isHandlingEvents();
        verify(listener).handleKeywordEnded(new KeywordEndedEvent("kw", "Setup"));
        verify(listener).eventsProcessingFinished();
        verifyNoMoreInteractions(listener);
    }

    @Test
    public void listenerIsNotifiedAboutVariablesChangedEvent() throws Exception {
        final RobotAgentEventListener listener = mock(RobotAgentEventListener.class);
        when(listener.isHandlingEvents()).thenReturn(true);

        final RobotAgentEventDispatcher dispatcher = new RobotAgentEventDispatcher(null, listener);

        final List<Map<String, Object>> attributes = newArrayList(
                ImmutableMap.of("a", newArrayList("t", 1, "test"), "b", newArrayList("t", 2, "suite")));
        final String json = toJson(
                ImmutableMap.of("variables", newArrayList(ImmutableMap.of("var_scopes", attributes))));
        dispatcher.runEventsLoop(readerFor(json));

        verify(listener).eventsProcessingAboutToStart();
        verify(listener, atLeast(1)).isHandlingEvents();
        verify(listener).handleVariables(new VariablesEvent(
                newArrayList(ImmutableMap.of(new Variable("a", VariableScope.TEST_CASE), new VariableTypedValue("t", 1),
                        new Variable("b", VariableScope.TEST_SUITE), new VariableTypedValue("t", 2))),
                null));
        verify(listener).eventsProcessingFinished();
        verifyNoMoreInteractions(listener);
    }

    @Test
    public void listenerIsNotifiedAboutShouldContinueConditionEvent() throws Exception {
        final RobotAgentEventListener listener = mock(RobotAgentEventListener.class);
        when(listener.isHandlingEvents()).thenReturn(true);

        final RobotAgentEventDispatcher dispatcher = new RobotAgentEventDispatcher(null, listener);

        final Map<String, String> attributes = ImmutableMap.of("pausing_point", "START_KEYWORD");
        final String json = toJson(ImmutableMap.of("should_continue", newArrayList(attributes)));
        dispatcher.runEventsLoop(readerFor(json));

        verify(listener).eventsProcessingAboutToStart();
        verify(listener, atLeast(1)).isHandlingEvents();
        verify(listener).handleShouldContinue(new ShouldContinueEvent(null, PausingPoint.START_KEYWORD));
        verify(listener).eventsProcessingFinished();
        verifyNoMoreInteractions(listener);
    }

    @Test
    public void listenerIsNotifiedAboutConditionResultEvent() throws Exception {
        final RobotAgentEventListener listener = mock(RobotAgentEventListener.class);
        when(listener.isHandlingEvents()).thenReturn(true);

        final RobotAgentEventDispatcher dispatcher = new RobotAgentEventDispatcher(null, listener);

        final ImmutableMap<String, Boolean> attributes = ImmutableMap.of("result", Boolean.TRUE);
        final String json = toJson(ImmutableMap.of("condition_result", newArrayList(attributes)));
        dispatcher.runEventsLoop(readerFor(json));

        verify(listener).eventsProcessingAboutToStart();
        verify(listener, atLeast(1)).isHandlingEvents();
        verify(listener).handleConditionEvaluated(new ConditionEvaluatedEvent(true));
        verify(listener).eventsProcessingFinished();
        verifyNoMoreInteractions(listener);
    }

    @Test
    public void listenerIsNotifiedAboutConditionErrorEvent() throws Exception {
        final RobotAgentEventListener listener = mock(RobotAgentEventListener.class);
        when(listener.isHandlingEvents()).thenReturn(true);

        final RobotAgentEventDispatcher dispatcher = new RobotAgentEventDispatcher(null, listener);

        final ImmutableMap<String, String> attributes = ImmutableMap.of("error", "Error evaluating condition");
        final String json = toJson(ImmutableMap.of("condition_result", newArrayList(attributes)));
        dispatcher.runEventsLoop(readerFor(json));

        verify(listener).eventsProcessingAboutToStart();
        verify(listener, atLeast(1)).isHandlingEvents();
        verify(listener).handleConditionEvaluated(new ConditionEvaluatedEvent("Error evaluating condition"));
        verify(listener).eventsProcessingFinished();
        verifyNoMoreInteractions(listener);
    }

    @Test
    public void listenerIsNotifiedAboutPausedEvent() throws Exception {
        final RobotAgentEventListener listener = mock(RobotAgentEventListener.class);
        when(listener.isHandlingEvents()).thenReturn(true);

        final RobotAgentEventDispatcher dispatcher = new RobotAgentEventDispatcher(null, listener);

        final String json = toJson(ImmutableMap.of("paused", 0));
        dispatcher.runEventsLoop(readerFor(json));

        verify(listener).eventsProcessingAboutToStart();
        verify(listener, atLeast(1)).isHandlingEvents();
        verify(listener).handlePaused(new PausedEvent(null));
        verify(listener).eventsProcessingFinished();
        verifyNoMoreInteractions(listener);
    }

    @Test
    public void listenerIsNotifiedAboutResumedEvent() throws Exception {
        final RobotAgentEventListener listener = mock(RobotAgentEventListener.class);
        when(listener.isHandlingEvents()).thenReturn(true);

        final RobotAgentEventDispatcher dispatcher = new RobotAgentEventDispatcher(null, listener);

        final String json = toJson(ImmutableMap.of("resumed", 0));
        dispatcher.runEventsLoop(readerFor(json));

        verify(listener).eventsProcessingAboutToStart();
        verify(listener, atLeast(1)).isHandlingEvents();
        verify(listener).handleResumed();
        verify(listener).eventsProcessingFinished();
        verifyNoMoreInteractions(listener);
    }

    @Test
    public void listenerIsNotifiedAboutClosedEvent() throws Exception {
        final RobotAgentEventListener listener = mock(RobotAgentEventListener.class);
        when(listener.isHandlingEvents()).thenReturn(true);

        final RobotAgentEventDispatcher dispatcher = new RobotAgentEventDispatcher(null, listener);

        final String json = toJson(ImmutableMap.of("close", 0));
        dispatcher.runEventsLoop(readerFor(json));

        verify(listener).eventsProcessingAboutToStart();
        verify(listener, atLeast(1)).isHandlingEvents();
        verify(listener).handleClosed();
        verify(listener).eventsProcessingFinished();
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

        verify(listener).eventsProcessingAboutToStart();
        verify(listener, atLeast(1)).isHandlingEvents();
        verify(listener).handleLogMessage(new MessageEvent("msg", LogLevel.INFO, "time"));
        verify(listener).eventsProcessingFinished();
        verifyNoMoreInteractions(listener);
    }

    @Test
    public void listenerIsNotifiedAboutOutputFileEvent() throws Exception {
        final RobotAgentEventListener listener = mock(RobotAgentEventListener.class);
        when(listener.isHandlingEvents()).thenReturn(true);

        final RobotAgentEventDispatcher dispatcher = new RobotAgentEventDispatcher(null, listener);

        final String json = toJson(ImmutableMap.of("output_file", newArrayList("/a/b/file.xml")));
        dispatcher.runEventsLoop(readerFor(json));

        verify(listener).eventsProcessingAboutToStart();
        verify(listener, atLeast(1)).isHandlingEvents();
        verify(listener).handleOutputFile(new OutputFileEvent(new URI("file:///a/b/file.xml")));
        verify(listener).eventsProcessingFinished();
        verifyNoMoreInteractions(listener);
    }

    @Test
    public void listenerIsNotifiedAboutLibraryImportEvent() throws Exception {
        final RobotAgentEventListener listener = mock(RobotAgentEventListener.class);
        when(listener.isHandlingEvents()).thenReturn(true);

        final RobotAgentEventDispatcher dispatcher = new RobotAgentEventDispatcher(null, listener);

        final Object attributes = ImmutableMap.of("importer", "/importerPath", "source", "/sourcePath", "args",
                newArrayList("arg1", "arg2"), "originalname", "lib1");
        final String json = toJson(ImmutableMap.of("library_import", newArrayList("lib1", attributes)));
        dispatcher.runEventsLoop(readerFor(json));

        verify(listener).eventsProcessingAboutToStart();
        verify(listener, atLeast(1)).isHandlingEvents();
        verify(listener).handleLibraryImport(new LibraryImportEvent("lib1", new URI("file:///importerPath"),
                new URI("file:///sourcePath"), newArrayList("arg1", "arg2")));
        verify(listener).eventsProcessingFinished();
        verifyNoMoreInteractions(listener);
    }

    @Test
    public void listenerIsNotifiedAboutLibraryImportEvent_whenOriginalNameExists() throws Exception {
        final RobotAgentEventListener listener = mock(RobotAgentEventListener.class);
        when(listener.isHandlingEvents()).thenReturn(true);

        final RobotAgentEventDispatcher dispatcher = new RobotAgentEventDispatcher(null, listener);

        final Object attributes = ImmutableMap.of("importer", "/importerPath", "source", "/sourcePath", "args",
                newArrayList("arg1", "arg2"), "originalname", "lib2");
        final String json = toJson(ImmutableMap.of("library_import", newArrayList("lib1", attributes)));
        dispatcher.runEventsLoop(readerFor(json));

        verify(listener).eventsProcessingAboutToStart();
        verify(listener, atLeast(1)).isHandlingEvents();
        verify(listener).handleLibraryImport(new LibraryImportEvent("lib2", new URI("file:///importerPath"),
                new URI("file:///sourcePath"), newArrayList("arg1", "arg2")));
        verify(listener).eventsProcessingFinished();
        verifyNoMoreInteractions(listener);
    }

    @Test
    public void listenerIsNotifiedAboutMessageEvent() throws Exception {
        final RobotAgentEventListener listener = mock(RobotAgentEventListener.class);
        when(listener.isHandlingEvents()).thenReturn(true);

        final RobotAgentEventDispatcher dispatcher = new RobotAgentEventDispatcher(null, listener);

        final Object attributes = ImmutableMap.of("message", "abc", "timestamp", "time", "level", "ERROR");
        final String json = toJson(ImmutableMap.of("message", newArrayList(attributes)));
        dispatcher.runEventsLoop(readerFor(json));

        verify(listener).eventsProcessingAboutToStart();
        verify(listener, atLeast(1)).isHandlingEvents();
        verify(listener).handleMessage(new MessageEvent("abc", LogLevel.ERROR, "time"));
        verify(listener).eventsProcessingFinished();
        verifyNoMoreInteractions(listener);
    }

    private static String toJson(final Object object) throws Exception {
        return new ObjectMapper().writeValueAsString(object);
    }

    private static BufferedReader readerFor(final String content) {
        return new BufferedReader(new StringReader(content));
    }
}

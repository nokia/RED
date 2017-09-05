/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.execution.server;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.synchronizedList;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.rf.ide.core.execution.agent.RobotAgentEventListener;
import org.rf.ide.core.execution.agent.RobotAgentEventListener.RobotAgentEventsListenerException;
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
import org.rf.ide.core.execution.agent.event.VariablesEvent;
import org.rf.ide.core.execution.agent.event.VersionsEvent;

import com.google.common.collect.Iterables;

class RobotAgentEventDispatcher {

    private final List<RobotAgentEventListener> eventsListeners;

    private final AgentClient client;

    RobotAgentEventDispatcher(final AgentClient client, final RobotAgentEventListener... eventsListeners) {
        this.client = client;
        this.eventsListeners = synchronizedList(newArrayList(eventsListeners));
    }

    void runEventsLoop(final BufferedReader eventReader) throws IOException, RobotAgentEventsListenerException {
        try {
            eventsLoop(eventReader);
        } finally {
            for (final RobotAgentEventListener listener : eventsListeners) {
                listener.eventsProcessingFinished();
            }
        }
    }

    private void eventsLoop(final BufferedReader eventReader) throws IOException, RobotAgentEventsListenerException {
        String event = eventReader.readLine();
        final ObjectMapper mapper = new ObjectMapper();
        while (event != null && anyListenerIsHandlingEvents()) {

            final TypeReference<Map<String, Object>> stringToObjectMapType = new TypeReference<Map<String, Object>>() {
            };
            final Map<String, Object> eventMap = mapper.readValue(event, stringToObjectMapType);
            final String eventType = getEventType(eventMap);
            if (eventType == null) {
                event = eventReader.readLine();
                continue;
            }

            switch (eventType) {
                case "agent_initializing":
                    handleAgentInitializing();
                    break;
                case "version":
                    handleVersion(eventMap);
                    break;
                case "ready_to_start":
                    handleReadyToStart();
                    break;
                case "resource_import":
                    handleResourceImport(eventMap);
                    break;
                case "library_import":
                    handleLibraryImport(eventMap);
                    break;
                case "start_suite":
                    handleStartSuite(eventMap);
                    break;
                case "end_suite":
                    handleEndSuite(eventMap);
                    break;
                case "start_test":
                    handleStartTest(eventMap);
                    break;
                case "end_test":
                    handleEndTest(eventMap);
                    break;
                case "pre_start_keyword":
                    handlePreStartKeyword(eventMap);
                    break;
                case "start_keyword":
                    handleStartKeyword(eventMap);
                    break;
                case "pre_end_keyword":
                    handlePreEndKeyword(eventMap);
                    break;
                case "end_keyword":
                    handleEndKeyword(eventMap);
                    break;
                case "variables":
                    handleVariables(eventMap);
                    break;
                case "should_continue":
                    handleShouldContinue(eventMap);
                    break;
                case "condition_result":
                    handleConditionResult(eventMap);
                    break;
                case "paused":
                    handlePause();
                    break;
                case "resumed":
                    handleResumed();
                    break;
                case "log_message":
                    handleLogMessage(eventMap);
                    break;
                case "message":
                    handleMessage(eventMap);
                    break;
                case "output_file":
                    handleOutputFile(eventMap);
                    break;
                case "close":
                    handleClose();
                    break;
                default:
                    break;
            }

            event = eventReader.readLine();
        }
    }

    private void handleAgentInitializing() {
        for (final RobotAgentEventListener listener : eventsListeners) {
            listener.handleAgentInitializing(AgentInitializingEvent.from(client));
        }
    }

    private void handleVersion(final Map<String, Object> eventMap) {
        for (final RobotAgentEventListener listener : eventsListeners) {
            listener.handleVersions(VersionsEvent.from(client, eventMap));
        }
    }

    private void handleReadyToStart() {
        for (final RobotAgentEventListener listener : eventsListeners) {
            listener.handleAgentIsReadyToStart(ReadyToStartEvent.from(client));
        }
    }

    private void handleResourceImport(final Map<String, Object> eventMap) {
        final ResourceImportEvent event = ResourceImportEvent.from(eventMap);
        for (final RobotAgentEventListener listener : eventsListeners) {
            listener.handleResourceImport(event);
        }
    }

    private void handleStartSuite(final Map<String, Object> eventMap) {
        final SuiteStartedEvent event = SuiteStartedEvent.from(eventMap);
        for (final RobotAgentEventListener listener : eventsListeners) {
            listener.handleSuiteStarted(event);
        }
    }

    private void handleEndSuite(final Map<String, Object> eventMap) {
        final SuiteEndedEvent event = SuiteEndedEvent.from(eventMap);
        for (final RobotAgentEventListener listener : eventsListeners) {
            listener.handleSuiteEnded(event);
        }
    }

    private void handleStartTest(final Map<String, Object> eventMap) {
        final TestStartedEvent event = TestStartedEvent.from(eventMap);
        for (final RobotAgentEventListener listener : eventsListeners) {
            listener.handleTestStarted(event);
        }
    }

    private void handleEndTest(final Map<String, Object> eventMap) {
        final TestEndedEvent event = TestEndedEvent.from(eventMap);
        for (final RobotAgentEventListener listener : eventsListeners) {
            listener.handleTestEnded(event);
        }
    }

    private void handlePreStartKeyword(final Map<String, Object> eventMap) {
        final KeywordStartedEvent event = KeywordStartedEvent.fromPre(eventMap);
        for (final RobotAgentEventListener listener : eventsListeners) {
            listener.handleKeywordAboutToStart(event);
        }
    }

    private void handleStartKeyword(final Map<String, Object> eventMap) {
        final KeywordStartedEvent event = KeywordStartedEvent.from(eventMap);
        for (final RobotAgentEventListener listener : eventsListeners) {
            listener.handleKeywordStarted(event);
        }
    }

    private void handlePreEndKeyword(final Map<String, Object> eventMap) {
        final KeywordEndedEvent event = KeywordEndedEvent.fromPre(eventMap);
        for (final RobotAgentEventListener listener : eventsListeners) {
            listener.handleKeywordAboutToEnd(event);
        }
    }

    private void handleEndKeyword(final Map<String, Object> eventMap) {
        final KeywordEndedEvent event = KeywordEndedEvent.from(eventMap);
        for (final RobotAgentEventListener listener : eventsListeners) {
            listener.handleKeywordEnded(event);
        }
    }

    private void handleVariables(final Map<String, Object> eventMap) {
        final VariablesEvent event = VariablesEvent.from(eventMap);
        for (final RobotAgentEventListener listener : eventsListeners) {
            listener.handleVariables(event);
        }
    }

    private void handleShouldContinue(final Map<String, Object> eventMap) {
        final ShouldContinueEvent event = ShouldContinueEvent.from(client, eventMap);
        for (final RobotAgentEventListener listener : eventsListeners) {
            listener.handleShouldContinue(event);
        }
    }

    private void handleConditionResult(final Map<String, Object> eventMap) {
        final ConditionEvaluatedEvent event = ConditionEvaluatedEvent.from(eventMap);
        for (final RobotAgentEventListener listener : eventsListeners) {
            listener.handleConditionEvaluated(event);
        }
    }

    private void handlePause() {
        final PausedEvent event = PausedEvent.from(client);
        for (final RobotAgentEventListener listener : eventsListeners) {
            listener.handlePaused(event);
        }
    }

    private void handleResumed() {
        for (final RobotAgentEventListener listener : eventsListeners) {
            listener.handleResumed();
        }
    }

    private void handleClose() {
        for (final RobotAgentEventListener listener : eventsListeners) {
            listener.handleClosed();
        }
    }

    private void handleOutputFile(final Map<String, Object> eventMap) {
        final OutputFileEvent event = OutputFileEvent.from(eventMap);
        for (final RobotAgentEventListener listener : eventsListeners) {
            listener.handleOutputFile(event);
        }
    }

    private void handleLibraryImport(final Map<String, Object> eventMap) {
        final LibraryImportEvent event = LibraryImportEvent.from(eventMap);
        for (final RobotAgentEventListener listener : eventsListeners) {
            listener.handleLibraryImport(event);
        }
    }

    private void handleLogMessage(final Map<String, Object> eventMap) {
        final MessageEvent event = MessageEvent.fromLogMessage(eventMap);
        for (final RobotAgentEventListener listener : eventsListeners) {
            listener.handleLogMessage(event);
        }
    }

    private void handleMessage(final Map<String, Object> eventMap) {
        final MessageEvent event = MessageEvent.fromMessage(eventMap);
        for (final RobotAgentEventListener listener : eventsListeners) {
            listener.handleMessage(event);
        }
    }

    private String getEventType(final Map<String, ?> eventMap) {
        if (eventMap == null) {
            return null;
        }
        return Iterables.getFirst(eventMap.keySet(), null);
    }

    private boolean anyListenerIsHandlingEvents() {
        for (final RobotAgentEventListener listener : eventsListeners) {
            if (listener.isHandlingEvents()) {
                return true;
            }
        }
        return false;
    }
}

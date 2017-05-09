/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.execution.server;

import static com.google.common.collect.Lists.newArrayList;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.rf.ide.core.execution.agent.LogLevel;
import org.rf.ide.core.execution.agent.RobotAgentEventListener;
import org.rf.ide.core.execution.agent.RobotAgentEventListener.RobotAgentEventsListenerException;
import org.rf.ide.core.execution.agent.event.KeywordEndedEvent;
import org.rf.ide.core.execution.agent.event.KeywordStartedEvent;
import org.rf.ide.core.execution.agent.event.LibraryImportEvent;
import org.rf.ide.core.execution.agent.event.OutputFileEvent;
import org.rf.ide.core.execution.agent.event.ResourceImportEvent;
import org.rf.ide.core.execution.agent.event.SuiteEndedEvent;
import org.rf.ide.core.execution.agent.event.SuiteStartedEvent;
import org.rf.ide.core.execution.agent.event.TestEndedEvent;
import org.rf.ide.core.execution.agent.event.TestStartedEvent;

import com.google.common.collect.Iterables;

class RobotAgentEventDispatcher {

    private final List<RobotAgentEventListener> eventsListeners;

    RobotAgentEventDispatcher(final AgentClient client, final RobotAgentEventListener... eventsListeners) {
        final List<RobotAgentEventListener> listeners = newArrayList(eventsListeners);
        listeners.add(0, new AgentServerProtocolVersionChecker());
        this.eventsListeners = Collections.synchronizedList(listeners);

        for (final RobotAgentEventListener listener : this.eventsListeners) {
            listener.setClient(client);
        }
    }

    void addEventsListener(final RobotAgentEventListener listener) {
        eventsListeners.add(listener);
    }

    void removeEventsListener(final RobotAgentEventListener listener) {
        eventsListeners.remove(listener);
    }

    void runEventsLoop(final BufferedReader eventReader) throws IOException, RobotAgentEventsListenerException {
        String event = eventReader.readLine();
        final ObjectMapper mapper = new ObjectMapper();
        while (event != null && anyListenerIsHandlingEvents()) {

            final TypeReference<Map<String, Object>> stringToObjectMapType = new TypeReference<Map<String, Object>>() {
            };
            final Map<String, Object> eventMap = mapper.readValue(event, stringToObjectMapType);
            final String eventType = getEventType(eventMap);
            if (eventType == null) {
                continue;
            }

            switch (eventType) {
                case "ready_to_start":
                    handleReadyToStart();
                    break;
                case "agent_initializing":
                    handleAgentInitializing();
                    break;
                case "version":
                    handleVersion(eventMap);
                    break;
                case "resource_import":
                    handleResourceImport(eventMap);
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
                case "start_keyword":
                    handleStartKeyword(eventMap);
                    break;
                case "end_keyword":
                    handleEndKeyword(eventMap);
                    break;
                case "vars":
                    handleVariables(eventMap);
                    break;
                case "global_vars":
                    handleGlobalVariables(eventMap);
                    break;
                case "check_condition":
                    handleCheckCondition();
                    break;
                case "condition_result":
                    handleConditionResult(eventMap);
                    break;
                case "condition_error":
                    handleConditionError(eventMap);
                    break;
                case "condition_checked":
                    handleConditionChecked();
                    break;
                case "paused":
                    handlePause();
                    break;
                case "close":
                    handleClose();
                    break;
                case "log_message":
                    handleLogMessage(eventMap);
                    break;
                case "output_file":
                    handleOutputFile(eventMap);
                    break;
                case "library_import":
                    handleLibraryImport(eventMap);
                    break;
                case "message":
                    handleMessage(eventMap);
                    break;
                default:
                    break;
            }

            event = eventReader.readLine();
        }
    }

    private void handleReadyToStart() {
        for (final RobotAgentEventListener listener : eventsListeners) {
            listener.handleAgentIsReadyToStart();
        }
    }

    private void handleAgentInitializing() {
        for (final RobotAgentEventListener listener : eventsListeners) {
            listener.handleAgentInitializing();
        }
    }

    private void handleVersion(final Map<String, Object> eventMap) {
        final List<?> arguments = (List<?>) eventMap.get("version");
        final Map<?, ?> attributes = (Map<?, ?>) arguments.get(0);
        final String pythonVersion = (String) attributes.get("python");
        final String robotVersion = (String) attributes.get("robot");
        final int protocolVersion = (Integer) attributes.get("protocol");

        for (final RobotAgentEventListener listener : eventsListeners) {
            listener.handleVersions(pythonVersion, robotVersion, protocolVersion);
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

    private void handleStartKeyword(final Map<String, Object> eventMap) {
        final KeywordStartedEvent event = KeywordStartedEvent.from(eventMap);
        for (final RobotAgentEventListener listener : eventsListeners) {
            listener.handleKeywordStarted(event);
        }
    }

    private void handleEndKeyword(final Map<String, Object> eventMap) {
        final KeywordEndedEvent event = KeywordEndedEvent.from(eventMap);
        for (final RobotAgentEventListener listener : eventsListeners) {
            listener.handleKeywordEnded(event);
        }
    }

    private void handleVariables(final Map<String, Object> eventMap) {
        final List<?> arguments = (List<?>) eventMap.get("vars");
        final Map<String, Object> vars = ensureOrderedMapOfStringsToObjects((Map<?, ?>) arguments.get(1));

        for (final RobotAgentEventListener listener : eventsListeners) {
            listener.handleVariables(vars);
        }
    }

    private void handleGlobalVariables(final Map<String, Object> eventMap) {
        final List<?> arguments = (List<?>) eventMap.get("global_vars");
        final Map<String, String> globalVars = ensureOrderedMapOfStringsToStrings((Map<?, ?>) arguments.get(1));

        for (final RobotAgentEventListener listener : eventsListeners) {
            listener.handleGlobalVariables(globalVars);
        }
    }

    private void handleCheckCondition() {
        for (final RobotAgentEventListener listener : eventsListeners) {
            listener.handleCheckCondition();
        }
    }

    private void handleConditionResult(final Map<String, Object> eventMap) {
        final List<?> arguments = (List<?>) eventMap.get("condition_result");
        final boolean result = (Boolean) arguments.get(0);

        for (final RobotAgentEventListener listener : eventsListeners) {
            listener.handleConditionResult(result);
        }
    }

    private void handleConditionError(final Map<String, Object> eventMap) {
        final List<?> arguments = (List<?>) eventMap.get("condition_error");
        final String error = (String) arguments.get(0);

        for (final RobotAgentEventListener listener : eventsListeners) {
            listener.handleConditionError(error);
        }
    }

    private void handleConditionChecked() {
        for (final RobotAgentEventListener listener : eventsListeners) {
            listener.handleConditionChecked();
        }
    }

    private void handlePause() {
        for (final RobotAgentEventListener listener : eventsListeners) {
            listener.handlePaused();
        }
    }

    private void handleClose() {
        for (final RobotAgentEventListener listener : eventsListeners) {
            listener.handleClosed();
        }
    }

    private void handleLogMessage(final Map<String, Object> eventMap) {
        final List<?> arguments = (List<?>) eventMap.get("log_message");
        final Map<?, ?> message = (Map<?, ?>) arguments.get(0);
        final String msg = (String) message.get("message");
        final String timestamp = (String) message.get("timestamp");
        final LogLevel level = LogLevel.valueOf(((String) message.get("level")).toUpperCase());

        for (final RobotAgentEventListener listener : eventsListeners) {
            listener.handleLogMessage(msg, level, timestamp);
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

    private void handleMessage(final Map<String, Object> eventMap) {
        final List<?> arguments = (List<?>) eventMap.get("message");
        final Map<?, ?> attributes = (Map<?, ?>) arguments.get(0);
        final String msg = (String) attributes.get("message");
        final LogLevel level = LogLevel.valueOf(((String) attributes.get("level")).toUpperCase());

        for (final RobotAgentEventListener listener : eventsListeners) {
            listener.handleMessage(msg, level);
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

    private static Map<String, Object> ensureOrderedMapOfStringsToObjects(final Map<?, ?> map) {
        final LinkedHashMap<String, Object> result = new LinkedHashMap<>();
        map.entrySet().stream().forEach(e -> result.put((String) e.getKey(), e.getValue()));
        return result;
    }

    private static Map<String, String> ensureOrderedMapOfStringsToStrings(final Map<?, ?> map) {
        final LinkedHashMap<String, String> result = new LinkedHashMap<>();
        map.entrySet().stream().forEach(e -> result.put((String) e.getKey(), (String) e.getValue()));
        return result;
    }
}

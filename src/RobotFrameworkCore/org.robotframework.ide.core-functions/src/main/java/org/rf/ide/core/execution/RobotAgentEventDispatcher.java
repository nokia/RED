/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.execution;

import static com.google.common.collect.Lists.newArrayList;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.rf.ide.core.execution.RobotAgentEventListener.RobotAgentEventsListenerException;
import org.rf.ide.core.execution.server.AgentClient;

import com.google.common.collect.Iterables;

public class RobotAgentEventDispatcher {

    private final List<RobotAgentEventListener> eventsListeners;

    public RobotAgentEventDispatcher(final AgentClient client, final RobotAgentEventListener... eventsListeners) {
        this.eventsListeners = Collections.synchronizedList(newArrayList(eventsListeners));
        for (final RobotAgentEventListener listener : eventsListeners) {
            listener.setClient(client);
        }
    }

    public void addEventsListener(final RobotAgentEventListener listener) {
        eventsListeners.add(listener);
    }

    public void removeEventsListener(final RobotAgentEventListener listener) {
        eventsListeners.remove(listener);
    }

    public void runEventsLoop(final BufferedReader eventReader) throws IOException, RobotAgentEventsListenerException {
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
                case "pid":
                    handlePid();
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

    private void handlePid() {
        for (final RobotAgentEventListener listener : eventsListeners) {
            listener.handlePid();
        }
    }

    private void handleVersion(final Map<String, Object> eventMap) {
        final List<?> arguments = (List<?>) eventMap.get("version");
        final Map<?, ?> attributes = (Map<?, ?>) arguments.get(0);
        final String pythonVersion = (String) attributes.get("python");
        final String robotVersion = (String) attributes.get("robot");

        for (final RobotAgentEventListener listener : eventsListeners) {
            listener.handleVersions(pythonVersion, robotVersion);
        }
    }

    private void handleResourceImport(final Map<String, Object> eventMap) {
        final List<?> arguments = (List<?>) eventMap.get("resource_import");
        final Map<?, ?> attributes = (Map<?, ?>) arguments.get(1);
        final File resourceFilePath = new File((String) attributes.get("source"));

        for (final RobotAgentEventListener listener : eventsListeners) {
            listener.handleResourceImport(resourceFilePath);
        }
    }

    private void handleStartSuite(final Map<String, Object> eventMap) {
        final List<?> arguments = (List<?>) eventMap.get("start_suite");
        final String name = (String) arguments.get(0);
        final Map<?, ?> attributes = (Map<?, ?>) arguments.get(1);
        final File suiteFilePath = new File((String) attributes.get("source"));
        
        for (final RobotAgentEventListener listener : eventsListeners) {
            listener.handleSuiteStarted(name, suiteFilePath);
        }
    }

    private void handleEndSuite(final Map<String, Object> eventMap) {
        final List<?> arguments = (List<?>) eventMap.get("end_suite");
        final String name = (String) arguments.get(0);
        final Map<?, ?> attributes = (Map<?, ?>) arguments.get(1);
        final int elapsedTime = (Integer) attributes.get("elapsedtime");
        final String errorMessage = (String) attributes.get("message");
        final Status suiteStatus = Status.valueOf((String) attributes.get("status"));
        
        for (final RobotAgentEventListener listener : eventsListeners) {
            listener.handleSuiteEnded(name, elapsedTime, suiteStatus, errorMessage);
        }
    }

    private void handleStartTest(final Map<String, Object> eventMap) {
        final List<?> arguments = (List<?>) eventMap.get("start_test");
        final String name = (String) arguments.get(0);
        final Map<?, ?> attributes = (Map<?, ?>) arguments.get(1);
        final String longName = (String) attributes.get("longname");

        for (final RobotAgentEventListener listener : eventsListeners) {
            listener.handleTestStarted(name, longName);
        }
    }

    private void handleEndTest(final Map<String, Object> eventMap) {
        final List<?> arguments = (List<?>) eventMap.get("end_test");
        final String name = (String) arguments.get(0);
        final Map<?, ?> attributes = (Map<?, ?>) arguments.get(1);
        final String longName = (String) attributes.get("longname");
        final int elapsedTime = (Integer) attributes.get("elapsedtime");
        final String errorMessage = (String) attributes.get("message");
        final Status testStatus = Status.valueOf((String) attributes.get("status"));

        for (final RobotAgentEventListener listener : eventsListeners) {
            listener.handleTestEnded(name, longName, elapsedTime, testStatus, errorMessage);
        }
    }

    private void handleStartKeyword(final Map<String, Object> eventMap) {
        final List<?> arguments = (List<?>) eventMap.get("start_keyword");
        final String name = (String) arguments.get(0);
        final Map<?, ?> attributes = (Map<?, ?>) arguments.get(1);
        final String keywordType = (String) attributes.get("type");
        final List<String> keywordArgs = ensureListOfStrings((List<?>) attributes.get("args"));

        for (final RobotAgentEventListener listener : eventsListeners) {
            listener.handleKeywordStarted(name, keywordType, keywordArgs);
        }
    }

    private List<String> ensureListOfStrings(final List<?> list) {
        final List<String> elements = new ArrayList<>();
        for (final Object object : list) {
            elements.add((String) object);
        }
        return elements;
    }

    private void handleEndKeyword(final Map<String, Object> eventMap) {
        final List<?> arguments = (List<?>) eventMap.get("end_keyword");
        final String name = (String) arguments.get(0);
        final Map<?, ?> attributes = (Map<?, ?>) arguments.get(1);
        final String keywordType = (String) attributes.get("type");

        for (final RobotAgentEventListener listener : eventsListeners) {
            listener.handleKeywordEnded(name, keywordType);
        }
    }

    private void handleVariables(final Map<String, Object> eventMap) {
        final List<?> arguments = (List<?>) eventMap.get("vars");
        final Map<String, Object> vars = ensureMapOfStringsToObjects((Map<?, ?>) arguments.get(1));

        for (final RobotAgentEventListener listener : eventsListeners) {
            listener.handleVariables(vars);
        }
    }

    private Map<String, Object> ensureMapOfStringsToObjects(final Map<?, ?> map) {
        final Map<String, Object> mapping = new LinkedHashMap<>();
        for (final Entry<?, ?> entry : map.entrySet()) {
            mapping.put((String) entry.getKey(), entry.getValue());
        }
        return mapping;
    }

    private void handleGlobalVariables(final Map<String, Object> eventMap) {
        final List<?> arguments = (List<?>) eventMap.get("global_vars");
        final Map<String, String> globalVars = ensureMapOfStringsToStrings((Map<?, ?>) arguments.get(1));

        for (final RobotAgentEventListener listener : eventsListeners) {
            listener.handleGlobalVariables(globalVars);
        }
    }

    private Map<String, String> ensureMapOfStringsToStrings(final Map<?, ?> map) {
        final Map<String, String> mapping = new LinkedHashMap<>();
        for (final Entry<?, ?> entry : map.entrySet()) {
            mapping.put((String) entry.getKey(), (String) entry.getValue());
        }
        return mapping;
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
        final List<?> arguments = (List<?>) eventMap.get("output_file");
        final File path = new File((String) arguments.get(0));

        for (final RobotAgentEventListener listener : eventsListeners) {
            listener.handleOutputFile(path);
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

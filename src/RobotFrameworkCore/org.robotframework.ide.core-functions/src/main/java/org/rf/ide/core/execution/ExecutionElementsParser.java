/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.execution;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.rf.ide.core.execution.ExecutionElement.ExecutionElementType;
import org.rf.ide.core.executor.ILineHandler;

/**
 * @author mmarzec
 */
public class ExecutionElementsParser implements ILineHandler {

    public static final String ROBOT_EXECUTION_PASS_STATUS = "PASS";

    private static final String START_SUITE_EVENT = "start_suite";

    private static final String END_SUITE_EVENT = "end_suite";

    private static final String START_TEST_EVENT = "start_test";

    private static final String END_TEST_EVENT = "end_test";

    private static final String OUTPUT_FILE_EVENT = "output_file";

    private final ObjectMapper mapper;

    private Map<String, List<?>> eventMap;

    private final IExecutionHandler executionHandler;

    public ExecutionElementsParser(final IExecutionHandler executionHandler) {
        this.mapper = new ObjectMapper();
        this.eventMap = new HashMap<>();
        this.executionHandler = executionHandler;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void processLine(final String line) {
        try {
            eventMap = mapper.readValue(line, new TypeReference<Map<String, List<?>>>() {
            });
        } catch (final IOException e) {
            e.printStackTrace();
        }

        final String eventType = getEventType(eventMap);
        if (eventType == null) {
            return;
        }

        switch (eventType) {
            case START_SUITE_EVENT:
                final List<?> startSuiteList = eventMap.get(START_SUITE_EVENT);
                final Map<String, String> startSuiteDetails = (Map<String, String>) startSuiteList.get(1);
                final ExecutionElement startSuiteElement = createStartSuiteExecutionElement(
                        (String) startSuiteList.get(0), startSuiteDetails.get("source"));
                executionHandler.processExecutionElement(startSuiteElement);
                break;
            case END_SUITE_EVENT:
                final List<?> endSuiteList = eventMap.get(END_SUITE_EVENT);
                final Map<String, Object> endSuiteDetails = (Map<String, Object>) endSuiteList.get(1);
                final ExecutionElement endSuiteElement = createEndSuiteExecutionElement((String) endSuiteList.get(0),
                        endSuiteDetails);
                executionHandler.processExecutionElement(endSuiteElement);
                break;
            case START_TEST_EVENT:
                final List<?> testList = eventMap.get(START_TEST_EVENT);
                final ExecutionElement startTestElement = createStartTestExecutionElement((String) testList.get(0));
                executionHandler.processExecutionElement(startTestElement);
                break;
            case END_TEST_EVENT:
                final List<?> endTestList = eventMap.get(END_TEST_EVENT);
                final Map<String, Object> endTestDetails = (Map<String, Object>) endTestList.get(1);
                final ExecutionElement endTestElement = createEndTestExecutionElement((String) endTestList.get(0),
                        endTestDetails);
                executionHandler.processExecutionElement(endTestElement);
                break;
            case OUTPUT_FILE_EVENT:
                final List<?> outputFileList = eventMap.get(OUTPUT_FILE_EVENT);
                final ExecutionElement outputFilePathElement = createOutputFileExecutionElement(
                        (String) outputFileList.get(0));
                executionHandler.processExecutionElement(outputFilePathElement);
                break;
            default:
                break;
        }
    }

    public static ExecutionElement createStartSuiteExecutionElement(final String name, final String source) {
        return new ExecutionElement(name, ExecutionElementType.SUITE, new File(source), -1, null, null);
    }

    public static ExecutionElement createStartTestExecutionElement(final String name) {
        return createNewExecutionElement(name, ExecutionElementType.TEST);
    }

    public static ExecutionElement createEndTestExecutionElement(final String name, final Map<?, ?> endTestDetails) {
        return createEndExecutionElement(name, ExecutionElementType.TEST, endTestDetails);
    }

    public static ExecutionElement createEndTestExecutionElement(final String name, final int elapsedTime,
            final String message, final String status) {
        return createEndExecutionElement(name, ExecutionElementType.TEST, elapsedTime, message, status);
    }

    public static ExecutionElement createEndSuiteExecutionElement(final String name, final Map<?, ?> endSuiteDetails) {
        return createEndExecutionElement(name, ExecutionElementType.SUITE, endSuiteDetails);
    }

    public static ExecutionElement createEndSuiteExecutionElement(final String name, final int elapsedTime,
            final String message, final String status) {
        return createEndExecutionElement(name, ExecutionElementType.SUITE, elapsedTime, message, status);
    }

    public static ExecutionElement createOutputFileExecutionElement(final String name) {
        return createNewExecutionElement(name, ExecutionElementType.OUTPUT_FILE);
    }

    private static ExecutionElement createEndExecutionElement(final String name, final ExecutionElementType type,
            final Map<?, ?> details) {
        final int elapsedTime = (Integer) details.get("elapsedtime");
        final String message = (String) details.get("message");
        final Status status = Status.valueOf(((String) details.get("status")).toUpperCase());
        return new ExecutionElement(name, type, null, elapsedTime, status, message);
    }

    private static ExecutionElement createEndExecutionElement(final String name, final ExecutionElementType type,
            final int elapsedTime, final String message, final String status) {
        return new ExecutionElement(name, type, null, elapsedTime,
                Status.valueOf(status.toUpperCase()), message);
    }

    private static ExecutionElement createNewExecutionElement(final String name, final ExecutionElementType type) {
        return new ExecutionElement(name, type, null, -1, null, null);
    }

    private String getEventType(final Map<?, ?> eventMap) {
        if (eventMap == null) {
            return null;
        }
        final Set<?> keySet = eventMap.keySet();
        if (!keySet.isEmpty()) {
            return (String) keySet.iterator().next();
        }
        return null;
    }
}

/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.executor;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;

/**
 * @author mmarzec
 */
class MessageLogParser implements ILineHandler {

    private static final String LOG_MESSAGE_EVENT_NAME = "log_message";

    private static final String START_TEST_EVENT_NAME = "start_test";

    private static final String END_TEST_EVENT_NAME = "end_test";

    private final ObjectMapper mapper;

    private Map<String, Object> parsedLine;

    private final ILineHandler lineHandler;

    MessageLogParser(final ILineHandler lineHandler) {
        this.mapper = new ObjectMapper();
        this.parsedLine = new HashMap<String, Object>();
        this.lineHandler = lineHandler;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void processLine(final String line) {
        try {
            parsedLine = mapper.readValue(line, Map.class);
        } catch (final IOException e) {
            e.printStackTrace();
        }
        if (parsedLine.containsKey(LOG_MESSAGE_EVENT_NAME)) {
            final Map<String, String> elements = extractElementsFromLine(parsedLine, LOG_MESSAGE_EVENT_NAME, 0);
            lineHandler.processLine(
                    elements.get("timestamp") + " : " + elements.get("level") + " : " + elements.get("message") + '\n');
        } else if (parsedLine.containsKey(START_TEST_EVENT_NAME)) {
            final Map<String, String> elements = extractElementsFromLine(parsedLine, START_TEST_EVENT_NAME, 1);
            lineHandler.processLine("Starting test: " + elements.get("longname") + '\n');
        } else if (parsedLine.containsKey(END_TEST_EVENT_NAME)) {
            final Map<String, String> elements = extractElementsFromLine(parsedLine, END_TEST_EVENT_NAME, 1);
            lineHandler.processLine("Ending test: " + elements.get("longname") + '\n');
            lineHandler.processLine("\n");
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> extractElementsFromLine(final Map<String, Object> map, final String lineName, final int elementsPosition) {
        final List<Object> list = (List<Object>) map.get(lineName);
        return (Map<String, String>) list.get(elementsPosition);
    }
}

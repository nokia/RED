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
import org.codehaus.jackson.type.TypeReference;

/**
 * @author mmarzec
 */
class MessageLogParser implements ILineHandler {

    private static final String LOG_MESSAGE_EVENT_NAME = "log_message";

    private static final String START_TEST_EVENT_NAME = "start_test";

    private static final String END_TEST_EVENT_NAME = "end_test";

    private final ObjectMapper mapper;

    private Map<String, List<?>> eventMap;

    private final ILineHandler lineHandler;

    MessageLogParser(final ILineHandler lineHandler) {
        this.mapper = new ObjectMapper();
        this.eventMap = new HashMap<String, List<?>>();
        this.lineHandler = lineHandler;
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
        if (eventMap.containsKey(LOG_MESSAGE_EVENT_NAME)) {
            final Map<String, String> elements = (Map<String, String>) eventMap.get(LOG_MESSAGE_EVENT_NAME).get(0);
            lineHandler.processLine(
                    elements.get("timestamp") + " : " + elements.get("level") + " : " + elements.get("message") + '\n');
        } else if (eventMap.containsKey(START_TEST_EVENT_NAME)) {
            final Map<String, String> elements = (Map<String, String>) eventMap.get(START_TEST_EVENT_NAME).get(1);
            lineHandler.processLine("Starting test: " + elements.get("longname") + '\n');
        } else if (eventMap.containsKey(END_TEST_EVENT_NAME)) {
            final Map<String, String> elements = (Map<String, String>) eventMap.get(END_TEST_EVENT_NAME).get(1);
            lineHandler.processLine("Ending test: " + elements.get("longname") + '\n');
            lineHandler.processLine("\n");
        }
    }
}

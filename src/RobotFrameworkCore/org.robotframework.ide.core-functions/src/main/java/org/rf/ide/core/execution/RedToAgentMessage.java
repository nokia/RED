/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.execution;

import static com.google.common.collect.Lists.newArrayList;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;

public enum RedToAgentMessage {
    START_EXECUTION("do_start"),
    CONTINUE_EXECUTION("continue"),
    STOP_EXECUTION("stop"),
    RESUME_EXECUTION("resume"),
    INTERRUPT_EXECUTION("interrupt"),
    EVALUATE_CONDITION("keyword_condition") {

        @Override
        public String createMessage(final String... arguments) {
            final Map<String, Object> value = new HashMap<>();
            value.put(message, newArrayList(arguments));
            final ObjectMapper mapper = new ObjectMapper();
            try {
                return mapper.writeValueAsString(value);
            } catch (final IOException e) {
                throw new IllegalArgumentException("Unable to serialize breakpoint condition to json", e);
            }
        }
    },
    VARIABLE_CHANGE_REQUEST("variable_change") {

        @Override
        public String createMessage(final String... values) {
            final Map<String, Object> inside = new HashMap<>();
            inside.put(values[0], newArrayList(Arrays.copyOfRange(values, 1, values.length)));

            final Map<String, Object> value = new HashMap<>();
            value.put(message, inside);
            final ObjectMapper mapper = new ObjectMapper();
            try {
                return mapper.writeValueAsString(value);
            } catch (final IOException e) {
                throw new IllegalArgumentException("Unable to serialize breakpoint condition to json", e);
            }
        }
    };

    protected final String message;

    private RedToAgentMessage(final String message) {
        this.message = message;
    }

    public String createMessage(@SuppressWarnings("unused") final String... arguments) {
        return message;
    }
}
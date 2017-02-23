/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.execution.server.response;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;

public class ChangeVariable implements ServerResponse {

    private final String variableName;
    private final List<String> values;

    public ChangeVariable(final String variableName, final List<String> values) {
        this.variableName = variableName;
        this.values = values;
    }

    @Override
    public String toMessage() {
        final Map<String, Object> inside = new HashMap<>();
        inside.put(variableName, values);

        final Map<String, Object> value = new HashMap<>();
        value.put("variable_change", inside);
        final ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(value);
        } catch (final IOException e) {
            throw new IllegalArgumentException("Unable to serialize breakpoint condition to json", e);
        }
    }

}

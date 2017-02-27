/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.execution.server.response;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;

import com.google.common.collect.ImmutableMap;

public class ChangeVariable implements ServerResponse {

    private final String variableName;
    private final List<String> values;

    public ChangeVariable(final String variableName, final List<String> values) {
        this.variableName = variableName;
        this.values = values;
    }

    @Override
    public String toMessage() throws ResponseException {
        try {
            final Map<String, List<String>> arguments = ImmutableMap.of(variableName, values);
            final Map<String, Object> value = ImmutableMap.of("variable_change", arguments);

            return new ObjectMapper().writeValueAsString(value);
        } catch (final IOException e) {
            throw new ResponseException("Unable to serialize variable change response arguments to json", e);
        }
    }

}

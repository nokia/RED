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

public class EvaluateCondition implements ServerResponse {

    private final List<String> conditionWithArguments;

    public EvaluateCondition(final List<String> conditionWithArguments) {
        this.conditionWithArguments = conditionWithArguments;
    }

    @Override
    public String toMessage() {
        final Map<String, Object> value = new HashMap<>();
        value.put("keyword_condition", conditionWithArguments);
        final ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(value);
        } catch (final IOException e) {
            throw new ResponseException("Unable to serialize breakpoint condition to json", e);
        }
    }
}

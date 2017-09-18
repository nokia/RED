/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.execution.server.response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;
import org.rf.ide.core.testdata.model.table.variables.AVariable.VariableScope;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;

public final class ChangeVariable implements ServerResponse {

    private final ObjectMapper mapper;

    private final String variableName;

    private final VariableScope scope;

    private final int frameLevel;

    private final List<Object> path;

    private final List<String> arguments;


    public ChangeVariable(final String varName, final VariableScope scope, final int frameLevel,
            final List<String> arguments) {
        this(varName, scope, frameLevel, new ArrayList<>(), arguments);
    }

    public ChangeVariable(final String varName, final VariableScope scope, final int frameLevel,
            final List<Object> path, final List<String> arguments) {
        this(ResponseObjectsMapper.OBJECT_MAPPER, varName, scope, frameLevel, path, arguments);
    }

    @VisibleForTesting
    ChangeVariable(final ObjectMapper mapper, final String varName, final VariableScope scope,
            final int frameLevel, final List<Object> path, final List<String> arguments) {
        this.mapper = mapper;
        this.variableName = varName;
        this.scope = scope;
        this.frameLevel = frameLevel;
        this.path = path;
        this.arguments = arguments;
    }

    @Override
    public String toMessage() throws ResponseException {
        try {
            final Builder<String, Object> argsBuilder = ImmutableMap.<String, Object> builder()
                    .put("name", variableName)
                    .put("values", arguments)
                    .put("scope", scope.name().toLowerCase())
                    .put("level", frameLevel);
            if (!path.isEmpty()) {
                argsBuilder.put("path", path);
            }
            final Map<String, Object> value = ImmutableMap.of("change_variable", argsBuilder.build());

            return mapper.writeValueAsString(value);
        } catch (final IOException e) {
            throw new ResponseException("Unable to serialize change variable response arguments to json", e);
        }
    }

}

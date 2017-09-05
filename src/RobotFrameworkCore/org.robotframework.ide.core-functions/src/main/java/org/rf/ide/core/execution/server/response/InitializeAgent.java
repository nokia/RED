/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.execution.server.response;

import java.io.IOException;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;
import org.rf.ide.core.execution.agent.TestsMode;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;

public final class InitializeAgent implements ServerResponse {

    private final ObjectMapper mapper;

    private final TestsMode mode;

    private final boolean agentShouldWaitForSignal;

    public InitializeAgent(final TestsMode mode, final boolean agentShouldWaitForSignal) {
        this(ResponseObjectsMapper.OBJECT_MAPPER, mode, agentShouldWaitForSignal);
    }

    @VisibleForTesting
    InitializeAgent(final ObjectMapper mapper, final TestsMode mode, final boolean agentShouldWaitForSignal) {
        this.mapper = mapper;
        this.mode = mode;
        this.agentShouldWaitForSignal = agentShouldWaitForSignal;
    }

    @Override
    public String toMessage() throws ResponseException {
        try {
            final Map<String, ? extends Object> arguments = ImmutableMap.of("mode", mode.name(),
                    "wait_for_start_allowance", agentShouldWaitForSignal);
            final Map<String, Object> value = ImmutableMap.of("operating_mode", arguments);

            return mapper.writeValueAsString(value);
        } catch (final IOException e) {
            throw new ResponseException("Unable to serialize initialize agent response arguments to json", e);
        }
    }
}

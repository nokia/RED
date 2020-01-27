/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.execution.server.response;

import java.io.IOException;
import java.util.Map;

import org.rf.ide.core.execution.agent.TestsMode;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;

public final class InitializeAgent implements ServerResponse {

    private final ObjectMapper mapper;

    private final TestsMode mode;

    private final boolean agentShouldWaitForSignal;

    private final int maxValueLenght;

    public InitializeAgent(final TestsMode mode, final boolean agentShouldWaitForSignal, final int maxValueLenght) {
        this(ResponseObjectsMapper.OBJECT_MAPPER, mode, agentShouldWaitForSignal, maxValueLenght);
    }

    @VisibleForTesting
    InitializeAgent(final ObjectMapper mapper, final TestsMode mode, final boolean agentShouldWaitForSignal,
            final int maxValueLenght) {
        this.mapper = mapper;
        this.mode = mode;
        this.agentShouldWaitForSignal = agentShouldWaitForSignal;
        this.maxValueLenght = maxValueLenght;
    }

    @Override
    public String toMessage() throws ResponseException {
        try {
            final Builder<String, Object> argsBuilder = ImmutableMap.<String, Object> builder()
                    .put("mode", mode.name())
                    .put("wait_for_start_allowance", agentShouldWaitForSignal);
            if (maxValueLenght >= 0) {
                argsBuilder.put("max_lenght", maxValueLenght);
            }
            final Map<String, Object> value = ImmutableMap.of("operating_mode", argsBuilder.build());

            return mapper.writeValueAsString(value);
        } catch (final IOException e) {
            throw new ResponseException("Unable to serialize initialize agent response arguments to json", e);
        }
    }
}

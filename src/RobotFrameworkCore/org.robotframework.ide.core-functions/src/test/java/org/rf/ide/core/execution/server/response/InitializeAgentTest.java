/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.execution.server.response;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.rf.ide.core.execution.agent.TestsMode;
import org.rf.ide.core.execution.server.response.ServerResponse.ResponseException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class InitializeAgentTest {

    @Test
    public void properMessageIsConstructed_forInitializeAgentResponse() {
        assertThat(new InitializeAgent(TestsMode.RUN, true, 100).toMessage()).isEqualTo(
                "{\"operating_mode\":{\"mode\":\"RUN\",\"wait_for_start_allowance\":true,\"max_lenght\":100}}");
        assertThat(new InitializeAgent(TestsMode.RUN, false, 200).toMessage()).isEqualTo(
                "{\"operating_mode\":{\"mode\":\"RUN\",\"wait_for_start_allowance\":false,\"max_lenght\":200}}");
        assertThat(new InitializeAgent(TestsMode.DEBUG, true, 300).toMessage()).isEqualTo(
                "{\"operating_mode\":{\"mode\":\"DEBUG\",\"wait_for_start_allowance\":true,\"max_lenght\":300}}");
        assertThat(new InitializeAgent(TestsMode.DEBUG, false, 400).toMessage()).isEqualTo(
                "{\"operating_mode\":{\"mode\":\"DEBUG\",\"wait_for_start_allowance\":false,\"max_lenght\":400}}");
    }

    @Test
    public void mapperJsonProcessingExceptionIsWrappedAsResponseException() throws Exception {
        final ObjectMapper mapper = mock(ObjectMapper.class);
        when(mapper.writeValueAsString(any(Object.class))).thenThrow(JsonProcessingException.class);

        final InitializeAgent response = new InitializeAgent(mapper, TestsMode.RUN, false, 100);

        assertThatExceptionOfType(ResponseException.class).isThrownBy(response::toMessage);
    }
}

/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.execution.server.response;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;
import org.rf.ide.core.execution.agent.TestsMode;
import org.rf.ide.core.execution.server.response.ServerResponse.ResponseException;

public class InitializeAgentTest {

    @Test
    public void properMessageIsConstructed_forInitializeAgentResponse() {
        assertThat(new InitializeAgent(TestsMode.RUN, true).toMessage())
                .isEqualTo("{\"operating_mode\":{\"mode\":\"RUN\",\"wait_for_start_allowance\":true}}");
        assertThat(new InitializeAgent(TestsMode.RUN, false).toMessage())
                .isEqualTo("{\"operating_mode\":{\"mode\":\"RUN\",\"wait_for_start_allowance\":false}}");
        assertThat(new InitializeAgent(TestsMode.DEBUG, true).toMessage())
                .isEqualTo("{\"operating_mode\":{\"mode\":\"DEBUG\",\"wait_for_start_allowance\":true}}");
        assertThat(new InitializeAgent(TestsMode.DEBUG, false).toMessage())
                .isEqualTo("{\"operating_mode\":{\"mode\":\"DEBUG\",\"wait_for_start_allowance\":false}}");
    }

    @Test(expected = ResponseException.class)
    public void mapperIOExceptionIsWrappedAsResponseException() throws Exception {
        final ObjectMapper mapper = mock(ObjectMapper.class);
        when(mapper.writeValueAsString(any(Object.class))).thenThrow(IOException.class);

        final InitializeAgent response = new InitializeAgent(mapper, TestsMode.RUN, false);

        response.toMessage();
    }
}

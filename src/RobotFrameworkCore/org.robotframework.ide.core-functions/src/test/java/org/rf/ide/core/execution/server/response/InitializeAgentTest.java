/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.execution.server.response;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.rf.ide.core.execution.agent.TestsMode;

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
}

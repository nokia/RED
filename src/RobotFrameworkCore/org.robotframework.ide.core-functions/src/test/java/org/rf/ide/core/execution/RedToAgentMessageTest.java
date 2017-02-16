/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.execution;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class RedToAgentMessageTest {

    @Test
    public void properRequestIsConstructed_forStartExecutionMessage() {
        assertThat(RedToAgentMessage.START_EXECUTION.createMessage()).isEqualTo("do_start");
        assertThat(RedToAgentMessage.START_EXECUTION.createMessage("a")).isEqualTo("do_start");
        assertThat(RedToAgentMessage.START_EXECUTION.createMessage("a", "b")).isEqualTo("do_start");
        assertThat(RedToAgentMessage.START_EXECUTION.createMessage("a", "b", "c")).isEqualTo("do_start");
    }

    @Test
    public void properRequestIsConstructed_forContinueExecutionMessage() {
        assertThat(RedToAgentMessage.CONTINUE_EXECUTION.createMessage()).isEqualTo("continue");
        assertThat(RedToAgentMessage.CONTINUE_EXECUTION.createMessage("a")).isEqualTo("continue");
        assertThat(RedToAgentMessage.CONTINUE_EXECUTION.createMessage("a", "b")).isEqualTo("continue");
        assertThat(RedToAgentMessage.CONTINUE_EXECUTION.createMessage("a", "b", "c")).isEqualTo("continue");
    }

    @Test
    public void properRequestIsConstructed_forStopExecutionMessage() {
        assertThat(RedToAgentMessage.STOP_EXECUTION.createMessage()).isEqualTo("stop");
        assertThat(RedToAgentMessage.STOP_EXECUTION.createMessage("a")).isEqualTo("stop");
        assertThat(RedToAgentMessage.STOP_EXECUTION.createMessage("a", "b")).isEqualTo("stop");
        assertThat(RedToAgentMessage.STOP_EXECUTION.createMessage("a", "b", "c")).isEqualTo("stop");
    }

    @Test
    public void properRequestIsConstructed_forResumeExecutionMessage() {
        assertThat(RedToAgentMessage.RESUME_EXECUTION.createMessage()).isEqualTo("resume");
        assertThat(RedToAgentMessage.RESUME_EXECUTION.createMessage("a")).isEqualTo("resume");
        assertThat(RedToAgentMessage.RESUME_EXECUTION.createMessage("a", "b")).isEqualTo("resume");
        assertThat(RedToAgentMessage.RESUME_EXECUTION.createMessage("a", "b", "c")).isEqualTo("resume");
    }

    @Test
    public void properRequestIsConstructed_forInterruptExecutionMessage() {
        assertThat(RedToAgentMessage.INTERRUPT_EXECUTION.createMessage()).isEqualTo("interrupt");
        assertThat(RedToAgentMessage.INTERRUPT_EXECUTION.createMessage("a")).isEqualTo("interrupt");
        assertThat(RedToAgentMessage.INTERRUPT_EXECUTION.createMessage("a", "b")).isEqualTo("interrupt");
        assertThat(RedToAgentMessage.INTERRUPT_EXECUTION.createMessage("a", "b", "c")).isEqualTo("interrupt");
    }

    @Test
    public void properRequestIsConstructed_forKeywordConditionMessage() {
        assertThat(RedToAgentMessage.EVALUATE_CONDITION.createMessage()).isEqualTo("{\"keyword_condition\":[]}");
        assertThat(RedToAgentMessage.EVALUATE_CONDITION.createMessage("a"))
                .isEqualTo("{\"keyword_condition\":[\"a\"]}");
        assertThat(RedToAgentMessage.EVALUATE_CONDITION.createMessage("a", "b"))
                .isEqualTo("{\"keyword_condition\":[\"a\",\"b\"]}");
        assertThat(RedToAgentMessage.EVALUATE_CONDITION.createMessage("a", "b", "c"))
                .isEqualTo("{\"keyword_condition\":[\"a\",\"b\",\"c\"]}");
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void exceptionIsThrown_whenConstructingRequestForVariableChangeWithoutArguments() {
        RedToAgentMessage.VARIABLE_CHANGE_REQUEST.createMessage();
    }

    @Test
    public void properRequestIsConstructed_forVariableChangeMessage() {
        assertThat(RedToAgentMessage.VARIABLE_CHANGE_REQUEST.createMessage("a"))
                .isEqualTo("{\"variable_change\":{\"a\":[]}}");
        assertThat(RedToAgentMessage.VARIABLE_CHANGE_REQUEST.createMessage("a", "b"))
                .isEqualTo("{\"variable_change\":{\"a\":[\"b\"]}}");
        assertThat(RedToAgentMessage.VARIABLE_CHANGE_REQUEST.createMessage("a", "b", "c"))
                .isEqualTo("{\"variable_change\":{\"a\":[\"b\",\"c\"]}}");
    }
}

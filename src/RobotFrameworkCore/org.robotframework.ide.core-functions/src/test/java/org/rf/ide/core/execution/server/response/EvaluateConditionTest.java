/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.execution.server.response;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.rf.ide.core.execution.server.response.ServerResponse.ResponseException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class EvaluateConditionTest {

    @Test
    public void properMessageIsConstructed_forKeywordConditionMessage() {
        assertThat(new EvaluateCondition(newArrayList()).toMessage()).isEqualTo("{\"evaluate_condition\":[]}");
        assertThat(new EvaluateCondition(newArrayList("a")).toMessage()).isEqualTo("{\"evaluate_condition\":[\"a\"]}");
        assertThat(new EvaluateCondition(newArrayList("a", "b")).toMessage())
                .isEqualTo("{\"evaluate_condition\":[\"a\",\"b\"]}");
        assertThat(new EvaluateCondition(newArrayList("a", "b", "c")).toMessage())
                .isEqualTo("{\"evaluate_condition\":[\"a\",\"b\",\"c\"]}");
    }

    @Test
    public void mapperJsonProcessingExceptionIsWrappedAsResponseException() throws Exception {
        final ObjectMapper mapper = mock(ObjectMapper.class);
        when(mapper.writeValueAsString(any(Object.class))).thenThrow(JsonProcessingException.class);

        final EvaluateCondition response = new EvaluateCondition(mapper, newArrayList("a", "b"));

        assertThatExceptionOfType(ResponseException.class).isThrownBy(response::toMessage);
    }
}

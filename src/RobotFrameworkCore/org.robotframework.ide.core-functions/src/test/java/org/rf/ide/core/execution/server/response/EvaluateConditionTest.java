/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.execution.server.response;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;
import org.rf.ide.core.execution.server.response.ServerResponse.ResponseException;

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

    @Test(expected = ResponseException.class)
    public void mapperIOExceptionIsWrappedAsResponseException() throws Exception {
        final ObjectMapper mapper = mock(ObjectMapper.class);
        when(mapper.writeValueAsString(any(Object.class))).thenThrow(IOException.class);

        final EvaluateCondition response = new EvaluateCondition(mapper, newArrayList("a", "b"));

        response.toMessage();
    }
}

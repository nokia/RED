/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.execution.server.response;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.rf.ide.core.execution.server.response.EvaluateExpression.ExpressionType;
import org.rf.ide.core.execution.server.response.ServerResponse.ResponseException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class EvaluateExpressionTest {

    @Test
    public void properMessageIsConstructed_forKeywordConditionMessage() {
        assertThat(EvaluateExpression.robot(1, "kw", newArrayList()).toMessage())
                .isEqualTo("{\"evaluate_expression\":{\"id\":1,\"type\":\"robot\",\"expr\":[\"kw\"]}}");
        assertThat(EvaluateExpression.robot(1, "kw", newArrayList("1", "2")).toMessage())
                .isEqualTo("{\"evaluate_expression\":{\"id\":1,\"type\":\"robot\",\"expr\":[\"kw\",\"1\",\"2\"]}}");

        assertThat(EvaluateExpression.variable(1, "${var}").toMessage())
                .isEqualTo("{\"evaluate_expression\":{\"id\":1,\"type\":\"variable\",\"expr\":[\"${var}\"]}}");

        assertThat(EvaluateExpression.python(1, "exit()").toMessage())
                .isEqualTo("{\"evaluate_expression\":{\"id\":1,\"type\":\"python\",\"expr\":[\"exit()\"]}}");
    }

    @Test(expected = ResponseException.class)
    public void mapperJsonProcessingExceptionIsWrappedAsResponseException() throws Exception {
        final ObjectMapper mapper = mock(ObjectMapper.class);
        when(mapper.writeValueAsString(any(Object.class))).thenThrow(JsonProcessingException.class);

        final EvaluateExpression response = new EvaluateExpression(mapper, 1, ExpressionType.ROBOT,
                newArrayList("a", "b"));

        response.toMessage();
    }
}

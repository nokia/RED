/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.execution.server.response;

import static com.google.common.collect.Lists.newArrayList;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;

public final class EvaluateExpression implements ServerResponseOnShouldContinue {

    public static EvaluateExpression robot(final int exprId, final String keywordToCall, final List<String> arguments) {
        final List<String> exprs = newArrayList(keywordToCall);
        exprs.addAll(arguments);
        return new EvaluateExpression(ResponseObjectsMapper.OBJECT_MAPPER, exprId, ExpressionType.ROBOT, exprs);
    }

    public static EvaluateExpression variable(final int exprId, final String variable) {
        return new EvaluateExpression(ResponseObjectsMapper.OBJECT_MAPPER, exprId, ExpressionType.VARIABLE,
                newArrayList(variable));
    }

    public static EvaluateExpression python(final int exprId, final String expression) {
        return new EvaluateExpression(ResponseObjectsMapper.OBJECT_MAPPER, exprId, ExpressionType.PYTHON,
                newArrayList(expression));
    }

    private final ObjectMapper mapper;

    private final int exprId;

    private final ExpressionType type;

    private final List<String> expressions;

    @VisibleForTesting
    EvaluateExpression(final ObjectMapper mapper, final int exprId, final ExpressionType type,
            final List<String> expressions) {
        this.mapper = mapper;
        this.exprId = exprId;
        this.type = type;
        this.expressions = expressions;
    }

    @Override
    public String toMessage() {
        try {
            final Map<String, Object> data = new LinkedHashMap<>();
            data.put("id", exprId);
            data.put("type", type.name().toLowerCase());
            data.put("expr", expressions);

            final Map<String, Object> value = ImmutableMap.of("evaluate_expression", data);

            return mapper.writeValueAsString(value);
        } catch (final IOException e) {
            throw new ResponseException("Unable to serialize expression evaluation response arguments to json", e);
        }
    }

    public enum ExpressionType {
        ROBOT,
        VARIABLE,
        PYTHON
    }
}

/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.execution.agent.event;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.rf.ide.core.execution.server.response.EvaluateExpression.ExpressionType;

import com.google.common.base.Objects;

public final class ExpressionEvaluatedEvent {

    public static ExpressionEvaluatedEvent from(final Map<String, Object> eventMap) {
        final List<?> arguments = (List<?>) eventMap.get("expression_result");
        final Map<String, Object> result = Events.ensureOrderedMapOfStringsToObjects((Map<?, ?>) arguments.get(0));
        final ExpressionType type = ExpressionType.valueOf(((String) result.get("type")).toUpperCase());
        final Integer id = (Integer) result.get("id");
        final String exprResult = (String) result.get("result");
        final String error = (String) result.get("error");

        if (id == null || exprResult == null && error == null) {
            throw new IllegalArgumentException(
                    "Expression event should either have evaluation result or exception message");
        }
        return new ExpressionEvaluatedEvent(id.intValue(), type, exprResult, error);
    }

    private final int id;

    private final ExpressionType type;

    private final String result;

    private final String error;


    public ExpressionEvaluatedEvent(final int id, final ExpressionType type, final String result, final String error) {
        this.id = id;
        this.type = type;
        this.result = result;
        this.error = error;
    }

    public int getId() {
        return id;
    }

    public ExpressionType getType() {
        return type;
    }

    public Optional<String> getResult() {
        return Optional.ofNullable(result);
    }

    public Optional<String> getError() {
        return Optional.ofNullable(error);
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj != null && obj.getClass() == ExpressionEvaluatedEvent.class) {
            final ExpressionEvaluatedEvent that = (ExpressionEvaluatedEvent) obj;
            return this.id == that.id && this.type == that.type && Objects.equal(this.result, that.result)
                    && Objects.equal(this.error, that.error);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id, type, result, error);
    }
}

/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.execution.agent.event;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.google.common.base.Objects;

public final class ConditionEvaluatedEvent {

    public static ConditionEvaluatedEvent from(final Map<String, Object> eventMap) {
        final List<?> arguments = (List<?>) eventMap.get("condition_result");
        final Map<String, Object> result = Events.ensureOrderedMapOfStringsToObjects((Map<?, ?>) arguments.get(0));
        final Boolean conditionResult = (Boolean) result.get("result");
        final String error = (String) result.get("error");

        if (conditionResult == null && error == null) {
            throw new IllegalArgumentException(
                    "Condition event should either have evaluation result or exception message");
        }
        return conditionResult != null ? new ConditionEvaluatedEvent(conditionResult)
                : new ConditionEvaluatedEvent(error);
    }


    private final Boolean result;

    private final String error;

    public ConditionEvaluatedEvent(final String error) {
        this.result = null;
        this.error = error;
    }

    public ConditionEvaluatedEvent(final Boolean conditionResult) {
        this.result = conditionResult;
        this.error = null;
    }

    public Optional<Boolean> getResult() {
        return Optional.ofNullable(result);
    }

    public Optional<String> getError() {
        return Optional.ofNullable(error);
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj != null && obj.getClass() == ConditionEvaluatedEvent.class) {
            final ConditionEvaluatedEvent that = (ConditionEvaluatedEvent) obj;
            return Objects.equal(this.result, that.result) && Objects.equal(this.error, that.error);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(result, error);
    }
}

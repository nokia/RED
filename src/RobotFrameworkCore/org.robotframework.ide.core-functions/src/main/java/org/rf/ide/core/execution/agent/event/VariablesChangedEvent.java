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

public final class VariablesChangedEvent {

    private final List<Map<Variable, VariableTypedValue>> variables;

    private final String error;

    public static VariablesChangedEvent from(final Map<String, Object> eventMap) {
        final Map<?, ?> arguments = (Map<?, ?>) ((List<?>) eventMap.get("vars_changed")).get(0);
        final List<Map<Variable, VariableTypedValue>> variables = Events
                .extractVariableScopes((List<?>) arguments.get("var_scopes"));
        return new VariablesChangedEvent(variables, (String) arguments.get("error"));
    }

    public VariablesChangedEvent(final List<Map<Variable, VariableTypedValue>> variables, final String error) {
        this.variables = variables;
        this.error = error;
    }

    public List<Map<Variable, VariableTypedValue>> getVariables() {
        return variables;
    }

    public boolean hasError() {
        return error != null;
    }

    public Optional<String> getError() {
        return Optional.ofNullable(error);
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj != null && obj.getClass() == VariablesChangedEvent.class) {
            final VariablesChangedEvent that = (VariablesChangedEvent) obj;
            return this.variables.equals(that.variables);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(variables);
    }
}

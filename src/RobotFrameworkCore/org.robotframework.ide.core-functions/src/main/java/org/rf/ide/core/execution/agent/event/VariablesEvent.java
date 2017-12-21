/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.execution.agent.event;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import org.rf.ide.core.testdata.model.table.variables.AVariable.VariableScope;

import com.google.common.base.Objects;

public final class VariablesEvent {

    public static VariablesEvent from(final Map<String, Object> eventMap) {
        final Map<?, ?> arguments = (Map<?, ?>) ((List<?>) eventMap.get("variables")).get(0);
        final List<?> vars_scopes = (List<?>) arguments.get("var_scopes");
        final String error = (String) arguments.get("error");

        if (vars_scopes == null) {
            throw new IllegalArgumentException("Variables events should have scopes provided");
        }
        return new VariablesEvent(extractVariableScopes(vars_scopes), error);
    }

    private static List<Map<Variable, VariableTypedValue>> extractVariableScopes(final List<?> arguments) {
        final List<Map<String, Object>> vars = Events.ensureListOfOrderedMapOfStringsToObjects(arguments);

        final List<Map<Variable, VariableTypedValue>> typedVars = new ArrayList<>();
        for (final Map<String, Object> frame : vars) {
            final Map<Variable, VariableTypedValue> typedScope = new LinkedHashMap<>();
            for (final String name : frame.keySet()) {
                final List<?> typeValScope = (List<?>) frame.get(name);
                final VariableScope scope = VariableScope.fromSimpleName((String) typeValScope.get(2));

                typedScope.put(new Variable(name, scope), reconstructTypesAndValues(typeValScope));
            }
            typedVars.add(typedScope);
        }
        return typedVars;
    }

    private static VariableTypedValue reconstructTypesAndValues(final List<?> typeAndVal) {
        final String type = (String) typeAndVal.get(0);
        final Object value = typeAndVal.get(1);

        if (value instanceof List<?>) {
            final List<Object> newValue = new ArrayList<>();
            for (final Object elem : ((List<?>) value)) {
                if (elem instanceof List<?>) {
                    newValue.add(reconstructTypesAndValues((List<?>) elem));
                } else {
                    newValue.add(new VariableTypedValue("<unknown>", elem));
                }
            }
            return new VariableTypedValue(type, newValue);

        } else if (value instanceof Map<?, ?>) {
            final Map<Object, Object> newValue = new LinkedHashMap<>();
            for (final Entry<?, ?> entry : ((Map<?, ?>) value).entrySet()) {

                if (entry.getValue() instanceof List<?>) {
                    newValue.put(entry.getKey(), reconstructTypesAndValues((List<?>) entry.getValue()));
                } else {
                    newValue.put(entry.getKey(), new VariableTypedValue("<unknown>", entry.getValue()));
                }
            }
            return new VariableTypedValue(type, newValue);
        } else {
            return new VariableTypedValue(type, value);
        }
    }


    private final List<Map<Variable, VariableTypedValue>> variables;

    private final String error;

    public VariablesEvent(final List<Map<Variable, VariableTypedValue>> variables, final String error) {
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
        if (obj != null && obj.getClass() == VariablesEvent.class) {
            final VariablesEvent that = (VariablesEvent) obj;
            return this.variables.equals(that.variables);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(variables);
    }
}

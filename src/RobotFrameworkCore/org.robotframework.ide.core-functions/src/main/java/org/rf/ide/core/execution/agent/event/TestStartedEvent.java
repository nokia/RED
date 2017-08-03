/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.execution.agent.event;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import com.google.common.base.Strings;

public final class TestStartedEvent {

    private final String name;

    private final String longName;

    private final String template;

    private final List<Map<Variable, VariableTypedValue>> variables;

    public static TestStartedEvent from(final Map<String, Object> eventMap) {
        final List<?> arguments = (List<?>) eventMap.get("start_test");
        final String name = (String) arguments.get(0);
        final Map<?, ?> attributes = (Map<?, ?>) arguments.get(1);
        final String longName = (String) attributes.get("longname");
        final String template = (String) attributes.get("template");
        final List<Map<Variable, VariableTypedValue>> variables = Events
                .extractVariableScopes((List<?>) attributes.get("vars_scopes"));

        return new TestStartedEvent(name, longName, template, variables);
    }

    public TestStartedEvent(final String name, final String longName, final String template,
            final List<Map<Variable, VariableTypedValue>> variables) {
        this.name = name;
        this.longName = longName;
        this.template = Strings.emptyToNull(template);
        this.variables = variables;
    }

    public String getName() {
        return name;
    }

    public String getLongName() {
        return longName;
    }

    public Optional<String> getTemplate() {
        return Optional.ofNullable(template);
    }

    public List<Map<Variable, VariableTypedValue>> getVariables() {
        return variables;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj != null && obj.getClass() == TestStartedEvent.class) {
            final TestStartedEvent that = (TestStartedEvent) obj;
            return this.name.equals(that.name) && this.longName.equals(that.longName)
                    && Objects.equals(this.template, that.template) && this.variables.equals(that.variables);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, longName, template, variables);
    }
}
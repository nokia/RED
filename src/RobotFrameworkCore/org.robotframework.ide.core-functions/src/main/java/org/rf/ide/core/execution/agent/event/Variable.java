/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.execution.agent.event;

import java.io.Serializable;

import org.rf.ide.core.testdata.model.table.variables.AVariable.VariableScope;

import com.google.common.base.Objects;

public final class Variable implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String name;

    private final VariableScope scope;

    public Variable(final String name) {
        this(name, null);
    }

    public Variable(final String name, final VariableScope scope) {
        this.name = name;
        this.scope = scope;
    }

    public String getName() {
        return name;
    }

    public VariableScope getScope() {
        return scope;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj != null && obj.getClass() == Variable.class) {
            final Variable that = (Variable) obj;
            return this.name.equals(that.name);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name);
    }

    @Override
    public String toString() {
        return (scope == null ? "<null>" : scope.name()) + ": " + name;
    }
}
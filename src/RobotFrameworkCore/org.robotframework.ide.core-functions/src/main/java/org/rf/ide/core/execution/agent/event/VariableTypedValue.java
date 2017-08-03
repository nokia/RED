/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.execution.agent.event;

import java.io.Serializable;

import com.google.common.base.Objects;

public final class VariableTypedValue implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String type;

    private final Object value;

    public VariableTypedValue(final String type, final Object value) {
        this.type = type;
        this.value = value;
    }

    public String getType() {
        return type;
    }

    public Object getValue() {
        return value;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj != null && obj.getClass() == VariableTypedValue.class) {
            final VariableTypedValue that = (VariableTypedValue) obj;
            return this.type.equals(that.type) && this.value.equals(that.value);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(type, value);
    }

    @Override
    public String toString() {
        return type + ": " + value.toString();
    }
}
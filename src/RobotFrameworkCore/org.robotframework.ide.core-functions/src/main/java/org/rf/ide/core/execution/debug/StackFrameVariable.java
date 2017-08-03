/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.execution.debug;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.rf.ide.core.testdata.model.table.variables.AVariable.VariableScope;

import com.google.common.base.Objects;

public final class StackFrameVariable {

    private final boolean isAutomatic;

    private final String name;

    private VariableScope scope;

    private String type;

    private Object value;

    public StackFrameVariable(final VariableScope scope, final boolean isAutomatic, final String name,
            final String type, final Object value) {
        this.scope = scope;
        this.isAutomatic = isAutomatic;
        this.name = name;
        this.type = type;
        this.value = value;
    }

    public VariableScope getScope() {
        return scope;
    }

    public void setScope(final VariableScope scope) {
        this.scope = scope;
    }

    public boolean isAutomatic() {
        return isAutomatic;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    void setType(final String type) {
        this.type = type;
    }

    public Object getValue() {
        return value;
    }

    void setValue(final Object value) {
        this.value = value;
    }

    public StackFrameVariable copy() {
        return new StackFrameVariable(scope, isAutomatic, name, type, deepClone(value));
    }

    private static Object deepClone(final Object object) {
        try {
            final ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();
            new ObjectOutputStream(byteOutput).writeObject(object);

            final ByteArrayInputStream bytesInput = new ByteArrayInputStream(byteOutput.toByteArray());
            return new ObjectInputStream(bytesInput).readObject();
        } catch (final Exception e) {
            return null;
        }
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj != null && obj.getClass() == StackFrameVariable.class) {
            final StackFrameVariable that = (StackFrameVariable) obj;
            return this.scope == that.scope && this.isAutomatic == that.isAutomatic && this.name.equals(that.name)
                    && this.type.equals(that.type) && Objects.equal(this.value, that.value);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(scope, isAutomatic, name, type, value);
    }
}

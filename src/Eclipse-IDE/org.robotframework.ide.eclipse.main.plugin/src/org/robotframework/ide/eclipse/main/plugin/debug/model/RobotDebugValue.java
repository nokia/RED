/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.debug.model;

import java.util.List;
import java.util.Map;

import org.eclipse.debug.core.model.IValue;
import org.robotframework.ide.eclipse.main.plugin.debug.model.RobotDebugVariable.RobotDebugVariableVisitor;

/**
 * @author mmarzec
 *
 */
public abstract class RobotDebugValue extends RobotDebugElement implements IValue {

    private final String type;

    private final String value;
    
    protected RobotDebugValue(final RobotDebugTarget target, final String type, final String value) {
        super(target);
        this.type = type;
        this.value = value;
    }

    public static RobotDebugValue createFromValue(final RobotDebugVariable parent, final String type,
            final Object value) {

        if (value instanceof List<?>) {
            return RobotDebugValueOfList.create(parent, type, (List<?>) value);

        } else if (value instanceof Map<?, ?>) {
            return RobotDebugValueOfDictionary.create(parent, type, (Map<?, ?>) value);

        } else {
            return RobotDebugValueOfScalar.create(parent, type, value == null ? null : value.toString());
        }
    }

    boolean isTuple() {
        return "tuple".equals(type);
    }

    @Override
    public String getReferenceTypeName() {
        return type;
    }

    @Override
    public String getValueString() {
        return value;
    }

    public abstract String getDetailedValue();

    @Override
    public boolean isAllocated() {
        return true;
    }

    @Override
    public boolean hasVariables() {
        return false;
    }

    @Override
    public RobotDebugVariable[] getVariables() {
        return new RobotDebugVariable[0];
    }

    RobotDebugVariable getVariable(final String varName) {
        for (final RobotDebugVariable variable : getVariables()) {
            if (variable.getName().equals(varName)) {
                return variable;
            }
        }
        return null;
    }

    public void visitAllVariables(final RobotDebugVariableVisitor visitor) {
        for (final RobotDebugVariable var : getVariables()) {
            var.visitAllVariables(visitor);
        }
    }
}

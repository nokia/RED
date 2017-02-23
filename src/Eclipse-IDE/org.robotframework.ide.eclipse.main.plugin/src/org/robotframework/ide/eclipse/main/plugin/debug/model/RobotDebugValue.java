/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.debug.model;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IVariable;

/**
 * @author mmarzec
 *
 */
public class RobotDebugValue extends RobotDebugElement implements IValue {

    private String value;
    
    private final IVariable[] nestedVariables;
    
    public RobotDebugValue(final RobotDebugTarget target, final String value, final IVariable[] nestedVariables) {
        super(target);
        this.value = value;
        this.nestedVariables = nestedVariables;
    }

    @Override
    public String getReferenceTypeName() throws DebugException {
        try {
            Integer.parseInt(value);
            return "integer";
        } catch (final NumberFormatException e) {
            return "text";
        }
    }

    @Override
    public String getValueString() throws DebugException {
        return value;
    }

    public void setValue(final String value) {
        this.value = value;
    }

    @Override
    public boolean isAllocated() throws DebugException {
        return true;
    }

    @Override
    public IVariable[] getVariables() throws DebugException {
        return nestedVariables;
    }

    @Override
    public boolean hasVariables() throws DebugException {
        return nestedVariables.length > 0;
    }
}

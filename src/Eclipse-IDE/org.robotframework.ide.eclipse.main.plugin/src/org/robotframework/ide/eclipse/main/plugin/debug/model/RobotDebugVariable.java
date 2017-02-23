/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.debug.model;

import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IVariable;

/**
 * @author mmarzec
 */
public class RobotDebugVariable extends RobotDebugElement implements IVariable {

    private final String name;

    private RobotDebugValue debugValue;

    private final RobotDebugVariable parent;

    private boolean isValueModificationEnabled = true;

    private boolean hasValueChanged;

    /**
     * Constructs a variable
     * 
     * @param name
     *            variable name
     * @param value
     *            variable value
     */
    public RobotDebugVariable(final RobotDebugTarget target, final String name, final Object value, final RobotDebugVariable parent) {
        super(target);
        this.name = name;
        this.parent = parent;
        debugValue = target.getRobotDebugValueManager().createRobotDebugValue(value, this, target);
    }

    @Override
    public IValue getValue() throws DebugException {
        return debugValue;
    }

    @Override
    public String getName() throws DebugException {
        return name;
    }

    @Override
    public String getReferenceTypeName() throws DebugException {
        return "RobotVariable";
    }

    @Override
    public boolean hasValueChanged() throws DebugException {
        return hasValueChanged;
    }

    @Override
    public void setValue(final String expression) throws DebugException {
        debugValue.setValue(expression);
        hasValueChanged = true;
        fireEvent(new DebugEvent(this, DebugEvent.CHANGE, DebugEvent.CLIENT_REQUEST));
        ((RobotDebugTarget) this.getDebugTarget()).sendChangeRequest(expression, name, parent);
    }

    @Override
    public void setValue(final IValue value) throws DebugException {
        debugValue.setValue(value.getValueString());
        hasValueChanged = true;
    }

    @Override
    public boolean supportsValueModification() {
        return isValueModificationEnabled;
    }

    @Override
    public boolean verifyValue(final String expression) throws DebugException {
        return true;
    }

    @Override
    public boolean verifyValue(final IValue value) throws DebugException {
        return true;
    }

    public void setHasValueChanged(final boolean valueChanged) {
        hasValueChanged = valueChanged;
    }

    public void setRobotDebugValue(final RobotDebugValue value) {
        this.debugValue = value;
    }

    public RobotDebugVariable getParent() {
        return parent;
    }

    public void setValueModificationEnabled(final boolean isValueModificationEnabled) {
        this.isValueModificationEnabled = isValueModificationEnabled;
    }
}

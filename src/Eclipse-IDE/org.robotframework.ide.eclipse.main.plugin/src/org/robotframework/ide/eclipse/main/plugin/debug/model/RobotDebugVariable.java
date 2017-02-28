/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.debug.model;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IVariable;

/**
 * @author mmarzec
 */
public class RobotDebugVariable extends RobotDebugElement implements IVariable {

    private final RobotDebugVariable parent;

    private final String name;

    private final RobotDebugValue debugValue;

    private boolean hasValueChanged;
    private boolean isValueModificationSupported;

    public RobotDebugVariable(final RobotDebugTarget target, final String name, final Object value) {
        super(target);
        this.parent = null;
        this.name = name;
        this.debugValue = RobotDebugValue.createFromValue(this, value);

        this.isValueModificationSupported = true;
        this.hasValueChanged = false;
    }

    public RobotDebugVariable(final RobotDebugTarget target, final String name, final RobotDebugValue value) {
        super(target);
        this.parent = null;
        this.name = name;
        this.debugValue = value;

        this.isValueModificationSupported = true;
        this.hasValueChanged = false;
    }

    public RobotDebugVariable(final RobotDebugVariable parent, final String name, final Object value) {
        super(parent.getDebugTarget());
        this.parent = parent;
        this.name = name;
        this.debugValue = RobotDebugValue.createFromValue(this, value);

        this.isValueModificationSupported = true;
        this.hasValueChanged = false;
    }

    @Override
    public RobotDebugValue getValue() {
        return debugValue;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getReferenceTypeName() {
        return "RobotVariable";
    }

    @Override
    public boolean hasValueChanged() {
        return hasValueChanged;
    }

    @Override
    public void setValue(final String expression) {
        debugValue.setValue(expression);
        hasValueChanged = true;

        fireChangeEvent(DebugEvent.CLIENT_REQUEST);

        final List<String> path = getPath();
        final String rootName = path.get(0);
        final List<String> arguments = newArrayList(path.subList(1, path.size()));
        arguments.add(expression);

        getDebugTarget().sendChangeRequest(rootName, arguments);
    }

    private List<String> getPath() {
        final List<String> allNames = new ArrayList<>();
        RobotDebugVariable current = this;
        while (current != null) {
            allNames.add(current.getName());
            current = current.parent;
        }
        Collections.reverse(allNames);
        return allNames.stream().map(this::extractChildName).collect(toList());
    }

    private String extractChildName(final String variableName) {
        if (variableName.startsWith("[") && variableName.endsWith("]")) {
            return variableName.substring(1, variableName.length() - 1);
        }
        return variableName;
    }

    @Override
    public void setValue(final IValue value) throws DebugException {
        debugValue.setValue(value.getValueString());
        hasValueChanged = true;
    }

    @Override
    public boolean supportsValueModification() {
        return isValueModificationSupported && debugValue.supportsModification();
    }

    public void disableValueModificationSupport() {
        this.isValueModificationSupported = false;
    }

    @Override
    public boolean verifyValue(final String expression) {
        return true;
    }

    @Override
    public boolean verifyValue(final IValue value) {
        return true;
    }

    public void setHasValueChanged(final boolean valueChanged) {
        hasValueChanged = valueChanged;
    }

    public RobotDebugVariable getParent() {
        return parent;
    }
}

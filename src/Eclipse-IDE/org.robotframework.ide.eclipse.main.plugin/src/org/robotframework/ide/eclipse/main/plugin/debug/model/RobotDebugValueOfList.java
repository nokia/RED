/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.debug.model;

import static java.util.stream.Collectors.joining;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.rf.ide.core.execution.agent.event.VariableTypedValue;

public class RobotDebugValueOfList extends RobotDebugValue {

    public static RobotDebugValueOfList create(final RobotDebugVariable parent, final String type, final List<?> list) {
        final List<RobotDebugVariable> nestedVariables = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            final VariableTypedValue typeWithValue = (VariableTypedValue) list.get(i);
            nestedVariables.add(
                    new RobotDebugVariable(parent, "[" + i + "]", typeWithValue.getType(), typeWithValue.getValue()));
        }
        final String val = type == null ? "" : type + "[" + nestedVariables.size() + "]";
        return new RobotDebugValueOfList(parent.getDebugTarget(), type, val, nestedVariables);
    }

    
    private final List<RobotDebugVariable> nestedVariables;

    private RobotDebugValueOfList(final RobotDebugTarget target, final String type, final String value,
            final List<RobotDebugVariable> nestedVariables) {
        super(target, type, value);
        this.nestedVariables = nestedVariables;
    }

    @Override
    public String getDetailedValue() {
        return Stream.of(getVariables())
                .map(var -> var.getValue().getDetailedValue())
                .collect(joining(", ", "[", "]"));
    }

    @Override
    public boolean hasVariables() {
        return !nestedVariables.isEmpty();
    }

    @Override
    public RobotDebugVariable[] getVariables() {
        return nestedVariables.toArray(new RobotDebugVariable[0]);
    }
}
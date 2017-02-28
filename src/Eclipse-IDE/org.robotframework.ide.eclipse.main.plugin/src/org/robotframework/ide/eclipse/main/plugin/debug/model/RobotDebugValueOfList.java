/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.debug.model;

import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.stream.Stream;

import com.google.common.base.Joiner;

public class RobotDebugValueOfList extends RobotDebugValue {

    private final List<RobotDebugVariable> nestedVariables;

    public RobotDebugValueOfList(final RobotDebugTarget target, final List<RobotDebugVariable> nestedVariables) {
        this(target, "List[" + nestedVariables.size() + "]", nestedVariables);
    }

    public RobotDebugValueOfList(final RobotDebugTarget target, final String value,
            final List<RobotDebugVariable> nestedVariables) {
        super(target, value);
        this.nestedVariables = nestedVariables;
    }

    @Override
    public String getDetailedValue() {
        final List<String> innerValues = Stream.of(getVariables())
                .map(var -> var.getValue().getDetailedValue())
                .collect(toList());
        return "[" + Joiner.on(", ").join(innerValues) + "]";
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

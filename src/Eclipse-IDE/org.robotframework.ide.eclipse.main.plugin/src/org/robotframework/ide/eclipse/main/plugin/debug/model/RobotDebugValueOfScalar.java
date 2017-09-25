/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.debug.model;

public class RobotDebugValueOfScalar extends RobotDebugValue {

    public static RobotDebugValueOfScalar create(final RobotDebugVariable parent, final String type,
            final String value) {
        return new RobotDebugValueOfScalar(parent.getDebugTarget(), type, value);
    }


    private RobotDebugValueOfScalar(final RobotDebugTarget target, final String type, final String value) {
        super(target, type, value);
    }

    @Override
    public String getDetailedValue() {
        return getValueString();
    }
}

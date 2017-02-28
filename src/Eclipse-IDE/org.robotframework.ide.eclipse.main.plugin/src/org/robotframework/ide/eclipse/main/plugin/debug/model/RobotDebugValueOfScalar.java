/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.debug.model;

public class RobotDebugValueOfScalar extends RobotDebugValue {

    public RobotDebugValueOfScalar(final RobotDebugTarget target, final String value) {
        super(target, value);
    }

    @Override
    public String getDetailedValue() {
        return getValueString();
    }

    @Override
    public boolean supportsModification() {
        return true;
    }
}

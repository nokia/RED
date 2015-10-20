/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.core.testData.robotImported;

import org.robotframework.ide.core.testData.model.table.variables.AVariable.VariableType;


public class ScalarRobotInternalVariable extends ARobotInternalVariable<String> {

    public ScalarRobotInternalVariable(final String name, final String value) {
        super(name, value, VariableType.SCALAR);
    }
}

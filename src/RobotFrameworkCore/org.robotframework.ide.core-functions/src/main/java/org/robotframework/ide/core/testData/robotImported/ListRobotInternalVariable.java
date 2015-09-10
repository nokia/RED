/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.core.testData.robotImported;

import java.util.List;

import org.robotframework.ide.core.testData.model.table.variables.AVariable.VariableType;


public class ListRobotInternalVariable extends ARobotInternalVariable<List<?>> {

    public ListRobotInternalVariable(String name) {
        super(name, VariableType.LIST);
    }
}

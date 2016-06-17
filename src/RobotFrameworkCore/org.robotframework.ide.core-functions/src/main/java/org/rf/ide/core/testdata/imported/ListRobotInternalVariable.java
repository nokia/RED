/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.imported;

import java.util.List;

import org.rf.ide.core.testdata.model.table.variables.AVariable.VariableType;
import org.rf.ide.core.testdata.model.table.variables.IVariableHolder;


public class ListRobotInternalVariable extends ARobotInternalVariable<List<?>> {

    public ListRobotInternalVariable(final String name, final List<?> value) {
        super(name, value, VariableType.LIST);
    }

    @Override
    public IVariableHolder copy() {
        return null;
    }
}

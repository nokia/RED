/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.imported;

import org.rf.ide.core.testdata.model.table.variables.AVariable.VariableType;
import org.rf.ide.core.testdata.model.table.variables.IVariableHolder;


public class ScalarRobotInternalVariable extends ARobotInternalVariable<String> {

    public ScalarRobotInternalVariable(final String name, final String value) {
        super(name, value, VariableType.SCALAR);
    }

    @Override
    public IVariableHolder copy() {
        return null;
    }
}

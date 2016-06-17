/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.imported;

import java.util.Map;

import org.rf.ide.core.testdata.model.table.variables.AVariable.VariableType;
import org.rf.ide.core.testdata.model.table.variables.IVariableHolder;


public class DictionaryRobotInternalVariable extends
        ARobotInternalVariable<Map<String, ?>> {

    public DictionaryRobotInternalVariable(final String name, final Map<String, ?> value) {
        super(name, value, VariableType.DICTIONARY);
    }

    @Override
    public IVariableHolder copy() {
        return null;
    }
}

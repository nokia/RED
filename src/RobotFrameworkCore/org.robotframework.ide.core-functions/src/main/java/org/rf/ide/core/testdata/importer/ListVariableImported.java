/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.importer;

import java.util.List;

import org.rf.ide.core.testdata.model.table.variables.AVariable.VariableType;
import org.rf.ide.core.testdata.model.table.variables.IVariableHolder;


public class ListVariableImported extends AVariableImported<List<?>> {

    public ListVariableImported(String name) {
        super(name, VariableType.LIST);
    }

    @Override
    public IVariableHolder copy() {
        return null;
    }
}

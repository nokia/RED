/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.core.testData.importer;

import java.util.List;

import org.robotframework.ide.core.testData.model.table.variables.AVariable.VariableType;


public class ListVariableImported extends AVariableImported<List<?>> {

    public ListVariableImported(String name) {
        super(name, VariableType.LIST);
    }
}

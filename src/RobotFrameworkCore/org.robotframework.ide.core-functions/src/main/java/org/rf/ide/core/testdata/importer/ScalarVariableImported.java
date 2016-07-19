/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.importer;

import org.rf.ide.core.testdata.model.table.variables.AVariable.VariableType;


public class ScalarVariableImported extends AVariableImported<String> {

    public ScalarVariableImported(final String name) {
        super(name, VariableType.SCALAR);
    }
}

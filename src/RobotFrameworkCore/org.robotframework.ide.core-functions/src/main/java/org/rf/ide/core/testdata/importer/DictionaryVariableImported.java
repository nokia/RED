/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.importer;

import java.util.Map;

import org.rf.ide.core.testdata.model.table.variables.AVariable.VariableType;


public class DictionaryVariableImported extends
        AVariableImported<Map<String, ?>> {

    public DictionaryVariableImported(final String name) {
        super(name, VariableType.DICTIONARY);
    }
}

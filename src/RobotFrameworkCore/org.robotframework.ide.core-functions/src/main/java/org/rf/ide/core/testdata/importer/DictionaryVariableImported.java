/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testData.importer;

import java.util.Map;

import org.rf.ide.core.testData.model.table.variables.AVariable.VariableType;


public class DictionaryVariableImported extends
        AVariableImported<Map<String, ?>> {

    public DictionaryVariableImported(String name) {
        super(name, VariableType.DICTIONARY);
    }
}

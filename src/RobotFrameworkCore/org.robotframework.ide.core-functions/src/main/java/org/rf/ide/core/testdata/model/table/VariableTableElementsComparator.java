/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table;

import java.util.LinkedHashMap;
import java.util.Map;

import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.ModelType;


public class VariableTableElementsComparator extends
        AModelTypeComparator<AModelElement<VariableTable>> {

    private final static Map<ModelType, Integer> POSITION = new LinkedHashMap<>();
    static {
        int startPosition = 1;
        POSITION.put(ModelType.SCALAR_VARIABLE_DECLARATION_IN_TABLE,
                startPosition);
        POSITION.put(ModelType.LIST_VARIABLE_DECLARATION_IN_TABLE,
                ++startPosition);
        POSITION.put(ModelType.DICTIONARY_VARIABLE_DECLARATION_IN_TABLE,
                ++startPosition);
    }


    public VariableTableElementsComparator() {
        super(POSITION);
    }
}

/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.core.testData.model.table;

import java.util.LinkedHashMap;
import java.util.Map;

import org.robotframework.ide.core.testData.model.AModelElement;
import org.robotframework.ide.core.testData.model.ModelType;


public class VariableTableElementsComparator extends
        AModelTypeComparator<AModelElement<VariableTable>> {

    private final static Map<ModelType, Integer> position = new LinkedHashMap<>();
    static {
        int startPosition = 1;
        position.put(ModelType.SCALAR_VARIABLE_DECLARATION_IN_TABLE,
                startPosition);
        position.put(ModelType.LIST_VARIABLE_DECLARATION_IN_TABLE,
                ++startPosition);
        position.put(ModelType.DICTIONARY_VARIABLE_DECLARATION_IN_TABLE,
                ++startPosition);
    }


    public VariableTableElementsComparator() {
        super(position);
    }
}

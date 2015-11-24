/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table;

import java.util.LinkedHashMap;
import java.util.Map;

import org.rf.ide.core.testdata.model.ModelType;


public class TableHeaderComparator extends
        AModelTypeComparator<TableHeader<? extends ARobotSectionTable>> {

    private final static Map<ModelType, Integer> POSITION = new LinkedHashMap<>();
    static {
        int startPosition = 1;
        POSITION.put(ModelType.SETTINGS_TABLE_HEADER, startPosition);
        POSITION.put(ModelType.VARIABLES_TABLE_HEADER, ++startPosition);
        POSITION.put(ModelType.TEST_CASE_TABLE_HEADER, ++startPosition);
        POSITION.put(ModelType.KEYWORDS_TABLE_HEADER, ++startPosition);
    }


    public TableHeaderComparator() {
        super(POSITION);
    }
}
/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.core.testData.model.table;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.robotframework.ide.core.testData.model.FilePosition;
import org.robotframework.ide.core.testData.model.ModelType;


public class TableHeaderComparator implements Comparator<TableHeader> {

    private Map<ModelType, Integer> position = new LinkedHashMap<>();


    public TableHeaderComparator() {
        int startPosition = 1;
        position.put(ModelType.SETTINGS_TABLE_HEADER, startPosition);
        position.put(ModelType.VARIABLES_TABLE_HEADER, ++startPosition);
        position.put(ModelType.TEST_CASE_TABLE_HEADER, ++startPosition);
        position.put(ModelType.KEYWORDS_TABLE_HEADER, ++startPosition);
    }


    @Override
    public int compare(TableHeader header1, TableHeader header2) {
        int result = ECompareResult.EQUAL.getValue();
        FilePosition header1FilePos = header1.getBeginPosition();
        FilePosition header2FilePos = header2.getBeginPosition();

        if (header1FilePos.isNotSet() && header2FilePos.isNotSet()) {
            result = Integer.compare(position.get(header1.getModelType()),
                    position.get(header2.getModelType()));
        } else if (header1FilePos.isNotSet()) {
            result = ECompareResult.LESS.getValue();
        } else if (header2FilePos.isNotSet()) {
            result = ECompareResult.GREATER.getValue();
        } else {
            result = header1FilePos.compare(header2FilePos);
        }

        return result;
    }

}
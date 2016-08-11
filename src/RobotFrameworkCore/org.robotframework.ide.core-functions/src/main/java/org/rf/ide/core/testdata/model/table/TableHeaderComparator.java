/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.FilePosition;
import org.rf.ide.core.testdata.model.ModelType;

public class TableHeaderComparator<T extends AModelElement<?>> implements Comparator<T> {

    private final static Map<ModelType, Integer> POSITION = new LinkedHashMap<>();

    static {
        int startPosition = 1;
        POSITION.put(ModelType.SETTINGS_TABLE_HEADER, startPosition);
        POSITION.put(ModelType.VARIABLES_TABLE_HEADER, ++startPosition);
        POSITION.put(ModelType.TEST_CASE_TABLE_HEADER, ++startPosition);
        POSITION.put(ModelType.KEYWORDS_TABLE_HEADER, ++startPosition);
    }

    @Override
    public int compare(T modelElement1, T modelElement2) {
        int result = ECompareResult.EQUAL_TO.getValue();
        FilePosition filePos1 = modelElement1.getBeginPosition();
        FilePosition filePos2 = modelElement2.getBeginPosition();

        if (filePos1.isNotSet() || filePos2.isNotSet()) {
            result = Integer.compare(POSITION.get(modelElement1.getModelType()),
                    POSITION.get(modelElement2.getModelType()));
        } else {
            result = filePos1.compare(filePos2);
        }

        return result;
    }
}

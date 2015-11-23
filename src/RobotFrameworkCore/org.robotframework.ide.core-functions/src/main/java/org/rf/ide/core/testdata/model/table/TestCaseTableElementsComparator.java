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
import org.rf.ide.core.testdata.model.table.testCases.TestCase;


public class TestCaseTableElementsComparator extends
        AModelTypeComparator<AModelElement<TestCase>> {

    private final static Map<ModelType, Integer> position = new LinkedHashMap<>();
    static {
        int startPosition = 1;
        position.put(ModelType.TEST_CASE, startPosition);
        position.put(ModelType.TEST_CASE_DOCUMENTATION, ++startPosition);
        position.put(ModelType.TEST_CASE_TAGS, ++startPosition);
        position.put(ModelType.TEST_CASE_SETUP, ++startPosition);
        position.put(ModelType.TEST_CASE_TEMPLATE, ++startPosition);
        position.put(ModelType.TEST_CASE_TIMEOUT, ++startPosition);
        position.put(ModelType.TEST_CASE_EXECUTABLE_ROW, ++startPosition);
        position.put(ModelType.TEST_CASE_TEARDOWN, ++startPosition);
    }


    public TestCaseTableElementsComparator() {
        super(position);

    }
}

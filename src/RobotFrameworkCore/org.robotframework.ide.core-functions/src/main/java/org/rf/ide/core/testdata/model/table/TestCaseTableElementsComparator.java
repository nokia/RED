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
import org.rf.ide.core.testdata.model.table.testcases.TestCase;

public class TestCaseTableElementsComparator extends AModelTypeComparator<AModelElement<TestCase>> {

    private final static Map<ModelType, Integer> POSITION = new LinkedHashMap<>();

    static {
        int startPosition = 1;
        POSITION.put(ModelType.TEST_CASE, startPosition);
        POSITION.put(ModelType.TEST_CASE_DOCUMENTATION, ++startPosition);
        POSITION.put(ModelType.TEST_CASE_TAGS, ++startPosition);
        POSITION.put(ModelType.TEST_CASE_SETUP, ++startPosition);
        POSITION.put(ModelType.TEST_CASE_TEMPLATE, ++startPosition);
        POSITION.put(ModelType.TEST_CASE_TIMEOUT, ++startPosition);
        POSITION.put(ModelType.TEST_CASE_EXECUTABLE_ROW, ++startPosition);
        POSITION.put(ModelType.TEST_CASE_TEARDOWN, ++startPosition);
        POSITION.put(ModelType.TEST_CASE_SETTING_UNKNOWN, ++startPosition);
    }

    public TestCaseTableElementsComparator() {
        super(POSITION);

    }
}

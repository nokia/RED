/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testData.model.table.testCases;

import org.rf.ide.core.testData.model.AKeywordBaseSetting;
import org.rf.ide.core.testData.model.ModelType;
import org.rf.ide.core.testData.text.read.recognizer.RobotToken;


public class TestCaseTeardown extends AKeywordBaseSetting<TestCase> {

    public TestCaseTeardown(RobotToken declaration) {
        super(declaration);
    }


    @Override
    public ModelType getModelType() {
        return ModelType.TEST_CASE_TEARDOWN;
    }
}

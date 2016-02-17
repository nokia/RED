/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table.testcases;

import java.util.ArrayList;
import java.util.List;

import org.rf.ide.core.testdata.model.AKeywordBaseSetting;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;

public class TestCaseTeardown extends AKeywordBaseSetting<TestCase> {

    public TestCaseTeardown(RobotToken declaration) {
        super(declaration);
    }

    @Override
    public ModelType getModelType() {
        return ModelType.TEST_CASE_TEARDOWN;
    }

    @Override
    protected List<AKeywordBaseSetting<TestCase>> getAllThisKindSettings() {
        final List<AKeywordBaseSetting<TestCase>> settings = new ArrayList<>(0);
        settings.addAll(getParent().getTeardowns());

        return settings;
    }
}

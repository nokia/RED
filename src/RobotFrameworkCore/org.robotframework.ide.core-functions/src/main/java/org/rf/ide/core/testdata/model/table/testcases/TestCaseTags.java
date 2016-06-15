/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table.testcases;

import org.rf.ide.core.testdata.model.ATags;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.text.read.IRobotTokenType;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

public class TestCaseTags extends ATags<TestCase> {

    private static final long serialVersionUID = -7809783360234115293L;

    public TestCaseTags(RobotToken declaration) {
        super(declaration);
    }

    @Override
    public ModelType getModelType() {
        return ModelType.TEST_CASE_TAGS;
    }

    @Override
    public IRobotTokenType getTagType() {
        return RobotTokenType.TEST_CASE_SETTING_TAGS;
    }
}

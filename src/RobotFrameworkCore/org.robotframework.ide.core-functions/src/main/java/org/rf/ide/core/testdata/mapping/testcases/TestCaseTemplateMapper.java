/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.mapping.testcases;

import org.rf.ide.core.testdata.model.RobotVersion;
import org.rf.ide.core.testdata.model.table.testcases.TestCase;
import org.rf.ide.core.testdata.model.table.testcases.TestCaseTemplate;
import org.rf.ide.core.testdata.text.read.ParsingState;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

public class TestCaseTemplateMapper extends ATestCaseSettingDeclarationMapper {

    public TestCaseTemplateMapper() {
        super(RobotTokenType.TEST_CASE_SETTING_TEMPLATE, ParsingState.TEST_CASE_SETTING_TEST_TEMPLATE);
    }

    @Override
    public boolean isApplicableFor(final RobotVersion robotVersion) {
        return robotVersion.isNewerOrEqualTo(new RobotVersion(3, 0));
    }

    @Override
    protected void createSetting(final RobotToken rt, final TestCase testCase) {
        testCase.addElement(new TestCaseTemplate(rt));
    }
}

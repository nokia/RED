/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.mapping.testcases;

import org.rf.ide.core.testdata.model.RobotVersion;
import org.rf.ide.core.testdata.model.table.testcases.TestCase;
import org.rf.ide.core.testdata.model.table.testcases.TestCaseTemplate;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

public class TestCaseTemplateMapperOld extends TestCaseTemplateMapper {

    @Override
    public boolean isApplicableFor(final RobotVersion robotVersion) {
        return robotVersion.isOlderThan(new RobotVersion(3, 0));
    }

    @Override
    protected void createSetting(final RobotToken rt, final TestCase testCase) {
        if (testCase.getTemplates().isEmpty()) {
            testCase.addElement(new TestCaseTemplate(rt));
        } else {
            rt.getTypes().add(1, RobotTokenType.TEST_CASE_SETTING_NAME_DUPLICATION);
        }
    }
}

/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see licence.txt file for details.
 */
package org.robotframework.ide.core.testData.model.mapping.hashComment.tableTestCase;

import java.util.List;

import org.robotframework.ide.core.testData.model.RobotFile;
import org.robotframework.ide.core.testData.model.mapping.IHashCommentMapper;
import org.robotframework.ide.core.testData.model.table.testCases.TestCase;
import org.robotframework.ide.core.testData.model.table.testCases.TestCaseTemplate;
import org.robotframework.ide.core.testData.text.read.ParsingState;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;


public class TestCaseSettingTemplateCommentMapper implements IHashCommentMapper {

    @Override
    public boolean isApplicable(ParsingState state) {
        return (state == ParsingState.TEST_CASE_SETTING_TEST_TEMPLATE
                || state == ParsingState.TEST_CASE_SETTING_TEST_TEMPLATE_KEYWORD || state == ParsingState.TEST_CASE_SETTING_TEST_TEMPLATE_KEYWORD_UNWANTED_ARGUMENTS);
    }


    @Override
    public void map(RobotToken rt, ParsingState currentState,
            RobotFile fileModel) {
        List<TestCase> testCases = fileModel.getTestCaseTable().getTestCases();
        TestCase testCase = testCases.get(testCases.size() - 1);

        List<TestCaseTemplate> templates = testCase.getTemplates();
        TestCaseTemplate testCaseTemplate = templates.get(templates.size() - 1);
        testCaseTemplate.addCommentPart(rt);
    }
}

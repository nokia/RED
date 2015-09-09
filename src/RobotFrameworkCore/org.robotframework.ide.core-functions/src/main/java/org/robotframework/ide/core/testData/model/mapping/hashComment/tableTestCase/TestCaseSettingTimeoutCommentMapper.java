/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.core.testData.model.mapping.hashComment.tableTestCase;

import java.util.List;

import org.robotframework.ide.core.testData.model.RobotFile;
import org.robotframework.ide.core.testData.model.mapping.IHashCommentMapper;
import org.robotframework.ide.core.testData.model.table.testCases.TestCase;
import org.robotframework.ide.core.testData.model.table.testCases.TestCaseTimeout;
import org.robotframework.ide.core.testData.text.read.ParsingState;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;


public class TestCaseSettingTimeoutCommentMapper implements IHashCommentMapper {

    @Override
    public boolean isApplicable(ParsingState state) {
        return (state == ParsingState.TEST_CASE_SETTING_TEST_TIMEOUT
                || state == ParsingState.TEST_CASE_SETTING_TEST_TIMEOUT_VALUE || state == ParsingState.TEST_CASE_SETTING_TEST_TIMEOUT_MESSAGE_ARGUMENTS);
    }


    @Override
    public void map(RobotToken rt, ParsingState currentState,
            RobotFile fileModel) {
        List<TestCase> testCases = fileModel.getTestCaseTable().getTestCases();
        TestCase testCase = testCases.get(testCases.size() - 1);

        List<TestCaseTimeout> timeouts = testCase.getTimeouts();
        TestCaseTimeout testCaseTimeout = timeouts.get(timeouts.size() - 1);
        testCaseTimeout.addCommentPart(rt);
    }
}

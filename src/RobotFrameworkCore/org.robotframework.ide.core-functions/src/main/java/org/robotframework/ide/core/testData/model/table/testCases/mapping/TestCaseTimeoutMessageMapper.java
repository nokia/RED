/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.core.testData.model.table.testCases.mapping;

import java.util.List;
import java.util.Stack;

import org.robotframework.ide.core.testData.model.FilePosition;
import org.robotframework.ide.core.testData.model.RobotFileOutput;
import org.robotframework.ide.core.testData.model.table.mapping.ElementsUtility;
import org.robotframework.ide.core.testData.model.table.mapping.IParsingMapper;
import org.robotframework.ide.core.testData.model.table.testCases.TestCase;
import org.robotframework.ide.core.testData.model.table.testCases.TestCaseTimeout;
import org.robotframework.ide.core.testData.text.read.IRobotTokenType;
import org.robotframework.ide.core.testData.text.read.ParsingState;
import org.robotframework.ide.core.testData.text.read.RobotLine;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotTokenType;

import com.google.common.annotations.VisibleForTesting;


public class TestCaseTimeoutMessageMapper implements IParsingMapper {

    private final ElementsUtility utility;


    public TestCaseTimeoutMessageMapper() {
        this.utility = new ElementsUtility();
    }


    @Override
    public RobotToken map(RobotLine currentLine,
            Stack<ParsingState> processingState,
            RobotFileOutput robotFileOutput, RobotToken rt, FilePosition fp,
            String text) {
        List<IRobotTokenType> types = rt.getTypes();
        types.remove(RobotTokenType.UNKNOWN);
        types.add(0, RobotTokenType.TEST_CASE_SETTING_TIMEOUT_MESSAGE);

        rt.setRaw(new StringBuilder(text));
        rt.setText(new StringBuilder(text));

        List<TestCase> testCases = robotFileOutput.getFileModel()
                .getTestCaseTable().getTestCases();
        TestCase testCase = testCases.get(testCases.size() - 1);
        List<TestCaseTimeout> timeouts = testCase.getTimeouts();
        TestCaseTimeout testCaseTimeout = timeouts.get(timeouts.size() - 1);
        testCaseTimeout.addMessagePart(rt);

        processingState
                .push(ParsingState.TEST_CASE_SETTING_TEST_TIMEOUT_MESSAGE_ARGUMENTS);

        return rt;
    }


    @Override
    public boolean checkIfCanBeMapped(RobotFileOutput robotFileOutput,
            RobotLine currentLine, RobotToken rt, String text,
            Stack<ParsingState> processingState) {
        boolean result;
        if (!processingState.isEmpty()) {
            ParsingState currentState = utility
                    .getCurrentStatus(processingState);
            if (currentState == ParsingState.TEST_CASE_SETTING_TEST_TIMEOUT_VALUE
                    || currentState == ParsingState.TEST_CASE_SETTING_TEST_TIMEOUT_MESSAGE_ARGUMENTS) {
                result = true;
            } else if (currentState == ParsingState.TEST_CASE_SETTING_TEST_TIMEOUT) {
                List<TestCase> testCases = robotFileOutput.getFileModel()
                        .getTestCaseTable().getTestCases();
                List<TestCaseTimeout> timeouts = testCases.get(
                        testCases.size() - 1).getTimeouts();
                result = checkIfHasAlreadyValue(timeouts);
            } else {
                result = false;
            }
        } else {
            result = false;
        }
        return result;
    }


    @VisibleForTesting
    protected boolean checkIfHasAlreadyValue(
            List<TestCaseTimeout> testCaseTimeouts) {
        boolean result = false;
        for (TestCaseTimeout setting : testCaseTimeouts) {
            result = (setting.getTimeout() != null);
            result = result || !setting.getMessage().isEmpty();
            if (result) {
                break;
            }
        }

        return result;
    }

}

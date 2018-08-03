/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.mapping.testcases;

import java.util.List;
import java.util.Stack;

import org.rf.ide.core.testdata.mapping.table.IParsingMapper;
import org.rf.ide.core.testdata.mapping.table.ParsingStateHelper;
import org.rf.ide.core.testdata.model.FilePosition;
import org.rf.ide.core.testdata.model.RobotFileOutput;
import org.rf.ide.core.testdata.model.table.testcases.TestCase;
import org.rf.ide.core.testdata.model.table.testcases.TestCaseTimeout;
import org.rf.ide.core.testdata.text.read.IRobotTokenType;
import org.rf.ide.core.testdata.text.read.ParsingState;
import org.rf.ide.core.testdata.text.read.RobotLine;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

public class TestCaseTimeoutMessageMapper implements IParsingMapper {

    private final ParsingStateHelper stateHelper = new ParsingStateHelper();


    @Override
    public boolean checkIfCanBeMapped(final RobotFileOutput robotFileOutput, final RobotLine currentLine,
            final RobotToken rt, final String text, final Stack<ParsingState> processingState) {

        final ParsingState currentState = stateHelper.getCurrentStatus(processingState);
        if (currentState == ParsingState.TEST_CASE_SETTING_TEST_TIMEOUT) {
            final List<TestCase> testCases = robotFileOutput.getFileModel().getTestCaseTable().getTestCases();
            final List<TestCaseTimeout> timeouts = testCases.get(testCases.size() - 1).getTimeouts();
            return hasValueAlready(timeouts);
        }
        return currentState == ParsingState.TEST_CASE_SETTING_TEST_TIMEOUT_VALUE
                || currentState == ParsingState.TEST_CASE_SETTING_TEST_TIMEOUT_MESSAGE_ARGUMENTS;
    }

    private boolean hasValueAlready(final List<TestCaseTimeout> testCaseTimeouts) {
        return !testCaseTimeouts.isEmpty() && testCaseTimeouts.get(testCaseTimeouts.size() - 1).getTimeout() != null;
    }

    @Override
    public RobotToken map(final RobotLine currentLine, final Stack<ParsingState> processingState,
            final RobotFileOutput robotFileOutput, final RobotToken rt, final FilePosition fp, final String text) {

        final List<IRobotTokenType> types = rt.getTypes();
        types.remove(RobotTokenType.UNKNOWN);
        types.add(0, RobotTokenType.TEST_CASE_SETTING_TIMEOUT_MESSAGE);
        rt.setText(text);

        final List<TestCase> testCases = robotFileOutput.getFileModel().getTestCaseTable().getTestCases();
        final TestCase testCase = testCases.get(testCases.size() - 1);
        final List<TestCaseTimeout> timeouts = testCase.getTimeouts();
        if (!timeouts.isEmpty()) {
            timeouts.get(timeouts.size() - 1).addMessagePart(rt);
        }

        processingState.push(ParsingState.TEST_CASE_SETTING_TEST_TIMEOUT_MESSAGE_ARGUMENTS);
        return rt;
    }
}

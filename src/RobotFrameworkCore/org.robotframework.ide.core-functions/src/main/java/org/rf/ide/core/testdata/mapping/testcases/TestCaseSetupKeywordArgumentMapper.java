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
import org.rf.ide.core.testdata.model.table.LocalSetting;
import org.rf.ide.core.testdata.model.table.testcases.TestCase;
import org.rf.ide.core.testdata.text.read.ParsingState;
import org.rf.ide.core.testdata.text.read.RobotLine;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;

public class TestCaseSetupKeywordArgumentMapper implements IParsingMapper {

    private final ParsingStateHelper stateHelper = new ParsingStateHelper();

    @Override
    public boolean checkIfCanBeMapped(final RobotFileOutput robotFileOutput, final RobotLine currentLine,
            final RobotToken rt, final String text, final Stack<ParsingState> processingState) {

        final ParsingState state = stateHelper.getCurrentState(processingState);
        if (state == ParsingState.TEST_CASE_SETTING_SETUP) {
            final List<TestCase> tests = robotFileOutput.getFileModel().getTestCaseTable().getTestCases();
            final List<LocalSetting<TestCase>> setups = tests.get(tests.size() - 1).getSetups();
            return TestCaseSetupKeywordMapper.hasKeywordNameAlready(setups);
        }
        return state == ParsingState.TEST_CASE_SETTING_SETUP_KEYWORD
                || state == ParsingState.TEST_CASE_SETTING_SETUP_KEYWORD_ARGUMENT;
    }

    @Override
    public RobotToken map(final RobotLine currentLine, final Stack<ParsingState> processingState,
            final RobotFileOutput robotFileOutput, final RobotToken rt, final FilePosition fp, final String text) {

        rt.setText(text);

        final List<TestCase> testCases = robotFileOutput.getFileModel().getTestCaseTable().getTestCases();
        final TestCase testCase = testCases.get(testCases.size() - 1);
        final List<LocalSetting<TestCase>> setups = testCase.getSetups();
        final LocalSetting<TestCase> tcs = setups.get(setups.size() - 1);
        tcs.addToken(rt);

        processingState.push(ParsingState.TEST_CASE_SETTING_SETUP_KEYWORD_ARGUMENT);
        return rt;
    }
}

/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.mapping.testcases;

import java.util.List;
import java.util.Stack;

import org.rf.ide.core.testdata.mapping.table.ElementsUtility;
import org.rf.ide.core.testdata.mapping.table.IParsingMapper;
import org.rf.ide.core.testdata.mapping.table.ParsingStateHelper;
import org.rf.ide.core.testdata.model.FilePosition;
import org.rf.ide.core.testdata.model.RobotFileOutput;
import org.rf.ide.core.testdata.model.table.testcases.TestCase;
import org.rf.ide.core.testdata.model.table.testcases.TestCaseSetup;
import org.rf.ide.core.testdata.text.read.IRobotTokenType;
import org.rf.ide.core.testdata.text.read.ParsingState;
import org.rf.ide.core.testdata.text.read.RobotLine;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

public class TestCaseSetupKeywordArgumentMapper implements IParsingMapper {

    private final ElementsUtility utility;

    private final ParsingStateHelper stateHelper;

    public TestCaseSetupKeywordArgumentMapper() {
        this.utility = new ElementsUtility();
        this.stateHelper = new ParsingStateHelper();
    }

    @Override
    public RobotToken map(final RobotLine currentLine, final Stack<ParsingState> processingState,
            final RobotFileOutput robotFileOutput, final RobotToken rt, final FilePosition fp, final String text) {
        final List<IRobotTokenType> types = rt.getTypes();
        types.remove(RobotTokenType.UNKNOWN);
        types.add(0, RobotTokenType.TEST_CASE_SETTING_SETUP_KEYWORD_ARGUMENT);

        rt.setText(text);
        final List<TestCase> testCases = robotFileOutput.getFileModel().getTestCaseTable().getTestCases();
        final TestCase testCase = testCases.get(testCases.size() - 1);
        final List<TestCaseSetup> setups = testCase.getSetups();
        for (final TestCaseSetup tcs : setups) {
            if (tcs.getKeywordName() != null && !tcs.getKeywordName().getFilePosition().isNotSet()) {
                tcs.addArgument(rt);
                break;
            }
        }

        processingState.push(ParsingState.TEST_CASE_SETTING_SETUP_KEYWORD_ARGUMENT);

        return rt;
    }

    @Override
    public boolean checkIfCanBeMapped(final RobotFileOutput robotFileOutput, final RobotLine currentLine,
            final RobotToken rt, final String text, final Stack<ParsingState> processingState) {
        boolean result = false;
        final ParsingState state = stateHelper.getCurrentStatus(processingState);
        if (state == ParsingState.TEST_CASE_SETTING_SETUP) {
            final List<TestCase> tests = robotFileOutput.getFileModel().getTestCaseTable().getTestCases();
            final List<TestCaseSetup> setups = tests.get(tests.size() - 1).getSetups();
            result = utility.checkIfHasAlreadyKeywordName(setups);
        } else if (state == ParsingState.TEST_CASE_SETTING_SETUP_KEYWORD
                || state == ParsingState.TEST_CASE_SETTING_SETUP_KEYWORD_ARGUMENT) {
            result = true;
        }

        return result;
    }

}

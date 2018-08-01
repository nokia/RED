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
import org.rf.ide.core.testdata.model.table.testcases.TestCaseTemplate;
import org.rf.ide.core.testdata.text.read.IRobotTokenType;
import org.rf.ide.core.testdata.text.read.ParsingState;
import org.rf.ide.core.testdata.text.read.RobotLine;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

public class TestCaseTemplateKeywordMapper implements IParsingMapper {

    private final ParsingStateHelper stateHelper = new ParsingStateHelper();

    @Override
    public boolean checkIfCanBeMapped(final RobotFileOutput robotFileOutput, final RobotLine currentLine,
            final RobotToken rt, final String text, final Stack<ParsingState> processingState) {

        if (stateHelper.getCurrentStatus(processingState) == ParsingState.TEST_CASE_SETTING_TEST_TEMPLATE) {
            final List<TestCase> tests = robotFileOutput.getFileModel().getTestCaseTable().getTestCases();
            final List<TestCaseTemplate> templates = tests.get(tests.size() - 1).getTemplates();
            return !checkIfHasAlreadyKeywordName(templates);
        }
        return false;
    }

    private boolean checkIfHasAlreadyKeywordName(final List<TestCaseTemplate> templates) {
        return !templates.isEmpty() && templates.get(templates.size() - 1).getKeywordName() != null;
    }

    @Override
    public RobotToken map(final RobotLine currentLine, final Stack<ParsingState> processingState,
            final RobotFileOutput robotFileOutput, final RobotToken rt, final FilePosition fp, final String text) {

        final List<IRobotTokenType> types = rt.getTypes();
        types.remove(RobotTokenType.UNKNOWN);
        types.add(0, RobotTokenType.TEST_CASE_SETTING_TEMPLATE_KEYWORD_NAME);
        rt.setText(text);

        final List<TestCase> testCases = robotFileOutput.getFileModel().getTestCaseTable().getTestCases();
        final TestCase testCase = testCases.get(testCases.size() - 1);
        final List<TestCaseTemplate> templates = testCase.getTemplates();
        if (!templates.isEmpty()) {
            templates.get(templates.size() - 1).setKeywordName(rt);
        }

        processingState.push(ParsingState.TEST_CASE_SETTING_TEST_TEMPLATE_KEYWORD);
        return rt;
    }
}

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
import org.robotframework.ide.core.testData.model.table.mapping.IParsingMapper;
import org.robotframework.ide.core.testData.model.table.mapping.ParsingStateHelper;
import org.robotframework.ide.core.testData.model.table.testCases.TestCase;
import org.robotframework.ide.core.testData.model.table.testCases.TestCaseTemplate;
import org.robotframework.ide.core.testData.text.read.IRobotTokenType;
import org.robotframework.ide.core.testData.text.read.ParsingState;
import org.robotframework.ide.core.testData.text.read.RobotLine;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotTokenType;

import com.google.common.annotations.VisibleForTesting;


public class TestCaseTemplateKeywordMapper implements IParsingMapper {

    private final ParsingStateHelper stateHelper;


    public TestCaseTemplateKeywordMapper() {
        this.stateHelper = new ParsingStateHelper();
    }


    @Override
    public RobotToken map(final RobotLine currentLine,
            final Stack<ParsingState> processingState,
            final RobotFileOutput robotFileOutput, final RobotToken rt, final FilePosition fp,
            final String text) {
        final List<IRobotTokenType> types = rt.getTypes();
        types.remove(RobotTokenType.UNKNOWN);
        types.add(0, RobotTokenType.TEST_CASE_SETTING_TEMPLATE_KEYWORD_NAME);

        rt.setText(text);
        rt.setRaw(text);
        final List<TestCase> testCases = robotFileOutput.getFileModel()
                .getTestCaseTable().getTestCases();
        final TestCase testCase = testCases.get(testCases.size() - 1);
        final List<TestCaseTemplate> templates = testCase.getTemplates();

        final TestCaseTemplate testTemplate = templates.get(templates.size() - 1);
        testTemplate.setKeywordName(rt);

        processingState
                .push(ParsingState.TEST_CASE_SETTING_TEST_TEMPLATE_KEYWORD);

        return rt;
    }


    @Override
    public boolean checkIfCanBeMapped(final RobotFileOutput robotFileOutput,
            final RobotLine currentLine, final RobotToken rt, final String text,
            final Stack<ParsingState> processingState) {
        boolean result = false;
        final ParsingState state = stateHelper.getCurrentStatus(processingState);
        if (state == ParsingState.TEST_CASE_SETTING_TEST_TEMPLATE) {
            final List<TestCase> tests = robotFileOutput.getFileModel()
                    .getTestCaseTable().getTestCases();
            final List<TestCaseTemplate> templates = tests.get(tests.size() - 1)
                    .getTemplates();
            result = !checkIfHasAlreadyKeywordName(templates);
        }

        return result;
    }


    @VisibleForTesting
    protected boolean checkIfHasAlreadyKeywordName(
            final List<TestCaseTemplate> testCaseTemplates) {
        boolean result = false;
        for (final TestCaseTemplate setting : testCaseTemplates) {
            result = (setting.getKeywordName() != null);
            result = result || !setting.getUnexpectedTrashArguments().isEmpty();
            if (result) {
                break;
            }
        }

        return result;
    }
}

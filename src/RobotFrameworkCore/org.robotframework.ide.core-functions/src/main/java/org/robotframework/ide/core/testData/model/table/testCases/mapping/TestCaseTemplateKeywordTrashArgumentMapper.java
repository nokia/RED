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


public class TestCaseTemplateKeywordTrashArgumentMapper implements
        IParsingMapper {

    private final ParsingStateHelper stateHelper;


    public TestCaseTemplateKeywordTrashArgumentMapper() {
        this.stateHelper = new ParsingStateHelper();
    }


    @Override
    public RobotToken map(final RobotLine currentLine,
            final Stack<ParsingState> processingState,
            final RobotFileOutput robotFileOutput, final RobotToken rt, final FilePosition fp,
            final String text) {
        final List<IRobotTokenType> types = rt.getTypes();
        types.remove(RobotTokenType.UNKNOWN);
        types.add(
                0,
                RobotTokenType.TEST_CASE_SETTING_TEMPLATE_KEYWORD_UNWANTED_ARGUMENT);

        rt.setText(text);
        rt.setRaw(text);
        final List<TestCase> testCases = robotFileOutput.getFileModel()
                .getTestCaseTable().getTestCases();
        final TestCase testCase = testCases.get(testCases.size() - 1);
        final List<TestCaseTemplate> templates = testCase.getTemplates();
        final TestCaseTemplate template = templates.get(templates.size() - 1);
        template.addUnexpectedTrashArgument(rt);

        processingState
                .push(ParsingState.TEST_CASE_SETTING_TEST_TEMPLATE_KEYWORD_UNWANTED_ARGUMENTS);

        return rt;
    }


    @Override
    public boolean checkIfCanBeMapped(final RobotFileOutput robotFileOutput,
            final RobotLine currentLine, final RobotToken rt, final String text,
            final Stack<ParsingState> processingState) {
        boolean result;
        if (!processingState.isEmpty()) {
            final ParsingState currentState = stateHelper
                    .getCurrentStatus(processingState);
            if (currentState == ParsingState.TEST_CASE_SETTING_TEST_TEMPLATE_KEYWORD
                    || currentState == ParsingState.TEST_CASE_SETTING_TEST_TEMPLATE_KEYWORD_UNWANTED_ARGUMENTS) {
                result = true;
            } else {
                result = false;
            }
        } else {
            result = false;
        }
        return result;
    }

}

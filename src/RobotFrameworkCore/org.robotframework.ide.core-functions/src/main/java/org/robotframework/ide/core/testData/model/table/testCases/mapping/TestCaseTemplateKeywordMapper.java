/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see licence.txt file for details.
 */
package org.robotframework.ide.core.testData.model.table.testCases.mapping;

import java.util.List;
import java.util.Stack;

import org.robotframework.ide.core.testData.model.FilePosition;
import org.robotframework.ide.core.testData.model.RobotFileOutput;
import org.robotframework.ide.core.testData.model.table.mapping.ElementsUtility;
import org.robotframework.ide.core.testData.model.table.mapping.IParsingMapper;
import org.robotframework.ide.core.testData.model.table.testCases.TestCase;
import org.robotframework.ide.core.testData.model.table.testCases.TestCaseTemplate;
import org.robotframework.ide.core.testData.text.read.IRobotTokenType;
import org.robotframework.ide.core.testData.text.read.ParsingState;
import org.robotframework.ide.core.testData.text.read.RobotLine;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotTokenType;

import com.google.common.annotations.VisibleForTesting;


public class TestCaseTemplateKeywordMapper implements IParsingMapper {

    private final ElementsUtility utility;


    public TestCaseTemplateKeywordMapper() {
        this.utility = new ElementsUtility();
    }


    @Override
    public RobotToken map(RobotLine currentLine,
            Stack<ParsingState> processingState,
            RobotFileOutput robotFileOutput, RobotToken rt, FilePosition fp,
            String text) {
        List<IRobotTokenType> types = rt.getTypes();
        types.remove(RobotTokenType.UNKNOWN);
        types.add(0, RobotTokenType.TEST_CASE_SETTING_TEMPLATE_KEYWORD_NAME);

        rt.setRaw(new StringBuilder(text));
        rt.setText(new StringBuilder(text));
        List<TestCase> testCases = robotFileOutput.getFileModel()
                .getTestCaseTable().getTestCases();
        TestCase testCase = testCases.get(testCases.size() - 1);
        List<TestCaseTemplate> templates = testCase.getTemplates();

        TestCaseTemplate testTemplate = templates.get(templates.size() - 1);
        testTemplate.setKeywordName(rt);

        processingState
                .push(ParsingState.TEST_CASE_SETTING_TEST_TEMPLATE_KEYWORD);

        return rt;
    }


    @Override
    public boolean checkIfCanBeMapped(RobotFileOutput robotFileOutput,
            RobotLine currentLine, RobotToken rt, String text,
            Stack<ParsingState> processingState) {
        boolean result = false;
        ParsingState state = utility.getCurrentStatus(processingState);
        if (state == ParsingState.TEST_CASE_SETTING_TEST_TEMPLATE) {
            List<TestCase> tests = robotFileOutput.getFileModel()
                    .getTestCaseTable().getTestCases();
            List<TestCaseTemplate> templates = tests.get(tests.size() - 1)
                    .getTemplates();
            result = !checkIfHasAlreadyKeywordName(templates);
        }

        return result;
    }


    @VisibleForTesting
    protected boolean checkIfHasAlreadyKeywordName(
            List<TestCaseTemplate> testCaseTemplates) {
        boolean result = false;
        for (TestCaseTemplate setting : testCaseTemplates) {
            result = (setting.getKeywordName() != null);
            result = result || !setting.getUnexpectedTrashArguments().isEmpty();
            if (result) {
                break;
            }
        }

        return result;
    }
}

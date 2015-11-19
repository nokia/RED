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
import org.robotframework.ide.core.testData.model.table.TestCaseTable;
import org.robotframework.ide.core.testData.model.table.mapping.IParsingMapper;
import org.robotframework.ide.core.testData.model.table.mapping.ParsingStateHelper;
import org.robotframework.ide.core.testData.model.table.testCases.TestCase;
import org.robotframework.ide.core.testData.model.table.testCases.TestCaseTags;
import org.robotframework.ide.core.testData.text.read.IRobotTokenType;
import org.robotframework.ide.core.testData.text.read.ParsingState;
import org.robotframework.ide.core.testData.text.read.RobotLine;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotTokenType;


public class TestCaseTagsTagNameMapper implements IParsingMapper {

    private final ParsingStateHelper stateHelper;


    public TestCaseTagsTagNameMapper() {
        this.stateHelper = new ParsingStateHelper();
    }


    @Override
    public RobotToken map(final RobotLine currentLine,
            final Stack<ParsingState> processingState,
            final RobotFileOutput robotFileOutput, final RobotToken rt, final FilePosition fp,
            final String text) {
        final List<IRobotTokenType> types = rt.getTypes();
        types.add(0, RobotTokenType.TEST_CASE_SETTING_TAGS);
        rt.setText(text);
        rt.setRaw(text);

        final TestCaseTable testCaseTable = robotFileOutput.getFileModel()
                .getTestCaseTable();
        final List<TestCase> testCases = testCaseTable.getTestCases();
        final TestCase testCase = testCases.get(testCases.size() - 1);
        final List<TestCaseTags> tags = testCase.getTags();
        final TestCaseTags testCaseTags = tags.get(tags.size() - 1);
        testCaseTags.addTag(rt);

        processingState.push(ParsingState.TEST_CASE_SETTING_TAGS_TAG_NAME);

        return rt;
    }


    @Override
    public boolean checkIfCanBeMapped(final RobotFileOutput robotFileOutput,
            final RobotLine currentLine, final RobotToken rt, final String text,
            final Stack<ParsingState> processingState) {
        boolean result = false;
        final ParsingState state = stateHelper.getCurrentStatus(processingState);
        result = (state == ParsingState.TEST_CASE_SETTING_TAGS || state == ParsingState.TEST_CASE_SETTING_TAGS_TAG_NAME);

        return result;
    }

}

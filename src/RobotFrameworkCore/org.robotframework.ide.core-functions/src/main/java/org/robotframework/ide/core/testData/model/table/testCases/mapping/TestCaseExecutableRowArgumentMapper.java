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
import org.robotframework.ide.core.testData.model.table.RobotExecutableRow;
import org.robotframework.ide.core.testData.model.table.mapping.IParsingMapper;
import org.robotframework.ide.core.testData.model.table.mapping.ParsingStateHelper;
import org.robotframework.ide.core.testData.model.table.testCases.TestCase;
import org.robotframework.ide.core.testData.text.read.IRobotTokenType;
import org.robotframework.ide.core.testData.text.read.ParsingState;
import org.robotframework.ide.core.testData.text.read.RobotLine;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotTokenType;
import org.robotframework.ide.core.testData.text.read.recognizer.executables.RobotSpecialTokens;


public class TestCaseExecutableRowArgumentMapper implements IParsingMapper {

    private final ParsingStateHelper stateHelper;
    private final TestCaseFinder testCaseFinder;
    private final RobotSpecialTokens specialTokensRecognizer;


    public TestCaseExecutableRowArgumentMapper() {
        this.stateHelper = new ParsingStateHelper();
        this.testCaseFinder = new TestCaseFinder();
        this.specialTokensRecognizer = new RobotSpecialTokens();
    }


    @Override
    public RobotToken map(RobotLine currentLine,
            Stack<ParsingState> processingState,
            RobotFileOutput robotFileOutput, RobotToken rt, FilePosition fp,
            String text) {
        TestCase testCase = testCaseFinder.findOrCreateNearestTestCase(
                currentLine, processingState, robotFileOutput, rt, fp);
        List<IRobotTokenType> types = rt.getTypes();
        types.add(0, RobotTokenType.TEST_CASE_ACTION_ARGUMENT);

        List<RobotToken> specialTokens = specialTokensRecognizer.recognize(fp,
                text);
        for (RobotToken token : specialTokens) {
            types.addAll(token.getTypes());
        }

        List<RobotExecutableRow<TestCase>> testExecutionRows = testCase
                .getTestExecutionRows();
        RobotExecutableRow<TestCase> robotExecutableRow = testExecutionRows
                .get(testExecutionRows.size() - 1);
        robotExecutableRow.addArgument(rt);

        processingState.push(ParsingState.TEST_CASE_INSIDE_ACTION_ARGUMENT);
        return rt;
    }


    @Override
    public boolean checkIfCanBeMapped(RobotFileOutput robotFileOutput,
            RobotLine currentLine, RobotToken rt, String text,
            Stack<ParsingState> processingState) {
        boolean result = false;
        ParsingState state = stateHelper.getCurrentStatus(processingState);
        result = (state == ParsingState.TEST_CASE_INSIDE_ACTION || state == ParsingState.TEST_CASE_INSIDE_ACTION_ARGUMENT);

        return result;
    }
}

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
import org.robotframework.ide.core.testData.model.table.mapping.ElementPositionResolver;
import org.robotframework.ide.core.testData.model.table.mapping.ElementPositionResolver.PositionExpected;
import org.robotframework.ide.core.testData.model.table.mapping.IParsingMapper;
import org.robotframework.ide.core.testData.model.table.mapping.ParsingStateHelper;
import org.robotframework.ide.core.testData.model.table.testCases.TestCase;
import org.robotframework.ide.core.testData.text.read.IRobotTokenType;
import org.robotframework.ide.core.testData.text.read.ParsingState;
import org.robotframework.ide.core.testData.text.read.RobotLine;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotTokenType;
import org.robotframework.ide.core.testData.text.read.recognizer.executables.RobotSpecialTokens;


public class TestCaseExecutableRowActionMapper implements IParsingMapper {

    private final ElementPositionResolver posResolver;
    private final ParsingStateHelper stateHelper;
    private final TestCaseFinder testCaseFinder;
    private final RobotSpecialTokens specialTokensRecognizer;


    public TestCaseExecutableRowActionMapper() {
        this.posResolver = new ElementPositionResolver();
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
        types.add(0, RobotTokenType.TEST_CASE_ACTION_NAME);
        types.remove(RobotTokenType.UNKNOWN);

        List<RobotToken> specialTokens = specialTokensRecognizer.recognize(fp,
                text);
        for (RobotToken token : specialTokens) {
            types.addAll(token.getTypes());
        }

        RobotExecutableRow<TestCase> row = new RobotExecutableRow<TestCase>();
        row.setAction(rt);
        testCase.addTestExecutionRow(row);

        processingState.push(ParsingState.TEST_CASE_INSIDE_ACTION);
        return rt;
    }


    @Override
    public boolean checkIfCanBeMapped(RobotFileOutput robotFileOutput,
            RobotLine currentLine, RobotToken rt, String text,
            Stack<ParsingState> processingState) {
        ParsingState state = stateHelper.getCurrentStatus(processingState);
        return (state == ParsingState.TEST_CASE_TABLE_INSIDE || state == ParsingState.TEST_CASE_DECLARATION)
                && posResolver.isCorrectPosition(
                        PositionExpected.TEST_CASE_EXEC_ROW_ACTION_NAME,
                        robotFileOutput.getFileModel(), currentLine, rt);
    }
}

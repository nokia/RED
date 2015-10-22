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
import org.robotframework.ide.core.testData.model.table.mapping.ElementsUtility;
import org.robotframework.ide.core.testData.model.table.mapping.IParsingMapper;
import org.robotframework.ide.core.testData.model.table.mapping.ParsingStateHelper;
import org.robotframework.ide.core.testData.model.table.testCases.TestCase;
import org.robotframework.ide.core.testData.text.read.IRobotTokenType;
import org.robotframework.ide.core.testData.text.read.ParsingState;
import org.robotframework.ide.core.testData.text.read.RobotLine;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotTokenType;


public class TestCaseExecutableRowActionMapper implements IParsingMapper {

    private final ElementsUtility utility;
    private final ParsingStateHelper stateHelper;
    private final TestCaseFinder testCaseFinder;


    public TestCaseExecutableRowActionMapper() {
        this.utility = new ElementsUtility();
        this.stateHelper = new ParsingStateHelper();
        this.testCaseFinder = new TestCaseFinder();
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
                && !utility.isTheFirstColumn(currentLine, rt);
    }
}

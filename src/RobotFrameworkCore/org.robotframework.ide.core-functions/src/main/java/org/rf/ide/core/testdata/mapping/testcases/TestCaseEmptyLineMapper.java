/*
 * Copyright 2017 Nokia Solutions and Networks
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
import org.rf.ide.core.testdata.model.table.RobotEmptyRow;
import org.rf.ide.core.testdata.model.table.testcases.TestCase;
import org.rf.ide.core.testdata.text.read.IRobotTokenType;
import org.rf.ide.core.testdata.text.read.ParsingState;
import org.rf.ide.core.testdata.text.read.RobotLine;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

public class TestCaseEmptyLineMapper implements IParsingMapper {

    private final ParsingStateHelper stateHelper;

    private final TestCaseFinder testCaseFinder;

    public TestCaseEmptyLineMapper() {
        this.stateHelper = new ParsingStateHelper();
        this.testCaseFinder = new TestCaseFinder();
    }

    @Override
    public RobotToken map(final RobotLine currentLine, final Stack<ParsingState> processingState,
            final RobotFileOutput robotFileOutput, final RobotToken rt, final FilePosition fp, final String text) {
        final TestCase testCase = testCaseFinder.findOrCreateNearestTestCase(currentLine, robotFileOutput);
        final List<IRobotTokenType> types = rt.getTypes();
        types.add(0, RobotTokenType.TEST_CASE_EMPTY_CELL);
        types.remove(RobotTokenType.UNKNOWN);

        final RobotEmptyRow<TestCase> emptyLine = new RobotEmptyRow<>();
        emptyLine.setEmptyToken(rt);
        testCase.addElement(emptyLine);

        processingState.push(ParsingState.TEST_CASE_EMPTY_LINE);
        return rt;
    }

    @Override
    public boolean checkIfCanBeMapped(final RobotFileOutput robotFileOutput, final RobotLine currentLine,
            final RobotToken rt, final String text, final Stack<ParsingState> processingState) {
        final ParsingState state = stateHelper.getCurrentStatus(processingState);
        return state == ParsingState.TEST_CASE_DECLARATION
                && RobotEmptyRow.isEmpty(text)
                && currentLine.getLineElements().isEmpty();
    }
}

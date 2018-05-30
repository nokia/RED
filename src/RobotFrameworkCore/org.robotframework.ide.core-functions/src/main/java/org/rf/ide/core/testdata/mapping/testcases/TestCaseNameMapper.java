/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.mapping.testcases;

import java.util.List;
import java.util.Stack;

import org.rf.ide.core.testdata.mapping.table.ElementPositionResolver;
import org.rf.ide.core.testdata.mapping.table.ElementPositionResolver.PositionExpected;
import org.rf.ide.core.testdata.mapping.table.IParsingMapper;
import org.rf.ide.core.testdata.model.FilePosition;
import org.rf.ide.core.testdata.model.RobotFileOutput;
import org.rf.ide.core.testdata.model.table.TestCaseTable;
import org.rf.ide.core.testdata.model.table.testcases.TestCase;
import org.rf.ide.core.testdata.text.read.IRobotTokenType;
import org.rf.ide.core.testdata.text.read.ParsingState;
import org.rf.ide.core.testdata.text.read.RobotLine;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

import com.google.common.annotations.VisibleForTesting;

public class TestCaseNameMapper implements IParsingMapper {

    private final ElementPositionResolver positionResolver;

    public TestCaseNameMapper() {
        this.positionResolver = new ElementPositionResolver();
    }

    @Override
    public RobotToken map(final RobotLine currentLine,
            final Stack<ParsingState> processingState,
            final RobotFileOutput robotFileOutput, final RobotToken rt, final FilePosition fp,
            final String text) {
        final List<IRobotTokenType> types = rt.getTypes();
        types.remove(RobotTokenType.UNKNOWN);
        types.add(0, RobotTokenType.TEST_CASE_NAME);
        rt.setText(text);

        final TestCaseTable testCaseTable = robotFileOutput.getFileModel()
                .getTestCaseTable();
        final TestCase testCase = new TestCase(rt);
        testCaseTable.addTest(testCase);
        processingState.push(ParsingState.TEST_CASE_DECLARATION);

        return rt;
    }

    @Override
    public boolean checkIfCanBeMapped(final RobotFileOutput robotFileOutput,
            final RobotLine currentLine, final RobotToken rt, final String text,
            final Stack<ParsingState> processingState) {
        boolean result = false;
        if (positionResolver.isCorrectPosition(PositionExpected.TEST_CASE_NAME,
                robotFileOutput.getFileModel(), currentLine, rt)) {
            if (isIncludedInTestCaseTable(currentLine, processingState)) {
                boolean wasUpdated = false;
                final String testCaseName = rt.getText().toString();
                if (testCaseName != null) {
                    result = !testCaseName.trim().startsWith(
                            RobotTokenType.START_HASH_COMMENT
                                    .getRepresentation().get(0));
                    wasUpdated = true;

                }

                if (!wasUpdated) {
                    result = true;
                }
            } else {
                // FIXME: it is in wrong place means no keyword table
                // declaration
            }
        } else {
            // FIXME: wrong place | | Library or | Library | Library X |
            // case.
        }

        return result;
    }

    @VisibleForTesting
    protected boolean isIncludedInTestCaseTable(final RobotLine line,
            final Stack<ParsingState> processingState) {

        return processingState.contains(ParsingState.TEST_CASE_TABLE_INSIDE);
    }
}

/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.mapping.testcases;

import java.util.List;
import java.util.Stack;

import org.rf.ide.core.environment.RobotVersion;
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

public class TestCaseNameMapper implements IParsingMapper {

    private final ElementPositionResolver positionResolver = new ElementPositionResolver();

    @Override
    public boolean checkIfCanBeMapped(final RobotFileOutput robotFileOutput,
            final RobotLine currentLine, final RobotToken rt, final String text,
            final Stack<ParsingState> processingState) {

        return positionResolver.isCorrectPosition(PositionExpected.TEST_CASE_NAME, currentLine, rt)
                && processingState.contains(ParsingState.TEST_CASE_TABLE_INSIDE)
                && (rt.getText() == null || !rt.getText().trim()
                        .startsWith(RobotTokenType.START_HASH_COMMENT.getRepresentation().get(0)));
    }

    @Override
    public RobotToken map(final RobotLine currentLine, final Stack<ParsingState> processingState,
            final RobotFileOutput robotFileOutput, final RobotToken rt, final FilePosition fp, final String text) {

        setTokenTypes(robotFileOutput.getRobotVersion(), rt);
        rt.setText(text);

        final TestCaseTable testCaseTable = robotFileOutput.getFileModel().getTestCaseTable();
        testCaseTable.addTest(new TestCase(rt));

        processingState.push(ParsingState.TEST_CASE_DECLARATION);
        return rt;
    }

    private void setTokenTypes(final RobotVersion robotVersion, final RobotToken rt) {
        if (robotVersion.isOlderThan(new RobotVersion(3, 2))) {
            // until 3.2 a name of a test is plain text, so we're clearing other types (especially
            // variables)
            rt.setType(RobotTokenType.TEST_CASE_NAME);
        } else {
            // from 3.2 there can be variables in test name
            final List<IRobotTokenType> types = rt.getTypes();
            types.remove(RobotTokenType.UNKNOWN);
            types.add(0, RobotTokenType.TEST_CASE_NAME);
        }
    }
}

/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.mapping.testcases;

import java.util.List;
import java.util.Stack;

import org.rf.ide.core.environment.RobotVersion;
import org.rf.ide.core.testdata.mapping.HashCommentMapper;
import org.rf.ide.core.testdata.mapping.table.IParsingMapper;
import org.rf.ide.core.testdata.mapping.table.ParsingStateHelper;
import org.rf.ide.core.testdata.model.FilePosition;
import org.rf.ide.core.testdata.model.RobotFileOutput;
import org.rf.ide.core.testdata.model.table.RobotExecutableRow;
import org.rf.ide.core.testdata.model.table.testcases.TestCase;
import org.rf.ide.core.testdata.text.read.IRobotTokenType;
import org.rf.ide.core.testdata.text.read.ParsingState;
import org.rf.ide.core.testdata.text.read.RobotLine;
import org.rf.ide.core.testdata.text.read.recognizer.RobotSpecialTokens;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

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
    public boolean isApplicableFor(final RobotVersion robotVersion) {
        specialTokensRecognizer.initializeFor(robotVersion);
        return IParsingMapper.super.isApplicableFor(robotVersion);
    }

    @Override
    public boolean checkIfCanBeMapped(final RobotFileOutput robotFileOutput, final RobotLine currentLine,
            final RobotToken rt, final String text, final Stack<ParsingState> processingState) {

        final ParsingState state = stateHelper.getCurrentState(processingState);
        return state == ParsingState.TEST_CASE_INSIDE_ACTION || state == ParsingState.TEST_CASE_INSIDE_ACTION_ARGUMENT;
    }

    @Override
    public RobotToken map(final RobotLine currentLine, final Stack<ParsingState> processingState, final RobotFileOutput robotFileOutput,
            final RobotToken rt, final FilePosition fp, final String text) {
        final TestCase testCase = testCaseFinder.findOrCreateNearestTestCase(currentLine, robotFileOutput);
        final List<IRobotTokenType> types = rt.getTypes();
        types.remove(RobotTokenType.TEST_CASE_ACTION_ARGUMENT);
        types.remove(RobotTokenType.UNKNOWN);
        types.add(0, RobotTokenType.TEST_CASE_ACTION_ARGUMENT);

        final List<RobotToken> specialTokens = specialTokensRecognizer.recognize(fp, text);
        for (final RobotToken token : specialTokens) {
            types.addAll(token.getTypes());
        }

        final List<RobotExecutableRow<TestCase>> testExecutionRows = testCase.getExecutionContext();
        final RobotExecutableRow<TestCase> robotExecutableRow = testExecutionRows.get(testExecutionRows.size() - 1);

        boolean commentContinue = false;
        if (!robotExecutableRow.getComment().isEmpty()) {
            final int lineNumber = robotExecutableRow.getComment()
                    .get(robotExecutableRow.getComment().size() - 1)
                    .getLineNumber();
            commentContinue = (lineNumber == rt.getLineNumber());
        }

        if (text.startsWith("#") || commentContinue
                || HashCommentMapper.isTsvComment(text, robotFileOutput.getFileFormat())) {
            types.remove(RobotTokenType.TEST_CASE_NAME);
            types.remove(RobotTokenType.TEST_CASE_ACTION_ARGUMENT);
            types.add(0, RobotTokenType.START_HASH_COMMENT);
            robotExecutableRow.addCommentPart(rt);
        } else {
            if (robotExecutableRow.getAction().getFilePosition().isNotSet()) {
                types.remove(RobotTokenType.TEST_CASE_ACTION_ARGUMENT);
                types.add(0, RobotTokenType.TEST_CASE_ACTION_NAME);
                robotExecutableRow.setAction(rt);
            } else {
                types.remove(RobotTokenType.TEST_CASE_ACTION_ARGUMENT);
                types.add(0, RobotTokenType.TEST_CASE_ACTION_ARGUMENT);
                robotExecutableRow.addArgument(rt);
            }
        }

        processingState.push(ParsingState.TEST_CASE_INSIDE_ACTION_ARGUMENT);
        return rt;
    }
}

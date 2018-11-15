/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.mapping.tasks;

import java.util.List;
import java.util.Stack;

import org.rf.ide.core.testdata.mapping.table.IParsingMapper;
import org.rf.ide.core.testdata.mapping.table.ParsingStateHelper;
import org.rf.ide.core.testdata.model.FilePosition;
import org.rf.ide.core.testdata.model.RobotFileOutput;
import org.rf.ide.core.testdata.model.RobotVersion;
import org.rf.ide.core.testdata.model.table.RobotExecutableRow;
import org.rf.ide.core.testdata.model.table.tasks.Task;
import org.rf.ide.core.testdata.text.read.IRobotTokenType;
import org.rf.ide.core.testdata.text.read.ParsingState;
import org.rf.ide.core.testdata.text.read.RobotLine;
import org.rf.ide.core.testdata.text.read.recognizer.RobotSpecialTokens;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

public class TaskExecutableRowArgumentMapper implements IParsingMapper {

    private final ParsingStateHelper stateHelper = new ParsingStateHelper();

    private final TaskFinder testCaseFinder = new TaskFinder();

    private final RobotSpecialTokens specialTokensRecognizer = new RobotSpecialTokens();

    @Override
    public final boolean isApplicableFor(final RobotVersion robotVersion) {
        specialTokensRecognizer.initializeFor(robotVersion);
        return robotVersion.isNewerOrEqualTo(new RobotVersion(3, 1));
    }

    @Override
    public boolean checkIfCanBeMapped(final RobotFileOutput robotFileOutput, final RobotLine currentLine,
            final RobotToken rt, final String text, final Stack<ParsingState> processingState) {

        final ParsingState state = stateHelper.getCurrentState(processingState);
        return state == ParsingState.TASK_INSIDE_ACTION || state == ParsingState.TASK_INSIDE_ACTION_ARGUMENT;
    }

    @Override
    public RobotToken map(final RobotLine currentLine, final Stack<ParsingState> processingState, final RobotFileOutput robotFileOutput,
            final RobotToken rt, final FilePosition fp, final String text) {

        final Task task = testCaseFinder.findOrCreateNearestTask(currentLine, robotFileOutput);
        final List<IRobotTokenType> types = rt.getTypes();
        types.remove(RobotTokenType.TASK_ACTION_ARGUMENT);
        types.remove(RobotTokenType.UNKNOWN);
        types.add(0, RobotTokenType.TASK_ACTION_ARGUMENT);

        final List<RobotToken> specialTokens = specialTokensRecognizer.recognize(fp, text);
        for (final RobotToken token : specialTokens) {
            types.addAll(token.getTypes());
        }

        final List<RobotExecutableRow<Task>> taskExecutionRows = task.getExecutionContext();
        final RobotExecutableRow<Task> robotExecutableRow = taskExecutionRows.get(taskExecutionRows.size() - 1);

        boolean commentContinue = false;
        if (!robotExecutableRow.getComment().isEmpty()) {
            final int lineNumber = robotExecutableRow.getComment()
                    .get(robotExecutableRow.getComment().size() - 1)
                    .getLineNumber();
            commentContinue = (lineNumber == rt.getLineNumber());
        }

        if (text.startsWith("#") || commentContinue
                || RobotExecutableRow.isTsvComment(text, robotFileOutput.getFileFormat())) {
            types.remove(RobotTokenType.TASK_NAME);
            types.remove(RobotTokenType.TASK_ACTION_ARGUMENT);
            types.add(0, RobotTokenType.START_HASH_COMMENT);
            robotExecutableRow.addCommentPart(rt);
        } else {
            if (robotExecutableRow.getAction().getFilePosition().isNotSet()) {
                types.remove(RobotTokenType.TASK_ACTION_ARGUMENT);
                types.add(0, RobotTokenType.TASK_ACTION_NAME);
                robotExecutableRow.setAction(rt);
            } else {
                types.remove(RobotTokenType.TASK_ACTION_ARGUMENT);
                types.add(0, RobotTokenType.TASK_ACTION_ARGUMENT);
                robotExecutableRow.addArgument(rt);
            }
        }

        processingState.push(ParsingState.TASK_INSIDE_ACTION_ARGUMENT);
        return rt;
    }
}

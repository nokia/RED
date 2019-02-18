/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.mapping.tasks;

import java.util.List;
import java.util.Stack;

import org.rf.ide.core.environment.RobotVersion;
import org.rf.ide.core.testdata.mapping.HashCommentMapper;
import org.rf.ide.core.testdata.mapping.table.ElementPositionResolver;
import org.rf.ide.core.testdata.mapping.table.ElementPositionResolver.PositionExpected;
import org.rf.ide.core.testdata.mapping.table.IParsingMapper;
import org.rf.ide.core.testdata.mapping.table.ParsingStateHelper;
import org.rf.ide.core.testdata.model.FilePosition;
import org.rf.ide.core.testdata.model.RobotFileOutput;
import org.rf.ide.core.testdata.model.table.RobotEmptyRow;
import org.rf.ide.core.testdata.model.table.RobotExecutableRow;
import org.rf.ide.core.testdata.model.table.tasks.Task;
import org.rf.ide.core.testdata.text.read.IRobotTokenType;
import org.rf.ide.core.testdata.text.read.ParsingState;
import org.rf.ide.core.testdata.text.read.RobotLine;
import org.rf.ide.core.testdata.text.read.recognizer.RobotSpecialTokens;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

public class TaskExecutableRowActionMapper implements IParsingMapper {

    private final ElementPositionResolver posResolver = new ElementPositionResolver();

    private final ParsingStateHelper stateHelper = new ParsingStateHelper();

    private final TaskFinder finder = new TaskFinder();

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
        if (state == ParsingState.TASKS_TABLE_INSIDE || state == ParsingState.TASK_DECLARATION) {
            if (!RobotEmptyRow.isEmpty(text) || !currentLine.getLineElements().isEmpty()) {
                if (posResolver.isCorrectPosition(PositionExpected.TASK_EXEC_ROW_ACTION_NAME, currentLine, rt)) {
                    return true;
                } else if (posResolver.isCorrectPosition(PositionExpected.TASK_NAME, currentLine, rt)) {
                    if (text.trim().startsWith(RobotTokenType.START_HASH_COMMENT.getRepresentation().get(0))) {
                        if (!rt.getTypes().contains(RobotTokenType.START_HASH_COMMENT)) {
                            rt.getTypes().add(RobotTokenType.START_HASH_COMMENT);
                        }
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public RobotToken map(final RobotLine currentLine, final Stack<ParsingState> processingState, final RobotFileOutput robotFileOutput,
            final RobotToken rt, final FilePosition fp, final String text) {

        final Task task = finder.findOrCreateNearestTask(currentLine, robotFileOutput);
        final List<IRobotTokenType> types = rt.getTypes();
        types.add(0, RobotTokenType.TASK_ACTION_NAME);
        types.remove(RobotTokenType.UNKNOWN);

        final List<RobotToken> specialTokens = specialTokensRecognizer.recognize(fp, text);
        for (final RobotToken token : specialTokens) {
            types.addAll(token.getTypes());
        }

        final RobotExecutableRow<Task> row = new RobotExecutableRow<>();
        if (text.startsWith(" #") || text.startsWith("#")
                || HashCommentMapper.isTsvComment(text, robotFileOutput.getFileFormat())) {
            types.remove(RobotTokenType.TASK_ACTION_NAME);
            types.remove(RobotTokenType.TASK_ACTION_ARGUMENT);
            types.add(RobotTokenType.START_HASH_COMMENT);
            row.addCommentPart(rt);
        } else {
            row.setAction(rt);
        }
        task.addElement(row);

        processingState.push(ParsingState.TASK_INSIDE_ACTION);
        return rt;
    }
}

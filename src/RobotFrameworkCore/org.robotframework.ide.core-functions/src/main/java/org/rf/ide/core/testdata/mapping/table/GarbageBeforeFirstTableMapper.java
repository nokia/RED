/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.mapping.table;

import java.util.List;
import java.util.Stack;

import org.rf.ide.core.testdata.model.FilePosition;
import org.rf.ide.core.testdata.model.RobotFileOutput;
import org.rf.ide.core.testdata.text.read.IRobotLineElement;
import org.rf.ide.core.testdata.text.read.IRobotTokenType;
import org.rf.ide.core.testdata.text.read.ParsingState;
import org.rf.ide.core.testdata.text.read.RobotLine;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

public class GarbageBeforeFirstTableMapper implements IParsingMapper {

    @Override
    public RobotToken map(final RobotLine currentLine, final Stack<ParsingState> processingState,
            final RobotFileOutput robotFileOutput, final RobotToken rt, final FilePosition fp, final String text) {
        // nothing to do
        rt.setText(text);
        final List<IRobotTokenType> types = rt.getTypes();
        if (containsInLineOnlyComment(currentLine) || isPreviousLineContinueOrStartComment(currentLine)) {
            types.add(0, RobotTokenType.COMMENT_CONTINUE);
        }
        if (!types.contains(RobotTokenType.START_HASH_COMMENT) && !types.contains(RobotTokenType.COMMENT_CONTINUE)) {
            types.add(0, RobotTokenType.UNKNOWN);
        }
        return rt;
    }

    private boolean isPreviousLineContinueOrStartComment(final RobotLine currentLine) {
        final List<IRobotLineElement> lineElements = currentLine.getLineElements();
        int size = lineElements.size();
        if (size > 0) {
            for (int i = size - 1; i >= 0; i--) {
                final IRobotLineElement elem = lineElements.get(i);
                if (elem.getClass() == RobotToken.class) {
                    return (elem.getTypes().contains(RobotTokenType.START_HASH_COMMENT)
                            || elem.getTypes().contains(RobotTokenType.COMMENT_CONTINUE));
                }
            }
        }

        return false;
    }

    @Override
    public boolean checkIfCanBeMapped(final RobotFileOutput robotFileOutput, final RobotLine currentLine,
            final RobotToken rt, final String text, final Stack<ParsingState> processingState) {
        boolean result = false;
        if (rt.getTypes().contains(RobotTokenType.START_HASH_COMMENT) || containsInLineOnlyComment(currentLine)
                || isCurrentLineTrash(currentLine)) {
            if (processingState.isEmpty()) {
                result = true;
            } else {
                final ParsingState state = processingState.peek();
                result = (state == ParsingState.UNKNOWN || state == ParsingState.TRASH);
            }
        }

        return result;
    }

    private boolean isCurrentLineTrash(final RobotLine currentLine) {
        for (final IRobotLineElement e : currentLine.getLineElements()) {
            if (e.getClass() == RobotToken.class) {
                if (e.getTypes().contains(RobotTokenType.UNKNOWN)) {
                    return true;
                } else if (e.getTypes().contains(RobotTokenType.ASSIGNMENT)
                        || e.getTypes().contains(RobotTokenType.PRETTY_ALIGN_SPACE)) {
                    continue;
                } else {
                    return false;
                }
            }
        }

        return false;
    }

    private boolean containsInLineOnlyComment(final RobotLine currentLine) {
        for (final IRobotLineElement e : currentLine.getLineElements()) {
            if (e.getClass() == RobotToken.class) {
                if (e.getTypes().contains(RobotTokenType.START_HASH_COMMENT)) {
                    return true;
                } else if (e.getTypes().contains(RobotTokenType.ASSIGNMENT)
                        || e.getTypes().contains(RobotTokenType.PRETTY_ALIGN_SPACE)) {
                    continue;
                } else {
                    return false;
                }
            }
        }

        return false;
    }
}

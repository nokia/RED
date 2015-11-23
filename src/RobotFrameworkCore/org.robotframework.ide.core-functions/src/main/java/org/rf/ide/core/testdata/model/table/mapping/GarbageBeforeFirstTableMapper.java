/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table.mapping;

import java.util.List;
import java.util.Stack;

import org.rf.ide.core.testdata.model.FilePosition;
import org.rf.ide.core.testdata.model.RobotFileOutput;
import org.rf.ide.core.testdata.text.read.IRobotTokenType;
import org.rf.ide.core.testdata.text.read.ParsingState;
import org.rf.ide.core.testdata.text.read.RobotLine;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;


public class GarbageBeforeFirstTableMapper implements IParsingMapper {

    @Override
    public RobotToken map(final RobotLine currentLine,
            final Stack<ParsingState> processingState,
            final RobotFileOutput robotFileOutput, final RobotToken rt, final FilePosition fp,
            final String text) {
        // nothing to do
        rt.setText(text);
        final List<IRobotTokenType> types = rt.getTypes();
        if (!types.contains(RobotTokenType.START_HASH_COMMENT)
                && !types.contains(RobotTokenType.COMMENT_CONTINUE)) {
            rt.setType(RobotTokenType.UNKNOWN);
        }
        return rt;
    }


    @Override
    public boolean checkIfCanBeMapped(final RobotFileOutput robotFileOutput,
            final RobotLine currentLine, final RobotToken rt, final String text,
            final Stack<ParsingState> processingState) {
        boolean result = false;
        if (rt.getTypes().contains(RobotTokenType.START_HASH_COMMENT)) {
            if (processingState.isEmpty()) {
                result = true;
            } else {
                final ParsingState state = processingState.peek();
                result = (state == ParsingState.UNKNOWN || state == ParsingState.TRASH);
            }
        }

        return result;
    }
}

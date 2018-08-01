/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.mapping.keywords;

import java.util.List;
import java.util.Stack;

import org.rf.ide.core.testdata.mapping.table.IParsingMapper;
import org.rf.ide.core.testdata.mapping.table.ParsingStateHelper;
import org.rf.ide.core.testdata.model.FilePosition;
import org.rf.ide.core.testdata.model.RobotFileOutput;
import org.rf.ide.core.testdata.model.table.RobotEmptyRow;
import org.rf.ide.core.testdata.model.table.keywords.UserKeyword;
import org.rf.ide.core.testdata.text.read.IRobotTokenType;
import org.rf.ide.core.testdata.text.read.ParsingState;
import org.rf.ide.core.testdata.text.read.RobotLine;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

public class KeywordEmptyLineMapper implements IParsingMapper {

    private final ParsingStateHelper stateHelper = new ParsingStateHelper();

    private final KeywordFinder keywordFinder = new KeywordFinder();

    @Override
    public boolean checkIfCanBeMapped(final RobotFileOutput robotFileOutput, final RobotLine currentLine,
            final RobotToken rt, final String text, final Stack<ParsingState> processingState) {

        final ParsingState state = stateHelper.getCurrentStatus(processingState);
        return state == ParsingState.KEYWORD_DECLARATION && RobotEmptyRow.isEmpty(text)
                && currentLine.getLineElements().isEmpty();
    }

    @Override
    public RobotToken map(final RobotLine currentLine, final Stack<ParsingState> processingState,
            final RobotFileOutput robotFileOutput, final RobotToken rt, final FilePosition fp, final String text) {

        final UserKeyword keyword = keywordFinder.findOrCreateNearestKeyword(currentLine, robotFileOutput);
        final List<IRobotTokenType> types = rt.getTypes();
        types.add(0, RobotTokenType.KEYWORD_EMPTY_CELL);
        types.remove(RobotTokenType.UNKNOWN);

        final RobotEmptyRow<UserKeyword> emptyLine = new RobotEmptyRow<>();
        emptyLine.setEmptyToken(rt);
        keyword.addElement(emptyLine);

        processingState.push(ParsingState.KEYWORD_EMPTY_LINE);
        return rt;
    }
}

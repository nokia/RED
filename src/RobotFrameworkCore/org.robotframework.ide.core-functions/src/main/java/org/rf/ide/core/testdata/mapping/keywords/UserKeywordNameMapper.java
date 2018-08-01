/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.mapping.keywords;

import java.util.List;
import java.util.Stack;

import org.rf.ide.core.testdata.mapping.table.ElementPositionResolver;
import org.rf.ide.core.testdata.mapping.table.ElementPositionResolver.PositionExpected;
import org.rf.ide.core.testdata.mapping.table.IParsingMapper;
import org.rf.ide.core.testdata.model.FilePosition;
import org.rf.ide.core.testdata.model.RobotFileOutput;
import org.rf.ide.core.testdata.model.table.KeywordTable;
import org.rf.ide.core.testdata.model.table.keywords.UserKeyword;
import org.rf.ide.core.testdata.text.read.IRobotTokenType;
import org.rf.ide.core.testdata.text.read.ParsingState;
import org.rf.ide.core.testdata.text.read.RobotLine;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

public class UserKeywordNameMapper implements IParsingMapper {

    private final ElementPositionResolver positionResolver = new ElementPositionResolver();


    @Override
    public boolean checkIfCanBeMapped(final RobotFileOutput robotFileOutput,
            final RobotLine currentLine, final RobotToken rt, final String text,
            final Stack<ParsingState> processingState) {

        if (positionResolver.isCorrectPosition(PositionExpected.USER_KEYWORD_NAME, robotFileOutput.getFileModel(),
                currentLine, rt)) {
            if (processingState.contains(ParsingState.KEYWORD_TABLE_INSIDE)) {
                final String keywordName = rt.getText();
                return keywordName == null
                        || !keywordName.trim().startsWith(RobotTokenType.START_HASH_COMMENT.getRepresentation().get(0));
            }
        }
        return false;
    }

    @Override
    public RobotToken map(final RobotLine currentLine, final Stack<ParsingState> processingState,
            final RobotFileOutput robotFileOutput, final RobotToken rt, final FilePosition fp, final String text) {

        final List<IRobotTokenType> types = rt.getTypes();
        types.remove(RobotTokenType.UNKNOWN);
        types.add(0, RobotTokenType.KEYWORD_NAME);
        rt.setText(text);

        final KeywordTable keywordTable = robotFileOutput.getFileModel().getKeywordTable();
        final UserKeyword keyword = new UserKeyword(rt);
        keywordTable.addKeyword(keyword);

        processingState.push(ParsingState.KEYWORD_DECLARATION);
        return rt;
    }
}

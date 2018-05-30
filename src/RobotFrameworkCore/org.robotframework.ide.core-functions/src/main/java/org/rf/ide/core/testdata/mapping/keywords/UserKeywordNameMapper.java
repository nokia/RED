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

import com.google.common.annotations.VisibleForTesting;

public class UserKeywordNameMapper implements IParsingMapper {

    private final ElementPositionResolver positionResolver;

    public UserKeywordNameMapper() {
        this.positionResolver = new ElementPositionResolver();
    }

    @Override
    public RobotToken map(final RobotLine currentLine,
            final Stack<ParsingState> processingState,
            final RobotFileOutput robotFileOutput, final RobotToken rt, final FilePosition fp,
            final String text) {
        final List<IRobotTokenType> types = rt.getTypes();
        types.remove(RobotTokenType.UNKNOWN);
        types.add(0, RobotTokenType.KEYWORD_NAME);
        rt.setText(text);

        final KeywordTable keywordTable = robotFileOutput.getFileModel()
                .getKeywordTable();
        final UserKeyword keyword = new UserKeyword(rt);
        keywordTable.addKeyword(keyword);

        processingState.push(ParsingState.KEYWORD_DECLARATION);

        return rt;
    }

    @Override
    public boolean checkIfCanBeMapped(final RobotFileOutput robotFileOutput,
            final RobotLine currentLine, final RobotToken rt, final String text,
            final Stack<ParsingState> processingState) {
        boolean result = false;
        if (positionResolver.isCorrectPosition(
                PositionExpected.USER_KEYWORD_NAME,
                robotFileOutput.getFileModel(), currentLine, rt)) {
            if (isIncludedInKeywordTable(currentLine, processingState)) {
                boolean wasUpdated = false;
                final String keywordName = rt.getText();
                if (keywordName != null) {
                    result = !keywordName.trim().startsWith(
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
    protected boolean isIncludedInKeywordTable(final RobotLine line,
            final Stack<ParsingState> processingState) {

        return processingState.contains(ParsingState.KEYWORD_TABLE_INSIDE);
    }
}

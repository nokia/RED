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
import org.rf.ide.core.testdata.mapping.table.ParsingStateHelper;
import org.rf.ide.core.testdata.model.FilePosition;
import org.rf.ide.core.testdata.model.RobotFileOutput;
import org.rf.ide.core.testdata.model.table.RobotEmptyRow;
import org.rf.ide.core.testdata.model.table.RobotExecutableRow;
import org.rf.ide.core.testdata.model.table.keywords.UserKeyword;
import org.rf.ide.core.testdata.text.read.IRobotTokenType;
import org.rf.ide.core.testdata.text.read.ParsingState;
import org.rf.ide.core.testdata.text.read.RobotLine;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;
import org.rf.ide.core.testdata.text.read.recognizer.executables.RobotSpecialTokens;

public class KeywordExecutableRowActionMapper implements IParsingMapper {

    private final ElementPositionResolver posResolver;

    private final ParsingStateHelper stateHelper;

    private final KeywordFinder keywordFinder;

    private final RobotSpecialTokens specialTokensRecognizer;

    public KeywordExecutableRowActionMapper() {
        this.posResolver = new ElementPositionResolver();
        this.stateHelper = new ParsingStateHelper();
        this.keywordFinder = new KeywordFinder();
        this.specialTokensRecognizer = new RobotSpecialTokens();
    }

    @Override
    public RobotToken map(final RobotLine currentLine, final Stack<ParsingState> processingState, final RobotFileOutput robotFileOutput,
            final RobotToken rt, final FilePosition fp, final String text) {
        final UserKeyword keyword = keywordFinder.findOrCreateNearestKeyword(currentLine, robotFileOutput);
        final List<IRobotTokenType> types = rt.getTypes();
        types.add(0, RobotTokenType.KEYWORD_ACTION_NAME);
        types.remove(RobotTokenType.UNKNOWN);

        final List<RobotToken> specialTokens = specialTokensRecognizer.recognize(fp, text);
        for (final RobotToken token : specialTokens) {
            types.addAll(token.getTypes());
        }

        final RobotExecutableRow<UserKeyword> row = new RobotExecutableRow<>();
        if (text.startsWith("#") || RobotExecutableRow.isTsvComment(text, robotFileOutput.getFileFormat())) {
            types.remove(RobotTokenType.KEYWORD_ACTION_NAME);
            types.remove(RobotTokenType.KEYWORD_ACTION_ARGUMENT);
            types.add(RobotTokenType.START_HASH_COMMENT);
            row.addCommentPart(rt);
        } else {
            row.setAction(rt);
        }
        keyword.addElement(row);

        processingState.push(ParsingState.KEYWORD_INSIDE_ACTION);
        return rt;
    }

    @Override
    public boolean checkIfCanBeMapped(final RobotFileOutput robotFileOutput, final RobotLine currentLine, final RobotToken rt,
            final String text, final Stack<ParsingState> processingState) {
        boolean result = false;
        final ParsingState state = stateHelper.getCurrentStatus(processingState);
        if (state == ParsingState.KEYWORD_TABLE_INSIDE || state == ParsingState.KEYWORD_DECLARATION) {
            if (!RobotEmptyRow.isEmpty(text) || !currentLine.getLineElements().isEmpty()) {
                if (posResolver.isCorrectPosition(PositionExpected.KEYWORD_EXEC_ROW_ACTION_NAME,
                        robotFileOutput.getFileModel(), currentLine, rt)) {
                    result = true;
                } else if (posResolver.isCorrectPosition(PositionExpected.USER_KEYWORD_NAME,
                        robotFileOutput.getFileModel(), currentLine, rt)) {
                    if (text.trim().startsWith(RobotTokenType.START_HASH_COMMENT.getRepresentation().get(0))) {
                        if (!rt.getTypes().contains(RobotTokenType.START_HASH_COMMENT)) {
                            rt.getTypes().add(RobotTokenType.START_HASH_COMMENT);
                        }
                        result = true;
                    }
                }
            }
        }

        return result;
    }
}

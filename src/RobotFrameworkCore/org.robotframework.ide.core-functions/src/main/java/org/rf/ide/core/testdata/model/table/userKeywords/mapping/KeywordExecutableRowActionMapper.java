/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testData.model.table.userKeywords.mapping;

import java.util.List;
import java.util.Stack;

import org.rf.ide.core.testData.model.FilePosition;
import org.rf.ide.core.testData.model.RobotFileOutput;
import org.rf.ide.core.testData.model.table.RobotExecutableRow;
import org.rf.ide.core.testData.model.table.mapping.ElementPositionResolver;
import org.rf.ide.core.testData.model.table.mapping.IParsingMapper;
import org.rf.ide.core.testData.model.table.mapping.ParsingStateHelper;
import org.rf.ide.core.testData.model.table.mapping.ElementPositionResolver.PositionExpected;
import org.rf.ide.core.testData.model.table.userKeywords.UserKeyword;
import org.rf.ide.core.testData.text.read.IRobotTokenType;
import org.rf.ide.core.testData.text.read.ParsingState;
import org.rf.ide.core.testData.text.read.RobotLine;
import org.rf.ide.core.testData.text.read.recognizer.RobotToken;
import org.rf.ide.core.testData.text.read.recognizer.RobotTokenType;
import org.rf.ide.core.testData.text.read.recognizer.executables.RobotSpecialTokens;


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
    public RobotToken map(RobotLine currentLine,
            Stack<ParsingState> processingState,
            RobotFileOutput robotFileOutput, RobotToken rt, FilePosition fp,
            String text) {
        UserKeyword keyword = keywordFinder.findOrCreateNearestKeyword(
                currentLine, processingState, robotFileOutput, rt, fp);
        List<IRobotTokenType> types = rt.getTypes();
        types.add(0, RobotTokenType.KEYWORD_ACTION_NAME);
        types.remove(RobotTokenType.UNKNOWN);

        List<RobotToken> specialTokens = specialTokensRecognizer.recognize(fp,
                text);
        for (RobotToken token : specialTokens) {
            types.addAll(token.getTypes());
        }

        RobotExecutableRow<UserKeyword> row = new RobotExecutableRow<UserKeyword>();
        row.setAction(rt);
        keyword.addKeywordExecutionRow(row);

        processingState.push(ParsingState.KEYWORD_INSIDE_ACTION);
        return rt;
    }


    @Override
    public boolean checkIfCanBeMapped(RobotFileOutput robotFileOutput,
            RobotLine currentLine, RobotToken rt, String text,
            Stack<ParsingState> processingState) {
        boolean result = false;
        ParsingState state = stateHelper.getCurrentStatus(processingState);
        if (state == ParsingState.KEYWORD_TABLE_INSIDE
                || state == ParsingState.KEYWORD_DECLARATION) {
            if (posResolver.isCorrectPosition(
                    PositionExpected.KEYWORD_EXEC_ROW_ACTION_NAME,
                    robotFileOutput.getFileModel(), currentLine, rt)) {
                result = true;
            } else if (posResolver.isCorrectPosition(
                    PositionExpected.USER_KEYWORD_NAME,
                    robotFileOutput.getFileModel(), currentLine, rt)) {
                result = (text.trim()
                        .startsWith(RobotTokenType.START_HASH_COMMENT
                                .getRepresentation().get(0)));
            }
        }

        return result;
    }
}

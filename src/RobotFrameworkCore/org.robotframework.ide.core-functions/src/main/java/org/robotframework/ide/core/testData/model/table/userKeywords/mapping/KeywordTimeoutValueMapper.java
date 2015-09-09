/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.core.testData.model.table.userKeywords.mapping;

import java.util.List;
import java.util.Stack;

import org.robotframework.ide.core.testData.model.FilePosition;
import org.robotframework.ide.core.testData.model.RobotFileOutput;
import org.robotframework.ide.core.testData.model.table.KeywordTable;
import org.robotframework.ide.core.testData.model.table.mapping.ElementsUtility;
import org.robotframework.ide.core.testData.model.table.mapping.IParsingMapper;
import org.robotframework.ide.core.testData.model.table.userKeywords.KeywordTimeout;
import org.robotframework.ide.core.testData.model.table.userKeywords.UserKeyword;
import org.robotframework.ide.core.testData.text.read.IRobotTokenType;
import org.robotframework.ide.core.testData.text.read.ParsingState;
import org.robotframework.ide.core.testData.text.read.RobotLine;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotTokenType;

import com.google.common.annotations.VisibleForTesting;


public class KeywordTimeoutValueMapper implements IParsingMapper {

    private final ElementsUtility utility;


    public KeywordTimeoutValueMapper() {
        this.utility = new ElementsUtility();
    }


    @Override
    public RobotToken map(RobotLine currentLine,
            Stack<ParsingState> processingState,
            RobotFileOutput robotFileOutput, RobotToken rt, FilePosition fp,
            String text) {
        List<IRobotTokenType> types = rt.getTypes();
        types.remove(RobotTokenType.UNKNOWN);
        types.add(0, RobotTokenType.KEYWORD_SETTING_TIMEOUT_VALUE);
        rt.setRaw(new StringBuilder(text));
        rt.setText(new StringBuilder(text));

        KeywordTable keywordTable = robotFileOutput.getFileModel()
                .getKeywordTable();
        List<UserKeyword> keywords = keywordTable.getKeywords();
        UserKeyword keyword = keywords.get(keywords.size() - 1);
        List<KeywordTimeout> timeouts = keyword.getTimeouts();

        timeouts.get(timeouts.size() - 1).setTimeout(rt);
        processingState.push(ParsingState.KEYWORD_SETTING_TIMEOUT_VALUE);

        return rt;
    }


    @Override
    public boolean checkIfCanBeMapped(RobotFileOutput robotFileOutput,
            RobotLine currentLine, RobotToken rt, String text,
            Stack<ParsingState> processingState) {
        boolean result = false;
        ParsingState state = utility.getCurrentStatus(processingState);

        if (state == ParsingState.KEYWORD_SETTING_TIMEOUT) {
            KeywordTable keywordTable = robotFileOutput.getFileModel()
                    .getKeywordTable();
            List<UserKeyword> keywords = keywordTable.getKeywords();
            UserKeyword keyword = keywords.get(keywords.size() - 1);
            List<KeywordTimeout> timeouts = keyword.getTimeouts();

            result = !checkIfHasAlreadyValue(timeouts);
        }
        return result;
    }


    @VisibleForTesting
    protected boolean checkIfHasAlreadyValue(
            List<KeywordTimeout> keywordTimeouts) {
        boolean result = false;
        for (KeywordTimeout setting : keywordTimeouts) {
            result = (setting.getTimeout() != null);
            result = result || !setting.getMessage().isEmpty();
            if (result) {
                break;
            }
        }

        return result;
    }
}

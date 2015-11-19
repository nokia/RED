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
import org.robotframework.ide.core.testData.model.table.mapping.IParsingMapper;
import org.robotframework.ide.core.testData.model.table.mapping.ParsingStateHelper;
import org.robotframework.ide.core.testData.model.table.userKeywords.KeywordTimeout;
import org.robotframework.ide.core.testData.model.table.userKeywords.UserKeyword;
import org.robotframework.ide.core.testData.text.read.IRobotTokenType;
import org.robotframework.ide.core.testData.text.read.ParsingState;
import org.robotframework.ide.core.testData.text.read.RobotLine;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotTokenType;

import com.google.common.annotations.VisibleForTesting;


public class KeywordTimeoutValueMapper implements IParsingMapper {

    private final ParsingStateHelper utility;


    public KeywordTimeoutValueMapper() {
        this.utility = new ParsingStateHelper();
    }


    @Override
    public RobotToken map(final RobotLine currentLine,
            final Stack<ParsingState> processingState,
            final RobotFileOutput robotFileOutput, final RobotToken rt, final FilePosition fp,
            final String text) {
        final List<IRobotTokenType> types = rt.getTypes();
        types.remove(RobotTokenType.UNKNOWN);
        types.add(0, RobotTokenType.KEYWORD_SETTING_TIMEOUT_VALUE);
        rt.setText(text);
        rt.setRaw(text);

        final KeywordTable keywordTable = robotFileOutput.getFileModel()
                .getKeywordTable();
        final List<UserKeyword> keywords = keywordTable.getKeywords();
        final UserKeyword keyword = keywords.get(keywords.size() - 1);
        final List<KeywordTimeout> timeouts = keyword.getTimeouts();

        timeouts.get(timeouts.size() - 1).setTimeout(rt);
        processingState.push(ParsingState.KEYWORD_SETTING_TIMEOUT_VALUE);

        return rt;
    }


    @Override
    public boolean checkIfCanBeMapped(final RobotFileOutput robotFileOutput,
            final RobotLine currentLine, final RobotToken rt, final String text,
            final Stack<ParsingState> processingState) {
        boolean result = false;
        final ParsingState state = utility.getCurrentStatus(processingState);

        if (state == ParsingState.KEYWORD_SETTING_TIMEOUT) {
            final KeywordTable keywordTable = robotFileOutput.getFileModel()
                    .getKeywordTable();
            final List<UserKeyword> keywords = keywordTable.getKeywords();
            final UserKeyword keyword = keywords.get(keywords.size() - 1);
            final List<KeywordTimeout> timeouts = keyword.getTimeouts();

            result = !checkIfHasAlreadyValue(timeouts);
        }
        return result;
    }


    @VisibleForTesting
    protected boolean checkIfHasAlreadyValue(
            final List<KeywordTimeout> keywordTimeouts) {
        boolean result = false;
        for (final KeywordTimeout setting : keywordTimeouts) {
            result = (setting.getTimeout() != null);
            result = result || !setting.getMessage().isEmpty();
            if (result) {
                break;
            }
        }

        return result;
    }
}

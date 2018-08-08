/*
 * Copyright 2015 Nokia Solutions and Networks
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
import org.rf.ide.core.testdata.model.table.LocalSetting;
import org.rf.ide.core.testdata.model.table.keywords.UserKeyword;
import org.rf.ide.core.testdata.text.read.ParsingState;
import org.rf.ide.core.testdata.text.read.RobotLine;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;

public class KeywordTeardownArgumentMapper implements IParsingMapper {

    private final ParsingStateHelper stateHelper = new ParsingStateHelper();

    @Override
    public boolean checkIfCanBeMapped(final RobotFileOutput robotFileOutput, final RobotLine currentLine,
            final RobotToken rt, final String text, final Stack<ParsingState> processingState) {

        if (stateHelper.getCurrentState(processingState) == ParsingState.KEYWORD_SETTING_TEARDOWN) {
            final List<UserKeyword> keywords = robotFileOutput.getFileModel().getKeywordTable().getKeywords();
            final List<LocalSetting<UserKeyword>> teardowns = keywords.get(keywords.size() - 1).getTeardowns();
            return KeywordTeardownNameMapper.hasKeywordNameAlready(teardowns);
        }
        return stateHelper.getCurrentState(processingState) == ParsingState.KEYWORD_SETTING_TEARDOWN_KEYWORD
                || stateHelper
                        .getCurrentState(processingState) == ParsingState.KEYWORD_SETTING_TEARDOWN_KEYWORD_ARGUMENT;
    }

    @Override
    public RobotToken map(final RobotLine currentLine, final Stack<ParsingState> processingState,
            final RobotFileOutput robotFileOutput, final RobotToken rt, final FilePosition fp, final String text) {

        rt.setText(text);

        final List<UserKeyword> keywords = robotFileOutput.getFileModel().getKeywordTable().getKeywords();
        final UserKeyword keyword = keywords.get(keywords.size() - 1);
        final List<LocalSetting<UserKeyword>> teardowns = keyword.getTeardowns();
        final LocalSetting<UserKeyword> teardown = teardowns.get(teardowns.size() - 1);
        teardown.addToken(rt);

        processingState.push(ParsingState.KEYWORD_SETTING_TEARDOWN_KEYWORD_ARGUMENT);
        return rt;
    }
}

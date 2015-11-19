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
import org.robotframework.ide.core.testData.model.table.userKeywords.KeywordTags;
import org.robotframework.ide.core.testData.model.table.userKeywords.UserKeyword;
import org.robotframework.ide.core.testData.text.read.ParsingState;
import org.robotframework.ide.core.testData.text.read.RobotLine;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotTokenType;


public class KeywordTagsTagNameMapper implements IParsingMapper {

    private final ParsingStateHelper utility;


    public KeywordTagsTagNameMapper() {
        this.utility = new ParsingStateHelper();
    }


    @Override
    public RobotToken map(final RobotLine currentLine,
            final Stack<ParsingState> processingState,
            final RobotFileOutput robotFileOutput, final RobotToken rt, final FilePosition fp,
            final String text) {
        rt.getTypes().add(0, RobotTokenType.KEYWORD_SETTING_TAGS_TAG_NAME);
        rt.setText(text);
        rt.setRaw(text);

        final KeywordTable keywordTable = robotFileOutput.getFileModel()
                .getKeywordTable();
        final List<UserKeyword> keywords = keywordTable.getKeywords();
        final UserKeyword keyword = keywords.get(keywords.size() - 1);
        final List<KeywordTags> tags = keyword.getTags();
        final KeywordTags keywordTags = tags.get(tags.size() - 1);
        keywordTags.addTag(rt);

        processingState.push(ParsingState.KEYWORD_SETTING_TAGS_TAG_NAME);

        return rt;
    }


    @Override
    public boolean checkIfCanBeMapped(final RobotFileOutput robotFileOutput,
            final RobotLine currentLine, final RobotToken rt, final String text,
            final Stack<ParsingState> processingState) {
        boolean result = false;
        final ParsingState state = utility.getCurrentStatus(processingState);
        result = (state == ParsingState.KEYWORD_SETTING_TAGS || state == ParsingState.KEYWORD_SETTING_TAGS_TAG_NAME);

        return result;
    }

}

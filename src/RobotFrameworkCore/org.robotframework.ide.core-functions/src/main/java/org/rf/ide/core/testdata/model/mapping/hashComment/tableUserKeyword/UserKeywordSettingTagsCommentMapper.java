/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.mapping.hashComment.tableUserKeyword;

import java.util.List;

import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.mapping.IHashCommentMapper;
import org.rf.ide.core.testdata.model.table.userKeywords.KeywordTags;
import org.rf.ide.core.testdata.model.table.userKeywords.UserKeyword;
import org.rf.ide.core.testdata.text.read.ParsingState;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;


public class UserKeywordSettingTagsCommentMapper implements IHashCommentMapper {

    @Override
    public boolean isApplicable(ParsingState state) {
        return (state == ParsingState.KEYWORD_SETTING_TAGS || state == ParsingState.KEYWORD_SETTING_TAGS_TAG_NAME);
    }


    @Override
    public void map(RobotToken rt, ParsingState currentState,
            RobotFile fileModel) {
        List<UserKeyword> keywords = fileModel.getKeywordTable().getKeywords();
        UserKeyword keyword = keywords.get(keywords.size() - 1);

        List<KeywordTags> tags = keyword.getTags();
        KeywordTags keywordTags = tags.get(tags.size() - 1);
        keywordTags.addCommentPart(rt);
    }
}

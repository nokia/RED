/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see licence.txt file for details.
 */
package org.robotframework.ide.core.testData.model.mapping.hashComment.tableUserKeyword;

import java.util.List;

import org.robotframework.ide.core.testData.model.RobotFile;
import org.robotframework.ide.core.testData.model.mapping.IHashCommentMapper;
import org.robotframework.ide.core.testData.model.table.userKeywords.KeywordDocumentation;
import org.robotframework.ide.core.testData.model.table.userKeywords.UserKeyword;
import org.robotframework.ide.core.testData.text.read.ParsingState;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;


public class UserKeywordSettingDocumentationCommentMapper implements
        IHashCommentMapper {

    @Override
    public boolean isApplicable(ParsingState state) {
        return (state == ParsingState.KEYWORD_SETTING_DOCUMENTATION_DECLARATION || state == ParsingState.KEYWORD_SETTING_DOCUMENTATION_TEXT);
    }


    @Override
    public void map(RobotToken rt, ParsingState currentState,
            RobotFile fileModel) {
        List<UserKeyword> keywords = fileModel.getKeywordTable().getKeywords();
        UserKeyword keyword = keywords.get(keywords.size() - 1);

        List<KeywordDocumentation> documentation = keyword.getDocumentation();
        KeywordDocumentation testDocumentation = documentation
                .get(documentation.size() - 1);
        testDocumentation.addCommentPart(rt);

    }

}

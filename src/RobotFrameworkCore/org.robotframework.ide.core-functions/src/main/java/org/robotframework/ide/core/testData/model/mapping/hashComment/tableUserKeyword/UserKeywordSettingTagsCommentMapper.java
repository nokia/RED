package org.robotframework.ide.core.testData.model.mapping.hashComment.tableUserKeyword;

import java.util.List;

import org.robotframework.ide.core.testData.model.listener.ITablesGetter;
import org.robotframework.ide.core.testData.model.mapping.IHashCommentMapper;
import org.robotframework.ide.core.testData.model.table.userKeywords.KeywordTags;
import org.robotframework.ide.core.testData.model.table.userKeywords.UserKeyword;
import org.robotframework.ide.core.testData.text.read.ParsingState;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;


public class UserKeywordSettingTagsCommentMapper implements IHashCommentMapper {

    @Override
    public boolean isApplicable(ParsingState state) {
        return (state == ParsingState.KEYWORD_SETTING_TAGS || state == ParsingState.KEYWORD_SETTING_TAGS_TAG_NAME);
    }


    @Override
    public void map(RobotToken rt, ParsingState currentState,
            ITablesGetter fileModel) {
        List<UserKeyword> keywords = fileModel.getKeywordTable().getKeywords();
        UserKeyword keyword = keywords.get(keywords.size() - 1);

        List<KeywordTags> tags = keyword.getTags();
        KeywordTags keywordTags = tags.get(tags.size() - 1);
        keywordTags.addCommentPart(rt);
    }
}

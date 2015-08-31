package org.robotframework.ide.core.testData.model.mapping.hashComment.tableUserKeyword;

import java.util.List;

import org.robotframework.ide.core.testData.model.listener.ITablesGetter;
import org.robotframework.ide.core.testData.model.mapping.IHashCommentMapper;
import org.robotframework.ide.core.testData.model.table.userKeywords.KeywordTeardown;
import org.robotframework.ide.core.testData.model.table.userKeywords.UserKeyword;
import org.robotframework.ide.core.testData.text.read.ParsingState;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;


public class UserKeywordSettingTeardownCommentMapper implements
        IHashCommentMapper {

    @Override
    public boolean isApplicable(ParsingState state) {
        return (state == ParsingState.KEYWORD_SETTING_TEARDOWN
                || state == ParsingState.KEYWORD_SETTING_TEARDOWN_KEYWORD || state == ParsingState.KEYWORD_SETTING_TEARDOWN_KEYWORD_ARGUMENT);
    }


    @Override
    public void map(RobotToken rt, ParsingState currentState,
            ITablesGetter fileModel) {
        List<UserKeyword> keywords = fileModel.getKeywordTable().getKeywords();
        UserKeyword keyword = keywords.get(keywords.size() - 1);

        List<KeywordTeardown> teardowns = keyword.getTeardowns();
        KeywordTeardown keywordTeardown = teardowns.get(teardowns.size() - 1);
        keywordTeardown.addCommentPart(rt);
    }
}

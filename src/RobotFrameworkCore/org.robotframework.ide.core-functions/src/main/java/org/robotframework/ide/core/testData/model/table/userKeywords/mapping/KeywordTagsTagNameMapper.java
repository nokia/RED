package org.robotframework.ide.core.testData.model.table.userKeywords.mapping;

import java.util.List;
import java.util.Stack;

import org.robotframework.ide.core.testData.model.FilePosition;
import org.robotframework.ide.core.testData.model.RobotFileOutput;
import org.robotframework.ide.core.testData.model.table.KeywordTable;
import org.robotframework.ide.core.testData.model.table.mapping.ElementsUtility;
import org.robotframework.ide.core.testData.model.table.mapping.IParsingMapper;
import org.robotframework.ide.core.testData.model.table.userKeywords.KeywordTags;
import org.robotframework.ide.core.testData.model.table.userKeywords.UserKeyword;
import org.robotframework.ide.core.testData.text.read.ParsingState;
import org.robotframework.ide.core.testData.text.read.RobotLine;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotTokenType;


public class KeywordTagsTagNameMapper implements IParsingMapper {

    private final ElementsUtility utility;


    public KeywordTagsTagNameMapper() {
        this.utility = new ElementsUtility();
    }


    @Override
    public RobotToken map(RobotLine currentLine,
            Stack<ParsingState> processingState,
            RobotFileOutput robotFileOutput, RobotToken rt, FilePosition fp,
            String text) {
        rt.setType(RobotTokenType.KEYWORD_SETTING_TAGS_TAG_NAME);
        rt.setText(new StringBuilder(text));

        KeywordTable keywordTable = robotFileOutput.getFileModel()
                .getKeywordTable();
        List<UserKeyword> keywords = keywordTable.getKeywords();
        UserKeyword keyword = keywords.get(keywords.size() - 1);
        List<KeywordTags> tags = keyword.getTags();
        KeywordTags keywordTags = tags.get(tags.size() - 1);
        keywordTags.addTag(rt);

        processingState.push(ParsingState.KEYWORD_SETTING_TAGS_TAG_NAME);

        return rt;
    }


    @Override
    public boolean checkIfCanBeMapped(RobotFileOutput robotFileOutput,
            RobotLine currentLine, RobotToken rt, String text,
            Stack<ParsingState> processingState) {
        boolean result = false;
        ParsingState state = utility.getCurrentStatus(processingState);
        result = (state == ParsingState.KEYWORD_SETTING_TAGS || state == ParsingState.KEYWORD_SETTING_TAGS_TAG_NAME);

        return result;
    }

}

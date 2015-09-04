package org.robotframework.ide.core.testData.model.table.userKeywords.mapping;

import java.util.List;
import java.util.Stack;

import org.robotframework.ide.core.testData.model.FilePosition;
import org.robotframework.ide.core.testData.model.RobotFileOutput;
import org.robotframework.ide.core.testData.model.table.userKeywords.KeywordTags;
import org.robotframework.ide.core.testData.model.table.userKeywords.UserKeyword;
import org.robotframework.ide.core.testData.text.read.IRobotTokenType;
import org.robotframework.ide.core.testData.text.read.ParsingState;
import org.robotframework.ide.core.testData.text.read.RobotLine;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotTokenType;


public class KeywordTagsMapper extends AKeywordSettingDeclarationMapper {

    public KeywordTagsMapper() {
        super(RobotTokenType.KEYWORD_SETTING_TAGS);
    }


    @Override
    public RobotToken map(RobotLine currentLine,
            Stack<ParsingState> processingState,
            RobotFileOutput robotFileOutput, RobotToken rt, FilePosition fp,
            String text) {
        List<IRobotTokenType> types = rt.getTypes();
        types.add(0, RobotTokenType.KEYWORD_SETTING_TAGS);
        types.add(RobotTokenType.KEYWORD_THE_FIRST_ELEMENT);

        rt.setText(new StringBuilder(text));

        UserKeyword keyword = finder.findOrCreateNearestKeyword(currentLine,
                processingState, robotFileOutput, rt, fp);
        KeywordTags tags = robotFileOutput.getObjectCreator()
                .createKeywordTags(rt);
        keyword.addTag(tags);

        processingState.push(ParsingState.KEYWORD_SETTING_TAGS);

        return rt;
    }
}

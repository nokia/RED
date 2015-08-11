package org.robotframework.ide.core.testData.model.table.userKeywords.mapping;

import java.util.Stack;

import org.robotframework.ide.core.testData.model.FilePosition;
import org.robotframework.ide.core.testData.model.RobotFileOutput;
import org.robotframework.ide.core.testData.model.table.userKeywords.KeywordArguments;
import org.robotframework.ide.core.testData.model.table.userKeywords.UserKeyword;
import org.robotframework.ide.core.testData.text.read.ParsingState;
import org.robotframework.ide.core.testData.text.read.RobotLine;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotTokenType;


public class KeywordArgumentsMapper extends AKeywordSettingDeclarationMapper {

    public KeywordArgumentsMapper() {
        super(RobotTokenType.KEYWORD_SETTING_ARGUMENTS);
    }


    @Override
    public RobotToken map(RobotLine currentLine,
            Stack<ParsingState> processingState,
            RobotFileOutput robotFileOutput, RobotToken rt, FilePosition fp,
            String text) {
        rt.setType(RobotTokenType.KEYWORD_SETTING_ARGUMENTS);
        rt.setText(new StringBuilder(text));

        UserKeyword keyword = finder.findOrCreateNearestKeyword(currentLine,
                processingState, robotFileOutput, rt, fp);
        KeywordArguments arguments = new KeywordArguments(rt);
        keyword.addArguments(arguments);

        processingState.push(ParsingState.KEYWORD_SETTING_ARGUMENTS);

        return rt;
    }
}

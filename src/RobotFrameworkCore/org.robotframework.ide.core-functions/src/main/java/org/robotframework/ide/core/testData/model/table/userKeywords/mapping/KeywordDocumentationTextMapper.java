package org.robotframework.ide.core.testData.model.table.userKeywords.mapping;

import java.util.List;
import java.util.Stack;

import org.robotframework.ide.core.testData.model.FilePosition;
import org.robotframework.ide.core.testData.model.RobotFileOutput;
import org.robotframework.ide.core.testData.model.table.mapping.ElementsUtility;
import org.robotframework.ide.core.testData.model.table.mapping.IParsingMapper;
import org.robotframework.ide.core.testData.model.table.userKeywords.KeywordDocumentation;
import org.robotframework.ide.core.testData.model.table.userKeywords.UserKeyword;
import org.robotframework.ide.core.testData.text.read.IRobotTokenType;
import org.robotframework.ide.core.testData.text.read.ParsingState;
import org.robotframework.ide.core.testData.text.read.RobotLine;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotTokenType;


public class KeywordDocumentationTextMapper implements IParsingMapper {

    private final ElementsUtility utility;


    public KeywordDocumentationTextMapper() {
        this.utility = new ElementsUtility();
    }


    @Override
    public RobotToken map(RobotLine currentLine,
            Stack<ParsingState> processingState,
            RobotFileOutput robotFileOutput, RobotToken rt, FilePosition fp,
            String text) {
        List<IRobotTokenType> types = rt.getTypes();
        types.remove(RobotTokenType.UNKNOWN);
        types.add(0, RobotTokenType.KEYWORD_SETTING_DOCUMENTATION_TEXT);
        rt.setRaw(new StringBuilder(text));
        rt.setText(new StringBuilder(text));
        List<UserKeyword> keywords = robotFileOutput.getFileModel()
                .getKeywordTable().getKeywords();
        UserKeyword keyword = keywords.get(keywords.size() - 1);
        List<KeywordDocumentation> documentations = keyword.getDocumentation();

        KeywordDocumentation keywordDoc = documentations.get(documentations
                .size() - 1);
        keywordDoc.addDocumentationText(rt);

        processingState.push(ParsingState.KEYWORD_SETTING_DOCUMENTATION_TEXT);

        return rt;
    }


    @Override
    public boolean checkIfCanBeMapped(RobotFileOutput robotFileOutput,
            RobotLine currentLine, RobotToken rt, String text,
            Stack<ParsingState> processingState) {
        boolean result = false;
        ParsingState state = utility.getCurrentStatus(processingState);
        if (state == ParsingState.KEYWORD_SETTING_DOCUMENTATION_DECLARATION
                || state == ParsingState.KEYWORD_SETTING_DOCUMENTATION_TEXT) {
            result = true;
        }

        return result;
    }

}

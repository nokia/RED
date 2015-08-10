package org.robotframework.ide.core.testData.model.table.userKeywords.mapping;

import java.util.List;
import java.util.Stack;

import org.robotframework.ide.core.testData.model.FilePosition;
import org.robotframework.ide.core.testData.model.RobotFileOutput;
import org.robotframework.ide.core.testData.model.table.KeywordTable;
import org.robotframework.ide.core.testData.model.table.mapping.ElementsUtility;
import org.robotframework.ide.core.testData.model.table.mapping.IParsingMapper;
import org.robotframework.ide.core.testData.model.table.userKeywords.KeywordTimeout;
import org.robotframework.ide.core.testData.model.table.userKeywords.UserKeyword;
import org.robotframework.ide.core.testData.text.read.IRobotTokenType;
import org.robotframework.ide.core.testData.text.read.ParsingState;
import org.robotframework.ide.core.testData.text.read.RobotLine;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotTokenType;

import com.google.common.annotations.VisibleForTesting;


public class KeywordTimeoutMessageMapper implements IParsingMapper {

    private final ElementsUtility utility;


    public KeywordTimeoutMessageMapper() {
        this.utility = new ElementsUtility();
    }


    @Override
    public RobotToken map(RobotLine currentLine,
            Stack<ParsingState> processingState,
            RobotFileOutput robotFileOutput, RobotToken rt, FilePosition fp,
            String text) {
        List<IRobotTokenType> types = rt.getTypes();
        types.remove(RobotTokenType.UNKNOWN);
        types.add(0, RobotTokenType.KEYWORD_SETTING_TIMEOUT_MESSAGE);
        rt.setRaw(new StringBuilder(text));
        rt.setText(new StringBuilder(text));

        KeywordTable keywordTable = robotFileOutput.getFileModel()
                .getKeywordTable();
        List<UserKeyword> keywords = keywordTable.getKeywords();
        UserKeyword keyword = keywords.get(keywords.size() - 1);
        List<KeywordTimeout> timeouts = keyword.getTimeouts();
        KeywordTimeout keywordTimeout = timeouts.get(timeouts.size() - 1);
        keywordTimeout.addMessagePart(rt);

        processingState
                .push(ParsingState.KEYWORD_SETTING_TIMEOUT_MESSAGE_ARGUMENTS);

        return rt;
    }


    @Override
    public boolean checkIfCanBeMapped(RobotFileOutput robotFileOutput,
            RobotLine currentLine, RobotToken rt, String text,
            Stack<ParsingState> processingState) {
        boolean result;
        if (!processingState.isEmpty()) {
            ParsingState currentState = utility
                    .getCurrentStatus(processingState);
            if (currentState == ParsingState.KEYWORD_SETTING_TIMEOUT_VALUE
                    || currentState == ParsingState.KEYWORD_SETTING_TIMEOUT_MESSAGE_ARGUMENTS) {
                result = true;
            } else if (currentState == ParsingState.KEYWORD_SETTING_TIMEOUT) {
                List<UserKeyword> keywords = robotFileOutput.getFileModel()
                        .getKeywordTable().getKeywords();
                List<KeywordTimeout> keywordTimeouts = keywords.get(
                        keywords.size() - 1).getTimeouts();
                result = checkIfHasAlreadyValue(keywordTimeouts);
            } else {
                result = false;
            }
        } else {
            result = false;
        }
        return result;
    }


    @VisibleForTesting
    protected boolean checkIfHasAlreadyValue(
            List<KeywordTimeout> keywordTimeouts) {
        boolean result = false;
        for (KeywordTimeout setting : keywordTimeouts) {
            result = (setting.getTimeout() != null);
            result = result || !setting.getMessage().isEmpty();
            if (result) {
                break;
            }
        }

        return result;
    }

}

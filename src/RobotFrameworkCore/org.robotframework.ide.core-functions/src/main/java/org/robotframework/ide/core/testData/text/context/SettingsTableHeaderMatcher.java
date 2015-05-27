package org.robotframework.ide.core.testData.text.context;

import java.util.LinkedList;
import java.util.List;

import org.robotframework.ide.core.testData.text.AContextMatcher;
import org.robotframework.ide.core.testData.text.Context;
import org.robotframework.ide.core.testData.text.RobotToken;
import org.robotframework.ide.core.testData.text.RobotTokenContext;
import org.robotframework.ide.core.testData.text.RobotTokenType;
import org.robotframework.ide.core.testData.text.TxtRobotFileLexer.TokenizatorOutput;


public class SettingsTableHeaderMatcher extends AContextMatcher {

    public SettingsTableHeaderMatcher(TokenizatorOutput tokenProvider) {
        super(tokenProvider);
    }


    @Override
    public List<RobotTokenContext> findContexts(int lineNumber) {
        List<RobotTokenContext> myContextsFoundInLine = new LinkedList<>();

        List<RobotToken> tokens = this.tokenProvider.getTokens();
        RobotTokenContext current = new RobotTokenContext(
                Context.SETTING_TABLE_HEADER);

        for (int tokenIndex = lineNumber; tokenIndex < tokens.size(); tokenIndex++) {
            RobotToken currentToken = tokens.get(tokenIndex);
            RobotTokenType currentTokenType = currentToken.getType();

            if (currentTokenType == RobotTokenType.TABLE_ASTERISK) {

            } else if (currentTokenType == RobotTokenType.WORD_SETTING
                    || currentTokenType == RobotTokenType.WORD_METADATA) {

            } else if (currentTokenType == RobotTokenType.SPACE) {

            } else if (currentTokenType == RobotTokenType.TABULATOR) {

            } else if (currentTokenType == RobotTokenType.PIPE) {

            } else {

            }

            if (currentTokenType == RobotTokenType.END_OF_LINE
                    || currentTokenType == RobotTokenType.END_OF_FILE) {
                break;
            }
        }

        return myContextsFoundInLine;
    }
}

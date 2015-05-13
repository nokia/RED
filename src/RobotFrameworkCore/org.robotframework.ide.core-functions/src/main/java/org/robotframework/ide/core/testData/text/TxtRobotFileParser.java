package org.robotframework.ide.core.testData.text;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import org.robotframework.ide.core.testData.model.RobotTestDataFile;
import org.robotframework.ide.core.testData.text.contexts.ParserContextHandler;


public class TxtRobotFileParser {

    public RobotTestDataFile parse(InputStreamReader reader) throws IOException {
        RobotTestDataFile robotFile = new RobotTestDataFile();

        TxtRobotFileLexer lexer = new TxtRobotFileLexer();
        List<RobotToken> tokens = lexer.recognizeTokens(reader);

        ParserContextHandler parserContext = new ParserContextHandler();
        Separator sep = Separator.DOUBLE_SPACE_OR_TAB;
        for (int tokenIndex = 0; tokenIndex < tokens.size(); tokenIndex++) {
            RobotToken currentToken = tokens.get(tokenIndex);
            RobotTokenType currentTokenType = currentToken.getType();
            if (currentTokenType == RobotTokenType.START_LINE) {
                sep = sep.separator(tokenIndex, tokens);
            }

            parserContext.giveToken(currentToken, sep);
        }

        return robotFile;
    }
}

package org.robotframework.ide.core.testData.text.context.recognizer.settingTable;

import java.util.LinkedList;
import java.util.List;

import org.robotframework.ide.core.testData.text.context.ContextBuilder.ContextOutput;
import org.robotframework.ide.core.testData.text.context.IContextElement;
import org.robotframework.ide.core.testData.text.context.IContextElementType;
import org.robotframework.ide.core.testData.text.context.OneLineSingleRobotContextPart;
import org.robotframework.ide.core.testData.text.context.TokensLineIterator.LineTokenPosition;
import org.robotframework.ide.core.testData.text.context.recognizer.IContextRecognizer;
import org.robotframework.ide.core.testData.text.lexer.IRobotTokenType;
import org.robotframework.ide.core.testData.text.lexer.MultipleCharTokenType;
import org.robotframework.ide.core.testData.text.lexer.RobotSingleCharTokenType;
import org.robotframework.ide.core.testData.text.lexer.RobotToken;
import org.robotframework.ide.core.testData.text.lexer.RobotWordType;

import com.google.common.annotations.VisibleForTesting;


public abstract class ASettingTableElementRecognizer implements
        IContextRecognizer {

    private final SettingTableRobotContextType BUILD_TYPE;
    private final List<IRobotTokenType> expectedTokensSequence;
    private final int sequenceLength;


    protected ASettingTableElementRecognizer(
            final SettingTableRobotContextType type,
            final List<IRobotTokenType> expectedTokensSequence) {
        this.BUILD_TYPE = type;
        this.expectedTokensSequence = expectedTokensSequence;
        this.sequenceLength = expectedTokensSequence.size();
    }


    @Override
    public List<IContextElement> recognize(ContextOutput currentContext,
            LineTokenPosition lineInterval) {
        List<IContextElement> foundContexts = new LinkedList<>();
        OneLineSingleRobotContextPart context = createContext(lineInterval);

        List<RobotToken> tokens = currentContext.getTokenizedContent()
                .getTokens();
        int expectedTokenId = 0;
        IRobotTokenType currentType = expectedTokensSequence
                .get(expectedTokenId);
        for (int tokId = lineInterval.getStart(); tokId < lineInterval.getEnd(); tokId++) {
            RobotToken token = tokens.get(tokId);
            IRobotTokenType type = token.getType();

            if (type == currentType) {
                context.addNextToken(token);
                if (expectedTokenId + 1 >= sequenceLength) {
                    foundContexts.add(context);
                    context = createContext(lineInterval);
                } else {
                    expectedTokenId++;
                    currentType = expectedTokensSequence.get(expectedTokenId);
                }
            } else if (type == RobotSingleCharTokenType.SINGLE_SPACE
                    || type == RobotSingleCharTokenType.SINGLE_TABULATOR
                    || type == RobotSingleCharTokenType.SINGLE_PIPE
                    || type == RobotWordType.DOUBLE_SPACE
                    || type == RobotSingleCharTokenType.SINGLE_COMMENT_HASH
                    || type == MultipleCharTokenType.MANY_COMMENT_HASHS) {
                context.addNextToken(token);
            } else {
                // this is for ensure not line which is interesting for use
                context.removeAllContextTokens();
                expectedTokenId = 0;
                tokId = tokId - 1; // lets try to check if we do not have case
                                   // like i.e. Suite Suite Setup
            }
        }

        return foundContexts;
    }


    @Override
    public IContextElementType getContextType() {
        return BUILD_TYPE;
    }


    @VisibleForTesting
    protected OneLineSingleRobotContextPart createContext(
            final LineTokenPosition lineInterval) {
        return new OneLineSingleRobotContextPart(lineInterval.getLineNumber());
    }
}

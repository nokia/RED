package org.robotframework.ide.core.testData.text.context.recognizer;

import java.util.LinkedList;
import java.util.List;

import org.robotframework.ide.core.testData.text.context.ContextBuilder.ContextOutput;
import org.robotframework.ide.core.testData.text.context.IContextElement;
import org.robotframework.ide.core.testData.text.context.IContextElementType;
import org.robotframework.ide.core.testData.text.context.OneLineSingleRobotContextPart;
import org.robotframework.ide.core.testData.text.context.SimpleRobotContextType;
import org.robotframework.ide.core.testData.text.context.iterator.TokensLineIterator.LineTokenPosition;
import org.robotframework.ide.core.testData.text.lexer.IRobotTokenType;
import org.robotframework.ide.core.testData.text.lexer.RobotSingleCharTokenType;
import org.robotframework.ide.core.testData.text.lexer.RobotToken;

import com.google.common.annotations.VisibleForTesting;


/**
 * Contains literal all tokens as they present in line. It always return one
 * recognized line.
 * 
 * @author wypych
 * @since JDK 1.7 update 74
 * @version Robot Framework 2.9 alpha 2
 * 
 * 
 * @see SimpleRobotContextType#UNDECLARED_COMMENT
 */
public class UndeclaredCommentRecognizer implements IContextRecognizer {

    private final static SimpleRobotContextType BUILD_TYPE = SimpleRobotContextType.UNDECLARED_COMMENT;


    @Override
    public List<IContextElement> recognize(ContextOutput currentContext,
            LineTokenPosition lineInterval) {
        List<IContextElement> foundContexts = new LinkedList<>();
        OneLineSingleRobotContextPart context = createContext(lineInterval);

        List<RobotToken> tokens = currentContext.getTokenizedContent()
                .getTokens();

        boolean wasSomethingMoreThanLineEnd = false;
        for (int tokId = lineInterval.getStart(); tokId < lineInterval.getEnd(); tokId++) {
            RobotToken token = tokens.get(tokId);
            IRobotTokenType type = token.getType();

            context.addNextToken(token);
            if (type != RobotSingleCharTokenType.CARRIAGE_RETURN
                    && type != RobotSingleCharTokenType.LINE_FEED) {
                wasSomethingMoreThanLineEnd = true;
            }
        }

        if (wasSomethingMoreThanLineEnd) {
            context.setType(BUILD_TYPE);
            foundContexts.add(context);
        }

        return foundContexts;
    }


    @VisibleForTesting
    protected OneLineSingleRobotContextPart createContext(
            final LineTokenPosition lineInterval) {
        return new OneLineSingleRobotContextPart(lineInterval.getLineNumber());
    }


    @Override
    public IContextElementType getContextType() {
        return BUILD_TYPE;
    }
}

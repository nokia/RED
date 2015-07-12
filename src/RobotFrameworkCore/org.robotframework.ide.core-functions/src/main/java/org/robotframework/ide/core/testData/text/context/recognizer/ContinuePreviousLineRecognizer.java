package org.robotframework.ide.core.testData.text.context.recognizer;

import java.util.LinkedList;
import java.util.List;

import org.robotframework.ide.core.testData.text.context.ContextBuilder;
import org.robotframework.ide.core.testData.text.context.ContextBuilder.ContextOutput;
import org.robotframework.ide.core.testData.text.context.IContextElement;
import org.robotframework.ide.core.testData.text.context.IContextElementType;
import org.robotframework.ide.core.testData.text.context.OneLineSingleRobotContextPart;
import org.robotframework.ide.core.testData.text.context.SimpleRobotContextType;
import org.robotframework.ide.core.testData.text.context.TokensLineIterator.LineTokenPosition;
import org.robotframework.ide.core.testData.text.lexer.IRobotTokenType;
import org.robotframework.ide.core.testData.text.lexer.MultipleCharTokenType;
import org.robotframework.ide.core.testData.text.lexer.RobotToken;
import org.robotframework.ide.core.testData.text.lexer.RobotWordType;

import com.google.common.annotations.VisibleForTesting;


/**
 * Check if current line contains three dots close to each other - if yes than
 * it without special check mark this position as possible place, where previous
 * line is continue.
 * 
 * @author wypych
 * @since JDK 1.7 update 74
 * @version Robot Framework 2.9 alpha 2
 * 
 * @see ContextBuilder
 * @see RobotWordType#CONTINOUE_PREVIOUS_LINE_DOTS
 * @see MultipleCharTokenType#MORE_THAN_THREE_DOTS
 * 
 * @see SimpleRobotContextType#CONTINUE_PREVIOUS_LINE_DECLARATION
 */
public class ContinuePreviousLineRecognizer implements IContextRecognizer {

    private final static SimpleRobotContextType BUILD_TYPE = SimpleRobotContextType.CONTINUE_PREVIOUS_LINE_DECLARATION;


    @Override
    public List<IContextElement> recognize(ContextOutput currentContext,
            LineTokenPosition lineInterval) {
        List<IContextElement> foundContexts = new LinkedList<>();

        List<RobotToken> tokens = currentContext.getTokenizedContent()
                .getTokens();
        for (int tokId = lineInterval.getStart(); tokId < lineInterval.getEnd(); tokId++) {
            RobotToken token = tokens.get(tokId);
            IRobotTokenType type = token.getType();

            if (type == RobotWordType.CONTINOUE_PREVIOUS_LINE_DOTS
                    || type == MultipleCharTokenType.MORE_THAN_THREE_DOTS) {
                OneLineSingleRobotContextPart context = createContext(lineInterval);
                context.addNextToken(token);
                context.setType(BUILD_TYPE);
                foundContexts.add(context);
            }
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

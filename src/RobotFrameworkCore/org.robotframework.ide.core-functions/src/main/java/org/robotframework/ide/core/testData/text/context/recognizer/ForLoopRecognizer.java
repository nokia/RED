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
import org.robotframework.ide.core.testData.text.lexer.RobotSingleCharTokenType;
import org.robotframework.ide.core.testData.text.lexer.RobotToken;
import org.robotframework.ide.core.testData.text.lexer.RobotWordType;

import com.google.common.annotations.VisibleForTesting;


/**
 * Check if current line contains only whitespace or just no tokens.
 * 
 * @author wypych
 * @since JDK 1.7 update 74
 * @version Robot Framework 2.9 alpha 2
 * 
 * @see ContextBuilder
 * @see RobotSingleCharTokenType#SINGLE_COLON
 * @see RobotWordType#FOR_WORD
 * @see RobotWordType#IN_WORD
 * @see RobotWordType#RANGE_WORD
 * 
 * @see SimpleRobotContextType#FOR_LOOP_DECLARATION
 */
public class ForLoopRecognizer implements IContextRecognizer {

    private final static SimpleRobotContextType BUILD_TYPE = SimpleRobotContextType.FOR_LOOP_DECLARATION;


    @Override
    public List<IContextElement> recognize(ContextOutput currentContext,
            LineTokenPosition lineInterval) {
        List<IContextElement> foundContexts = new LinkedList<>();
        OneLineSingleRobotContextPart context = createContext(lineInterval);

        /**
         * Logic base on idea that only the first token are treat as begin of
         * loop, so in example:
         * 
         * <pre>
         * :FOR(id:1) FOR(id:2) IN(id:3) IN(id:4) RANGE(id:5)
         * </pre>
         * 
         * will result in getting tokens with id: 1, 3, 5 additionally only one
         * for-loop can exist in one line
         */
        List<RobotToken> tokens = currentContext.getTokenizedContent()
                .getTokens();

        boolean wasColon = false;
        boolean wasForWord = false;
        boolean wasInWord = false;
        for (int tokId = lineInterval.getStart(); tokId < lineInterval.getEnd(); tokId++) {
            RobotToken token = tokens.get(tokId);
            IRobotTokenType type = token.getType();

            if (type == RobotSingleCharTokenType.SINGLE_COLON) {
                if (context.getContextTokens().isEmpty() && !wasColon) {
                    context.addNextToken(token);
                    wasColon = true;
                }
            } else if (type == RobotWordType.FOR_WORD) {
                if (wasColon) {
                    context.addNextToken(token);
                    wasForWord = true;
                }
            } else if (type == RobotWordType.IN_WORD) {
                if (wasColon && wasForWord) {
                    context.addNextToken(token);
                    wasInWord = true;
                }
            } else {
                // invoke only if we have {@code :FOR} and {@code IN}
                if (type == RobotWordType.RANGE_WORD) {
                    if (wasColon && wasForWord && wasInWord) {
                        context.addNextToken(token);
                        context.setType(BUILD_TYPE);
                        foundContexts.add(context);
                        context = createContext(lineInterval);
                        wasColon = false;
                        wasForWord = false;
                        wasInWord = false;
                    }
                }
            }
        }

        if (wasColon && wasForWord && wasInWord) {
            context.setType(BUILD_TYPE);
            foundContexts.add(context);
        }

        System.out.println(foundContexts);

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
